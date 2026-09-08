package com.github.laxika.magicalvibes.cards.o;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.b.BenalishMissionary;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({Ophidian.class, BenalishInfantry.class, BenalishMissionary.class})
class OphidianTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new Ophidian());
        attacker.setAttacking(true);
        return attacker;
    }

    private void attackUnblocked() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        // Advance into the declare-blockers step (the defender has no blockers), firing the
        // "attacks and isn't blocked" trigger, then resolve it to present the may choice.
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting draws a card and the Ophidian deals no combat damage")
    void unblockedAcceptDrawsAndPreventsDamage() {
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, new ArrayList<>(List.of(new BenalishInfantry())));
        Permanent attacker = addAttacker();

        attackUnblocked();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isNull();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Declining draws nothing and the Ophidian deals its combat damage")
    void unblockedDeclineDealsDamage() {
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, new ArrayList<>(List.of(new BenalishInfantry())));
        Permanent attacker = addAttacker();

        attackUnblocked();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isNull();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Accepting prevents combat damage only from the Ophidian")
    void acceptingOnlyPreventsOphidianDamage() {
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, new ArrayList<>(List.of(new BenalishInfantry())));
        Permanent ophidian = addAttacker();
        Permanent otherAttacker = addCreatureReady(player1, new BenalishMissionary());
        otherAttacker.setAttacking(true);

        attackUnblocked();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(ophidian.getId())
                .doesNotContain(otherAttacker.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Blocked attacker does not trigger the ability")
    void blockedNoTrigger() {
        harness.setHand(player1, new ArrayList<>());
        harness.setLibrary(player1, new ArrayList<>(List.of(new BenalishInfantry())));

        addCreatureReady(player2, new BenalishMissionary());

        addAttacker();

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
