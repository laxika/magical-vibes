package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NecropolisFiend.class, Forest.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class NecropolisFiendTest extends BaseCardTest {

    @Test
    @DisplayName("Delve exiles graveyard cards to pay the generic creature cost")
    void delvePaysGenericCost() {
        List<Card> graveyard = List.of(
                new Shock(), new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new NecropolisFiend()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2, 3, 4, 5, 6));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrderElementsOf(graveyard);
        harness.assertOnBattlefield(player1, "Necropolis Fiend");
    }

    @Test
    @DisplayName("Exiling X graveyard cards gives the target creature -X/-X")
    void activatedAbilityScalesWithExiledCards() {
        Permanent fiend = addCreatureReady(player1, new NecropolisFiend());
        Permanent target = addCreatureReady(player2, new HillGiant());
        Shock first = new Shock();
        Shock second = new Shock();
        harness.setGraveyard(player1, List.of(first, second, new Shock()));
        forceMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        assertThat(fiend.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The X graveyard cost is checked before paying mana")
    void requiresEnoughCardsForX() {
        Permanent fiend = addCreatureReady(player1, new NecropolisFiend());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        List<Card> graveyard = List.of(new Shock());
        harness.setGraveyard(player1, graveyard);
        forceMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyard);
        assertThat(fiend.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The activated ability cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new NecropolisFiend());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forceMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The temporary debuff wears off at cleanup")
    void debuffWearsOffAtCleanup() {
        addCreatureReady(player1, new NecropolisFiend());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        forceMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("X zero works with an empty graveyard and still taps the Fiend")
    void zeroXWithEmptyGraveyard() {
        Permanent fiend = addCreatureReady(player1, new NecropolisFiend());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of());
        forceMainPhase();

        harness.activateAbility(player1, 0, 0, target.getId());
        harness.passBothPriorities();

        assertThat(fiend.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("X zero allows choosing no cards from a nonempty graveyard")
    void zeroXWithNonemptyGraveyard() {
        Permanent fiend = addCreatureReady(player1, new NecropolisFiend());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        forceMainPhase();

        harness.activateAbility(player1, 0, 0, target.getId());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(fiend.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(shock);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Reducing toughness to zero puts the target into its owner's graveyard")
    void lethalToughnessReduction() {
        addCreatureReady(player1, new NecropolisFiend());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Shock first = new Shock();
        Shock second = new Shock();
        harness.setGraveyard(player1, List.of(first, second));
        forceMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Summoning sickness prevents the tap ability even for X zero")
    void summoningSicknessPreventsActivation() {
        Permanent fiend = harness.addToBattlefieldAndReturn(player1, new NecropolisFiend());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        forceMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(fiend.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped Fiend cannot activate its ability")
    void tappedFiendCannotActivate() {
        Permanent fiend = addCreatureReady(player1, new NecropolisFiend());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        fiend.tap();
        forceMainPhase();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Delve can pay part of the generic cost with mana paying the rest")
    void partialDelvePayment() {
        List<Card> graveyard = List.of(new Shock(), new Forest(), new GrizzlyBears());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new NecropolisFiend()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Necropolis Fiend");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyard.get(2));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(graveyard.get(0), graveyard.get(1));
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Delve cannot pay the black mana in the spell cost")
    void delveCannotPayColoredMana() {
        List<Card> graveyard = List.of(
                new Shock(), new Shock(), new Shock(), new Shock(),
                new Shock(), new Shock(), new Shock());
        harness.setGraveyard(player1, graveyard);
        harness.setHand(player1, List.of(new NecropolisFiend()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(
                player1, 0, List.of(0, 1, 2, 3, 4, 5, 6)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(graveyard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        harness.assertInHand(player1, "Necropolis Fiend");
    }

    private void forceMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
