package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.h.HillcomberGiant;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KinsbaileSkirmisher.class, HillcomberGiant.class, NamelessInversion.class})
class KinsbaileSkirmisherTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives target creature +1/+1 until end of turn")
    void etbBoostsTargetCreature() {
        UUID giantId = harness.addToBattlefieldAndReturn(player2, new HillcomberGiant()).getId();

        harness.setHand(player1, List.of(new KinsbaileSkirmisher()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0, giantId);
        resolveAllTriggers();

        Permanent giant = findPermanent(player2, "Hillcomber Giant");
        assertThat(giant.getEffectivePower()).isEqualTo(4);
        assertThat(giant.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("ETB boosts only the chosen creature")
    void etbBoostsOnlyChosenCreature() {
        harness.addToBattlefield(player2, new HillcomberGiant());
        harness.addToBattlefield(player2, new HillcomberGiant());
        List<Permanent> giants = findPermanents(player2, "Hillcomber Giant");
        UUID chosenGiantId = giants.get(0).getId();

        harness.setHand(player1, List.of(new KinsbaileSkirmisher()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0, chosenGiantId);
        resolveAllTriggers();

        assertThat(giants.get(0).getEffectivePower()).isEqualTo(4);
        assertThat(giants.get(0).getEffectiveToughness()).isEqualTo(4);
        assertThat(giants.get(1).getEffectivePower()).isEqualTo(3);
        assertThat(giants.get(1).getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        UUID giantId = harness.addToBattlefieldAndReturn(player2, new HillcomberGiant()).getId();

        harness.setHand(player1, List.of(new KinsbaileSkirmisher()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0, giantId);
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent giant = findPermanent(player2, "Hillcomber Giant");
        assertThat(giant.getEffectivePower()).isEqualTo(3);
        assertThat(giant.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Can target its own controller's creature")
    void canTargetOwnCreature() {
        UUID giantId = harness.addToBattlefieldAndReturn(player1, new HillcomberGiant()).getId();

        harness.setHand(player1, List.of(new KinsbaileSkirmisher()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0, giantId);
        resolveAllTriggers();

        Permanent giant = findPermanent(player1, "Hillcomber Giant");
        assertThat(giant.getEffectivePower()).isEqualTo(4);
        assertThat(giant.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        UUID giantId = harness.addToBattlefieldAndReturn(player2, new HillcomberGiant()).getId();

        harness.setHand(player1, List.of(new KinsbaileSkirmisher()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0, giantId);
        harness.passBothPriorities(); // resolve creature spell → ETB on stack

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities(); // resolve ETB → fizzles

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("Can target itself when it is the only creature")
    void canTargetItself() {
        harness.castFromHand(player1, new KinsbaileSkirmisher(), "{1}{W}");
        harness.passBothPriorities();

        Permanent skirmisher = findPermanent(player1, "Kinsbaile Skirmisher");
        harness.handlePermanentChosen(player1, skirmisher.getId());
        resolveAllTriggers();

        assertThat(skirmisher.getEffectivePower()).isEqualTo(3);
        assertThat(skirmisher.getEffectiveToughness()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB still boosts its target after Skirmisher leaves the battlefield")
    void triggerResolvesAfterSourceLeaves() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillcomberGiant());
        harness.setHand(player1, List.of(new KinsbaileSkirmisher()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0, giant.getId());
        harness.passBothPriorities();

        Permanent skirmisher = findPermanent(player1, "Kinsbaile Skirmisher");
        harness.setHand(player2, List.of(new NamelessInversion()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, skirmisher.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kinsbaile Skirmisher");
        assertThat(giant.getEffectivePower()).isEqualTo(3);
        assertThat(giant.getEffectiveToughness()).isEqualTo(3);

        resolveAllTriggers();

        assertThat(giant.getEffectivePower()).isEqualTo(4);
        assertThat(giant.getEffectiveToughness()).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }
}
