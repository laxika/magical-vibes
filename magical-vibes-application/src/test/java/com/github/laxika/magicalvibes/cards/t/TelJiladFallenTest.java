package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromCardTypesEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelJiladFallenTest extends BaseCardTest {

    @Test
    @DisplayName("Has static protection from artifacts effect")
    void hasStaticProtectionFromArtifacts() {
        TelJiladFallen card = new TelJiladFallen();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).get(0)).isInstanceOf(ProtectionFromCardTypesEffect.class);

        ProtectionFromCardTypesEffect effect = (ProtectionFromCardTypesEffect) card.getEffects(EffectSlot.STATIC).get(0);
        assertThat(effect.cardTypes()).containsExactly(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Infect deals damage to creatures as -1/-1 counters")
    void infectDealsDamageAsMinusOneCounters() {
        Permanent fallen = new Permanent(new TelJiladFallen());
        fallen.setSummoningSick(false);
        fallen.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(fallen);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        // Grizzly Bears (2/2) receives 3 -1/-1 counters from infect (3 power), killing it
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Infect deals damage to players as poison counters")
    void infectDealsDamageAsPoisonCounters() {
        harness.setLife(player2, 20);

        Permanent fallen = new Permanent(new TelJiladFallen());
        fallen.setSummoningSick(false);
        fallen.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(fallen);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Life unchanged because infect deals poison, not life loss
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        // 3 power = 3 poison counters
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(3);
    }

    @Test
    @DisplayName("Protection from artifacts prevents blocking by artifact creature")
    void protectionPreventsBlockingByArtifactCreature() {
        Permanent fallen = new Permanent(new TelJiladFallen());
        fallen.setSummoningSick(false);
        fallen.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(fallen);

        Permanent ironMyr = new Permanent(new IronMyr());
        ironMyr.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(ironMyr);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from artifacts allows blocking by non-artifact creature")
    void protectionAllowsBlockingByNonArtifactCreature() {
        Permanent fallen = new Permanent(new TelJiladFallen());
        fallen.setSummoningSick(false);
        fallen.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(fallen);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(bears.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Static protection from artifacts persists after resetModifiers")
    void staticProtectionPersistsAfterReset() {
        harness.addToBattlefield(player1, new TelJiladFallen());

        Permanent fallen = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Tel-Jilad Fallen"))
                .findFirst().orElseThrow();

        // Simulate end of turn cleanup
        fallen.resetModifiers();

        // Static protection should still work (it's on the card, not on the permanent's mutable set)
        Permanent artifactSource = new Permanent(new IronMyr());
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, fallen, artifactSource)).isTrue();
    }

    @Test
    @DisplayName("Static protection from artifacts does not protect from non-artifact sources")
    void staticProtectionDoesNotProtectFromNonArtifacts() {
        harness.addToBattlefield(player1, new TelJiladFallen());

        Permanent fallen = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Tel-Jilad Fallen"))
                .findFirst().orElseThrow();

        Permanent nonArtifactSource = new Permanent(new GrizzlyBears());
        assertThat(gqs.hasProtectionFromSourceCardTypes(gd, fallen, nonArtifactSource)).isFalse();
    }
}
