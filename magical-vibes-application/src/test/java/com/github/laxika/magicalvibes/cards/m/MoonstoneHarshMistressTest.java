package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PullFromEternity;
import com.github.laxika.magicalvibes.cards.s.Sift;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoonstoneHarshMistress.class, Forest.class, GrizzlyBears.class, Sift.class,
        TormodsCrypt.class, PullFromEternity.class})
class MoonstoneHarshMistressTest extends BaseCardTest {

    @Test
    void acceptingDiscardTriggerExilesCardAndGrantsPlayPermission() {
        Forest discarded = discardCard();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(discarded);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(discarded);
        assertThat(gd.exilePlayPermissions).containsEntry(discarded.getId(), player1.getId());

        harness.castFromExile(player1, discarded.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(discarded);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getOriginalCard().getId().equals(discarded.getId()));
    }

    @Test
    void decliningDiscardTriggerLeavesCardInGraveyard() {
        Card discarded = discardCard();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(discarded);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(discarded.getId());
    }

    @Test
    void opponentsDiscardDoesNotTriggerMoonstone() {
        harness.addToBattlefield(player1, new MoonstoneHarshMistress());
        Forest discarded = new Forest();
        harness.setHand(player2, List.of(new Sift(), discarded));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player2, 0, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(discarded);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(discarded.getId());
    }

    @Test
    void discardedCreatureCanBeCastByPayingItsNormalManaCost() {
        GrizzlyBears discarded = new GrizzlyBears();
        discardCard(discarded);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThatThrownBy(() -> harness.castFromExile(player1, discarded.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castFromExile(player1, discarded.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(discarded);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void permissionLastsThroughNextTurnAndThenExpires() {
        Forest discarded = discardCard();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(discarded.getId(), player1.getId());
        harness.passUntilWithNoAttackers(player1, TurnStep.POSTCOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsEntry(discarded.getId(), player1.getId());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(discarded);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(discarded.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, discarded.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed(TormodsCrypt.class)
    void cardRemovedFromGraveyardBeforeResolutionDoesNotGainPermission() {
        Forest discarded = discardCard();
        harness.addToBattlefield(player1, new TormodsCrypt());
        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(discarded);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(discarded.getId());
    }

    @Test
    @CardUsed({TormodsCrypt.class, PullFromEternity.class})
    void cardThatLeavesAndReturnsToGraveyardIsNotTheDiscardedObject() {
        Forest discarded = discardCard();
        harness.addToBattlefield(player1, new TormodsCrypt());
        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new PullFromEternity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, discarded.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(discarded);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(discarded.getId());
    }

    private Forest discardCard() {
        Forest discarded = new Forest();
        discardCard(discarded);
        return discarded;
    }

    private void discardCard(Card discarded) {
        harness.addToBattlefield(player1, new MoonstoneHarshMistress());
        harness.setHand(player1, List.of(new Sift(), discarded));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
    }
}
