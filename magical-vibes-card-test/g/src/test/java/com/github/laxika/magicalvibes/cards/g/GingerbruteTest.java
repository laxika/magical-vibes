package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.w.WildwoodTracker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Gingerbrute.class, WildwoodTracker.class})
class GingerbruteTest extends BaseCardTest {

    @Test
    @DisplayName("The ability prevents non-haste creatures from blocking this turn")
    void nonHasteCreatureCannotBlock() {
        Permanent gingerbrute = addReadyGingerbrute();
        Permanent blocker = addCreatureReady(player2, new WildwoodTracker());
        activateEvasionAbility(gingerbrute);
        gingerbrute.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, gingerbrute))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("haste");
    }

    @Test
    @DisplayName("The ability allows a creature with haste to block this turn")
    void hasteCreatureCanBlock() {
        Permanent gingerbrute = addReadyGingerbrute();
        Permanent blocker = addCreatureReady(player2, new Gingerbrute());
        activateEvasionAbility(gingerbrute);
        gingerbrute.setAttacking(true);
        prepareDeclareBlockers();

        declareBlock(blocker, gingerbrute);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The blocking restriction expires at end of turn")
    void evasionExpiresAtEndOfTurn() {
        Permanent gingerbrute = addReadyGingerbrute();
        Permanent blocker = addCreatureReady(player2, new WildwoodTracker());
        activateEvasionAbility(gingerbrute);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        gingerbrute.setAttacking(true);
        prepareDeclareBlockers();
        declareBlock(blocker, gingerbrute);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("The sacrifice ability gains 3 life")
    void sacrificeAbilityGainsLife() {
        addReadyGingerbrute();
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertInGraveyard(player1, "Gingerbrute");
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
    }

    private Permanent addReadyGingerbrute() {
        return addCreatureReady(player1, new Gingerbrute());
    }

    @Test
    void evasionCanBeActivatedWhileTapped() {
        Permanent gingerbrute = addReadyGingerbrute();
        Permanent blocker = addCreatureReady(player2, new WildwoodTracker());
        gingerbrute.tap();

        activateEvasionAbility(gingerbrute);
        gingerbrute.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, gingerbrute))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("haste");
    }

    @Test
    void evasionDoesNotRestrictBlockingAnotherGingerbrute() {
        Permanent source = addReadyGingerbrute();
        Permanent other = addReadyGingerbrute();
        Permanent blocker = addCreatureReady(player2, new WildwoodTracker());
        activateEvasionAbility(source);
        other.setAttacking(true);
        prepareDeclareBlockers();

        declareBlock(blocker, other);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void evasionRequiresOneMana() {
        addReadyGingerbrute();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sacrificeRequiresTwoMana() {
        Permanent gingerbrute = addReadyGingerbrute();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");

        assertThat(gingerbrute.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Gingerbrute");
        harness.assertNotInGraveyard(player1, "Gingerbrute");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sacrificeCannotBeActivatedWhileTapped() {
        Permanent gingerbrute = addReadyGingerbrute();
        gingerbrute.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        harness.assertOnBattlefield(player1, "Gingerbrute");
        harness.assertNotInGraveyard(player1, "Gingerbrute");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void hasteAllowsSacrificeAbilityOnTheTurnItEnters() {
        harness.addToBattlefield(player1, new Gingerbrute());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertInGraveyard(player1, "Gingerbrute");
        harness.assertLife(player1, 20);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
    }

    @Test
    void hasteAllowsAttackingOnTheTurnItEnters() {
        Permanent gingerbrute = harness.addToBattlefieldAndReturn(player1, new Gingerbrute());
        addCreatureReady(player2, new WildwoodTracker());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(gingerbrute.isAttacking()).isTrue();
    }

    private void activateEvasionAbility(Permanent gingerbrute) {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(gingerbrute),
                0, null, null);
        harness.passBothPriorities();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }
}
