package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BarkshellBlessing;
import com.github.laxika.magicalvibes.cards.e.EnvironmentalSciences;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({PrismariPledgemage.class, BarkshellBlessing.class, GiantGrowth.class, GrizzlyBears.class,
        EnvironmentalSciences.class})
class PrismariPledgemageTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack before its magecraft ability resolves")
    void cannotAttackBeforeMagecraft() {
        addCreatureReady(player1, new PrismariPledgemage());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Casting an instant lets Prismari Pledgemage attack this turn")
    void castingInstantAllowsAttack() {
        Permanent pledgemage = addCreatureReady(player1, new PrismariPledgemage());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, pledgemage.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        assertThat(pledgemage.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Copying an instant lets Prismari Pledgemage attack this turn")
    void copyingInstantAllowsAttack() {
        Permanent pledgemage = addCreatureReady(player1, new PrismariPledgemage());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BarkshellBlessing()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castWithConspire(player1, 0, pledgemage.getId(),
                List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        declareAttackers(List.of(0));

        assertThat(pledgemage.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Casting a sorcery grants attack permission before that spell resolves")
    void castingSorceryAllowsAttack() {
        Permanent pledgemage = addCreatureReady(player1, new PrismariPledgemage());
        harness.addToBattlefield(player2, new PrismariPledgemage());
        harness.setHand(player1, List.of(new EnvironmentalSciences()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(harness.getCombatAttackService().getAttackableCreatureIndices(gd, player1.getId())).contains(0);
        assertThat(pledgemage.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Casting an instant does not grant permission until magecraft resolves")
    void permissionWaitsForTriggerResolution() {
        Permanent pledgemage = addCreatureReady(player1, new PrismariPledgemage());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, pledgemage.getId());

        assertThat(harness.getCombatAttackService().getAttackableCreatureIndices(gd, player1.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(harness.getCombatAttackService().getAttackableCreatureIndices(gd, player1.getId())).contains(0);
    }

    @Test
    @DisplayName("Opponent's instant does not grant attack permission")
    void opponentsInstantDoesNotAllowAttack() {
        Permanent pledgemage = addCreatureReady(player1, new PrismariPledgemage());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, pledgemage.getId());
        resolveAllTriggers();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Casting a creature does not grant attack permission")
    void castingCreatureDoesNotAllowAttack() {
        addCreatureReady(player1, new PrismariPledgemage());
        harness.setHand(player1, List.of(new PrismariPledgemage()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Magecraft does not bypass summoning sickness")
    void magecraftDoesNotGrantHaste() {
        Permanent pledgemage = harness.addToBattlefieldAndReturn(player1, new PrismariPledgemage());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, pledgemage.getId());
        resolveAllTriggers();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Magecraft attack permission expires at end of turn")
    void permissionExpiresAtEndOfTurn() {
        Permanent pledgemage = addCreatureReady(player1, new PrismariPledgemage());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, pledgemage.getId());
        resolveAllTriggers();
        assertThat(harness.getCombatAttackService().getAttackableCreatureIndices(gd, player1.getId())).contains(0);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }
}
