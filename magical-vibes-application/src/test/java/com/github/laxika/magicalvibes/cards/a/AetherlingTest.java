package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AetherlingTest extends BaseCardTest {

    @Test
    @DisplayName("First ability exiles Aetherling")
    void firstAbilityExiles() {
        addAetherlingReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Aetherling");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Aetherling"));
    }

    @Test
    @DisplayName("Exiled Aetherling returns at the beginning of the next end step")
    void returnsAtEndStep() {
        addAetherlingReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Aetherling");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Aetherling"));
    }

    @Test
    @DisplayName("Second ability makes Aetherling unblockable this turn")
    void secondAbilityMakesUnblockable() {
        Permanent aetherling = addAetherlingReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(aetherling.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Unblockable wears off at end of turn")
    void unblockableWearsOff() {
        Permanent aetherling = addAetherlingReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(aetherling.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Third ability gives +1/-1 until end of turn")
    void thirdAbilityBoosts() {
        Permanent aetherling = addAetherlingReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aetherling)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, aetherling)).isEqualTo(4);
    }

    @Test
    @DisplayName("Fourth ability gives -1/+1 until end of turn")
    void fourthAbilityBoosts() {
        Permanent aetherling = addAetherlingReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 3, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aetherling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, aetherling)).isEqualTo(6);
    }

    @Test
    @DisplayName("Pump wears off at end of turn")
    void pumpWearsOff() {
        Permanent aetherling = addAetherlingReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aetherling)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, aetherling)).isEqualTo(5);
    }

    private Permanent addAetherlingReady(Player player) {
        Permanent perm = new Permanent(new Aetherling());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
