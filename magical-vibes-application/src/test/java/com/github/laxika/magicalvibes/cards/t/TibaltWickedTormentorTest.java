package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TibaltWickedTormentor.class, GrizzlyBears.class, Shock.class, Forest.class})
class TibaltWickedTormentorTest extends BaseCardTest {

    @Test
    @DisplayName("+1 adds red mana and drafts an exiled spellbook card")
    void plusOneAddsManaAndDraftsCard() {
        addReadyTibalt(player1, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.SpellbookDraftChoice interaction =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookDraftChoice.class);
        Card chosen = interaction.cards().getFirst();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(chosen.getId()));
        assertThat(gd.exilePlayPermissions).containsEntry(chosen.getId(), player1.getId());
    }

    @Test
    @DisplayName("The second +1 lets the target controller take damage and then offers a rummage")
    void secondPlusOneOffersDamageAndRummage() {
        addReadyTibalt(player1, 3);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Card discard = new GrizzlyBears();
        Card draw = new Shock();
        harness.setHand(player1, List.of(discard));
        harness.setLibrary(player1, List.of(draw));

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discard);
        assertThat(gd.playerHands.get(player1.getId())).contains(draw);
    }

    @Test
    @DisplayName("The second +1 deals damage to the target when its controller declines")
    void secondPlusOneDamagesTargetWhenDeclined() {
        addReadyTibalt(player1, 3);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("-X creates X Devils with the death trigger")
    void minusXCreatesDevils() {
        addReadyTibalt(player1, 4);

        harness.activateAbility(player1, 0, 2, 2, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Devil")).hasSize(2);
    }

    @Test
    @DisplayName("The -X Devils deal 1 damage to a chosen target when they die")
    void devilDealsDamageWhenItDies() {
        addReadyTibalt(player1, 4);
        harness.activateAbility(player1, 0, 2, 1, null);
        harness.passBothPriorities();
        Permanent devil = findPermanents(player1, "Devil").getFirst();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, devil.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The second +1 cannot target a land")
    void secondPlusOneCannotTargetLand() {
        addReadyTibalt(player1, 3);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyTibalt(Player player, int loyalty) {
        Permanent tibalt = new Permanent(new TibaltWickedTormentor());
        tibalt.setCounterCount(CounterType.LOYALTY, loyalty);
        tibalt.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(tibalt);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return tibalt;
    }

    protected Permanent addCreatureReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
