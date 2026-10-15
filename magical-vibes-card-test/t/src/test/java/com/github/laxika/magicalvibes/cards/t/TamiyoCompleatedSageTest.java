package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JukaiNaturalist;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TamiyoCompleatedSage.class, Forest.class, GrizzlyBears.class, JukaiNaturalist.class,
        LeoninScimitar.class, Shock.class, Pacifism.class})
class TamiyoCompleatedSageTest extends BaseCardTest {

    @Test
    @DisplayName("+1 taps up to one artifact or creature and skips its next untap")
    void plusOneTapsArtifactOrCreatureThroughItsNextUntap() {
        Permanent tamiyo = addReadyTamiyo(5);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new LeoninScimitar());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(artifact.getId()));
        harness.passBothPriorities();

        assertThat(artifact.isTapped()).isTrue();
        assertThat(tamiyo.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);

        advanceToUpkeep(player2);

        assertThat(artifact.isTapped()).isTrue();
    }

    @Test
    @DisplayName("+1 cannot target a land")
    void plusOneCannotTargetLand() {
        addReadyTamiyo(5);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(forest.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-X exiles and copies a nonland permanent card with mana value X")
    void minusXExilesAndCopiesExactManaValuePermanent() {
        Permanent tamiyo = addReadyTamiyo(5);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        harness.activateAbility(player1, 0, 1, 2, bears.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bears);
        assertThat(findPermanents(player1, "Grizzly Bears"))
                .anyMatch(permanent -> permanent.getCard().isToken());
        assertThat(tamiyo.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    @DisplayName("-X cannot target a permanent card with a different mana value")
    void minusXCannotTargetDifferentManaValue() {
        addReadyTamiyo(5);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 1, 1, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-7 creates a legendary Notebook that reduces spells and draws cards")
    void minusSevenCreatesNotebook() {
        Permanent tamiyo = addReadyTamiyo(7);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        Permanent notebook = findPermanent(player1, "Tamiyo's Notebook");
        assertThat(tamiyo.getCounterCount(CounterType.LOYALTY)).isZero();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(1);

        Card shock = new Shock();
        harness.setLibrary(player1, List.of(shock));
        int notebookIndex = gd.playerBattlefields.get(player1.getId()).indexOf(notebook);
        harness.activateAbility(player1, notebookIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(shock);
    }

    @Test
    void compleatedEntersWithThreeLoyaltyWhenPaidWithLife() {
        harness.setHand(player1, List.of(new TamiyoCompleatedSage()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Tamiyo, Compleated Sage").getCounterCount(CounterType.LOYALTY))
                .isEqualTo(3);
        harness.assertLife(player1, 18);
    }

    @Test
    void compleatedEntersWithFiveLoyaltyWhenHybridSymbolPaidWithGreen() {
        harness.setHand(player1, List.of(new TamiyoCompleatedSage()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Tamiyo, Compleated Sage").getCounterCount(CounterType.LOYALTY))
                .isEqualTo(5);
        harness.assertLife(player1, 20);
    }

    @Test
    void compleatedEntersWithFiveLoyaltyWhenHybridSymbolPaidWithBlue() {
        harness.setHand(player1, List.of(new TamiyoCompleatedSage()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Tamiyo, Compleated Sage").getCounterCount(CounterType.LOYALTY))
                .isEqualTo(5);
        harness.assertLife(player1, 20);
    }

    @Test
    void plusOneMayChooseNoTarget() {
        Permanent tamiyo = addReadyTamiyo(5);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(tamiyo.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void plusOneLocksAnAlreadyTappedCreatureForOnlyOneUntap() {
        addReadyTamiyo(5);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.tap();

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void minusXCannotTargetAnOpponentsGraveyard() {
        addReadyTamiyo(5);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 2, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusZeroCannotTargetALand() {
        addReadyTamiyo(5);
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 0, forest.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusXCannotTargetAnInstantOfMatchingManaValue() {
        addReadyTamiyo(5);
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, 1, shock.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusXDoesNotCopyATargetThatLeavesTheGraveyard() {
        Permanent tamiyo = addReadyTamiyo(5);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));
        harness.activateAbility(player1, 0, 1, 2, bears.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(bears));

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Grizzly Bears")).isEmpty();
        assertThat(tamiyo.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    @Test
    void minusXCopiesAPlaneswalkerWithItsFullStartingLoyalty() {
        addReadyTamiyo(5);
        Card otherTamiyo = new TamiyoCompleatedSage();
        harness.setGraveyard(player1, List.of(otherTamiyo));

        harness.activateAbility(player1, 0, 1, 5, otherTamiyo.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(otherTamiyo);
        assertThat(findPermanents(player1, "Tamiyo, Compleated Sage")).hasSize(1);
        Permanent copy = findPermanent(player1, "Tamiyo, Compleated Sage");
        assertThat(copy.getCard().isToken()).isTrue();
        assertThat(copy.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void minusXCreatesAnAuraAttachedToAChosenCreature() {
        addReadyTamiyo(5);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card pacifism = new Pacifism();
        harness.setGraveyard(player1, List.of(pacifism));

        harness.activateAbility(player1, 0, 1, 2, pacifism.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, creature.getId());

        Permanent copy = findPermanent(player1, "Pacifism");
        assertThat(copy.getCard().isToken()).isTrue();
        assertThat(copy.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(pacifism);
    }

    @Test
    void minusXPreservesAllColorsOfAMulticoloredPermanent() {
        addReadyTamiyo(5);
        Card naturalist = new JukaiNaturalist();
        harness.setGraveyard(player1, List.of(naturalist));

        harness.activateAbility(player1, 0, 1, 2, naturalist.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        Permanent copy = findPermanent(player1, "Jukai Naturalist");
        assertThat(copy.getCard().isToken()).isTrue();
        assertThat(gqs.getEffectiveColors(gd, copy)).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
    }

    @Test
    void notebookReducesTwoGenericManaWithoutReducingColoredMana() {
        addReadyTamiyo(7);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new TamiyoCompleatedSage()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Tamiyo, Compleated Sage").getCounterCount(CounterType.LOYALTY))
                .isEqualTo(5);
        harness.assertLife(player1, 20);
    }

    private Permanent addReadyTamiyo(int loyalty) {
        Permanent tamiyo = harness.addToBattlefieldAndReturn(player1, new TamiyoCompleatedSage());
        tamiyo.setCounterCount(CounterType.LOYALTY, loyalty);
        tamiyo.setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return tamiyo;
    }
}
