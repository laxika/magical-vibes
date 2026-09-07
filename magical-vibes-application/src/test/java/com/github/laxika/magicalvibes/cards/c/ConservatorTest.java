package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
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

@CardUsed({Conservator.class, GrizzlyBears.class, LightningBolt.class})
class ConservatorTest extends BaseCardTest {

    @Test
    void preventsNextTwoDamageAcrossSeparateEvents() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new Conservator());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new LightningBolt(), new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 16);
    }

    @Test
    void requiresUntappedSource() {
        harness.addToBattlefield(player1, new Conservator());
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shieldExpiresAtEndOfTurn() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new Conservator());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Ability prevents the next 2 combat damage dealt to controller")
    void preventsTwoDamageToController() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new Conservator());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        // Opponent attacks the shielded controller with a 2/2.
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player2);

        // 2 damage fully prevented → life unchanged.
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Only the next 2 damage is prevented; excess still gets through")
    void preventsOnlyTwoOfLargerHit() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new Conservator());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        // Opponent attacks with a 5/5 → 2 prevented, 3 gets through.
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setPowerModifier(3);
        attacker.setToughnessModifier(3);
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("Cannot activate without paying the {3} cost")
    void requiresManaCost() {
        harness.addToBattlefield(player1, new Conservator());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
