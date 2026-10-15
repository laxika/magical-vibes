package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Aethersnipe;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillcomberGiant;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SowerOfTemptation.class, HillcomberGiant.class, Forest.class, Aethersnipe.class})
class SowerOfTemptationTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains control of target opponent creature")
    void etbGainsControlOfTargetCreature() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillcomberGiant());

        castSower(giant.getId());
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Hillcomber Giant now controlled by player1
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(giant.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(giant.getId()));

        // Tracked as source-dependent steal keyed to the Sower
        Permanent sower = findPermanent(player1, "Sower of Temptation");
        assertThat(gd.newestControlEffectFor(giant.getId()).sourcePermanentId()).isEqualTo(sower.getId());
    }

    @Test
    @DisplayName("Can target a creature its controller already controls")
    void canTargetOwnCreature() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillcomberGiant());

        castSower(giant.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(giant.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(giant.getId()));
    }

    @Test
    @DisplayName("Control remains if another player gains control of the Sower")
    void controlRemainsWhenAnotherPlayerGainsControlOfSower() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillcomberGiant());

        castSower(giant.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent firstSower = findPermanent(player1, "Sower of Temptation");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SowerOfTemptation()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castCreature(player2, 0, firstSower.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(firstSower.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(giant.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(giant.getId()));
    }

    @Test
    @DisplayName("ETB has no effect if the Sower leaves before the trigger resolves")
    void etbHasNoEffectIfSowerLeavesBeforeTriggerResolves() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillcomberGiant());

        castSower(giant.getId());
        harness.passBothPriorities();

        Permanent sower = findPermanent(player1, "Sower of Temptation");
        gd.playerBattlefields.get(player1.getId()).remove(sower);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(giant.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(giant.getId()));
    }

    @Test
    @DisplayName("ETB has no effect if the target leaves before the trigger resolves")
    void etbHasNoEffectIfTargetLeavesBeforeTriggerResolves() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillcomberGiant());

        castSower(giant.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).remove(giant);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(giant.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(giant.getId()));
    }

    @Test
    @DisplayName("Stolen creature returns to its owner when the Sower leaves the battlefield")
    void stolenCreatureReturnsWhenSowerBounced() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillcomberGiant());

        castSower(giant.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent sower = findPermanent(player1, "Sower of Temptation");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(giant.getId()));

        // Bounce the Sower with Aethersnipe
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Aethersnipe()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.castCreature(player2, 0, sower.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Hillcomber Giant returns to player2, tracking cleaned up
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(giant.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(giant.getId()));
        assertThat(gd.controlEffectsFor(giant.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new SowerOfTemptation()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Taking control does not untap the creature or let it attack immediately")
    void stolenTappedCreatureStaysTappedAndCannotAttackImmediately() {
        Permanent giant = addCreatureReady(player2, new HillcomberGiant());
        giant.tap();

        castSower(giant.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(giant.getId()));
        assertThat(giant.isTapped()).isTrue();
        assertThat(giant.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("An earlier Sower control effect resumes when a later Sower leaves")
    void earlierControlEffectResumesWhenLaterSowerLeaves() {
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillcomberGiant());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new SowerOfTemptation()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castCreature(player2, 0, giant.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent earlierSower = findPermanent(player2, "Sower of Temptation");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        castSower(giant.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent laterSower = findPermanent(player1, "Sower of Temptation");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(giant.getId()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Aethersnipe()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.castCreature(player2, 0, laterSower.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(giant.getId()))
                .anyMatch(p -> p.getId().equals(earlierSower.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(giant.getId()));
        harness.assertInHand(player1, "Sower of Temptation");
    }

    private void castSower(UUID targetId) {
        harness.setHand(player1, List.of(new SowerOfTemptation()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castCreature(player1, 0, 0, targetId);
    }
}
