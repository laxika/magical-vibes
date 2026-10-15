package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.h.HedgeTroll;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReveredDead.class, HedgeTroll.class})
class ReveredDeadTest extends BaseCardTest {

    @Test
    @DisplayName("Regeneration can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent dead = harness.addToBattlefieldAndReturn(player1, new ReveredDead());
        dead.setSummoningSick(true);
        dead.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dead.getRegenerationShield()).isEqualTo(1);
        assertThat(dead.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Revered Dead");
    }

    @Test
    @DisplayName("Multiple activations create separate shields without tapping the creature")
    void multipleActivationsCreateSeparateShields() {
        Permanent dead = addCreatureReady(player1, new ReveredDead());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dead.getRegenerationShield()).isEqualTo(2);
        assertThat(dead.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Paying white grants a regeneration shield")
    void whiteActivationGrantsRegenerationShield() {
        Permanent dead = addCreatureReady(player1, new ReveredDead());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(dead.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Regeneration shield saves Revered Dead from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent dead = addCreatureReady(player1, new ReveredDead());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        addCreatureReady(player2, new HedgeTroll());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Revered Dead");
        assertThat(dead.isTapped()).isTrue();
        assertThat(dead.isBlocking()).isFalse();
        assertThat(dead.getMarkedDamage()).isZero();
        assertThat(dead.getRegenerationShield()).isZero();
    }
}
