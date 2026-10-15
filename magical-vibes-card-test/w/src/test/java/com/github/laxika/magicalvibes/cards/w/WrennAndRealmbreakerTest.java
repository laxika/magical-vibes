package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.r.RestInPeace;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({WrennAndRealmbreaker.class, Forest.class, GrizzlyBears.class, Shock.class, RestInPeace.class})
class WrennAndRealmbreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Lands you control gain an ability to add one mana of any color")
    void landsGainAnyColorManaAbility() {
        addReadyWrenn(player1, 3);
        Permanent forest = addLand(player1);

        harness.activateAbility(player1, 1, null, null);

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("+1 animates up to one land with vigilance, hexproof, and haste until your next turn")
    void plusOneAnimatesTargetLand() {
        Permanent wrenn = addReadyWrenn(player1, 3);
        Permanent forest = addLand(player1);

        harness.activateAbility(player1, 0, 0, forest.getId(), null);
        harness.passBothPriorities();

        assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, forest)).contains(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
        assertThat(gqs.isLand(gd, forest)).isTrue();
    }

    @Test
    @DisplayName("+1 may be activated without choosing a land")
    void plusOneMayChooseNoTarget() {
        Permanent wrenn = addReadyWrenn(player1, 3);
        Permanent forest = addLand(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gqs.isCreature(gd, forest)).isFalse();
    }

    @Test
    @DisplayName("-2 mills three cards and may return a milled permanent to hand")
    void minusTwoMillsAndReturnsPermanent() {
        Permanent wrenn = addReadyWrenn(player1, 3);
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest, new Shock(), new Shock()));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("-7 grants an emblem to play lands and cast permanent spells from the graveyard")
    void minusSevenGrantsGraveyardPermission() {
        Permanent wrenn = addReadyWrenn(player1, 7);
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(forest, bears));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(wrenn.getCounterCount(CounterType.LOYALTY)).isEqualTo(0);
        assertThat(gd.emblems).hasSize(1);

        harness.playGraveyardLand(player1, gd.playerGraveyards.get(player1.getId()).indexOf(forest));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromGraveyard(player1, gd.playerGraveyards.get(player1.getId()).indexOf(bears));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void animationLastsThroughOpponentsTurnAndExpiresOnActivatorsNextTurn() {
        addReadyWrenn(player1, 4);
        Permanent forest = addLand(player1);

        harness.activateAbility(player1, 0, 0, forest.getId(), null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HEXPROOF)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isFalse();
    }

    @Test
    void changingLandControllerDoesNotShortenAnimationDuration() {
        addReadyWrenn(player1, 4);
        Permanent forest = addLand(player1);
        harness.activateAbility(player1, 0, 0, forest.getId(), null);
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(forest);
        gd.playerBattlefields.get(player2.getId()).add(forest);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    void minusTwoCanDeclineReturningPermanentFromShortLibrary() {
        addReadyWrenn(player1, 4);
        Card forest = new Forest();
        Card oldPermanent = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(oldPermanent));
        harness.setLibrary(player1, List.of(forest));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(oldPermanent, forest);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void minusTwoReturnsOnlyOneOfMultipleMilledPermanents() {
        addReadyWrenn(player1, 4);
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, third);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void minusTwoDoesNotOfferOldPermanentsWhenOnlyInstantsAreMilled() {
        addReadyWrenn(player1, 4);
        Card oldPermanent = new Forest();
        harness.setGraveyard(player1, List.of(oldPermanent));
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4).contains(oldPermanent);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void emblemDoesNotAllowInstantsOrAdditionalLandPlays() {
        addReadyWrenn(player1, 7);
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(firstLand, secondLand, shock));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 2))
                .isInstanceOf(IllegalStateException.class);
        harness.playGraveyardLand(player1, 0);
        assertThatThrownBy(() -> harness.playGraveyardLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(secondLand, shock);
    }

    @Test
    void minusTwoCanReturnPermanentMilledIntoExile() {
        addReadyWrenn(player1, 4);
        harness.addToBattlefield(player2, new RestInPeace());
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest, new Shock(), new Shock()));
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
    private Permanent addReadyWrenn(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new WrennAndRealmbreaker());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private Permanent addLand(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new Forest());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
