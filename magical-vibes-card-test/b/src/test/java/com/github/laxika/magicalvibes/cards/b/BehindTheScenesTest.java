package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.cards.h.HulkingDevil;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BehindTheScenes.class, DevilthornFox.class, HulkingDevil.class, Opalescence.class})
class BehindTheScenesTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control have skulk")
    void grantsSkulkToOwnCreatures() {
        harness.addToBattlefield(player1, new BehindTheScenes());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.SKULK)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.SKULK)).isFalse();
    }

    @Test
    @DisplayName("Activated ability gives your creatures +1/+1 until end of turn")
    void abilityBoostsOwnCreatures() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new BehindTheScenes());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new DevilthornFox());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(enchantment.getPowerModifier()).isEqualTo(0);
        assertThat(ownCreature.getPowerModifier()).isEqualTo(1);
        assertThat(ownCreature.getToughnessModifier()).isEqualTo(1);
        assertThat(opposingCreature.getPowerModifier()).isEqualTo(0);
        assertThat(opposingCreature.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Activated ability wears off at end of turn")
    void abilityBoostWearsOff() {
        harness.addToBattlefield(player1, new BehindTheScenes());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(ownCreature.getPowerModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(ownCreature.getPowerModifier()).isEqualTo(0);
        assertThat(ownCreature.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("An animated Behind the Scenes grants skulk to itself")
    void animatedEnchantmentHasSkulk() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new BehindTheScenes());
        harness.addToBattlefield(player1, new Opalescence());
        Permanent blocker = addCreatureReady(player2, new HulkingDevil());

        assertThat(gqs.isCreature(gd, enchantment)).isTrue();
        assertThat(gqs.hasKeyword(gd, enchantment, Keyword.SKULK)).isTrue();
        assertThat(bls.canBlockAttacker(gd, blocker, enchantment,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
    }

    @Test
    @DisplayName("Skulk forbids greater-power blockers but allows equal and lesser power")
    void skulkUsesCurrentPower() {
        harness.addToBattlefield(player1, new BehindTheScenes());
        Permanent attacker = addCreatureReady(player1, new DevilthornFox());
        Permanent equalBlocker = addCreatureReady(player2, new DevilthornFox());
        Permanent greaterBlocker = addCreatureReady(player2, new HulkingDevil());

        assertThat(bls.canBlockAttacker(gd, equalBlocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
        assertThat(bls.canBlockAttacker(gd, greaterBlocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isFalse();

        harness.addMana(player1, ManaColor.WHITE, 10);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
        assertThat(bls.canBlockAttacker(gd, equalBlocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
        assertThat(bls.canBlockAttacker(gd, greaterBlocker, attacker,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Skulk applies to new creatures and disappears when the enchantment leaves")
    void staticGrantTracksBattlefield() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new BehindTheScenes());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.SKULK)).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(enchantment);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.SKULK)).isFalse();
    }

    @Test
    @DisplayName("The boost affects creatures present at resolution, not creatures entering afterward")
    void boostLocksInCreaturesAtResolution() {
        harness.addToBattlefield(player1, new BehindTheScenes());
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.activateAbility(player1, 0, null, null);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());

        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());

        assertThat(beforeResolution.getPowerModifier()).isEqualTo(1);
        assertThat(beforeResolution.getToughnessModifier()).isEqualTo(1);
        assertThat(afterResolution.getPowerModifier()).isZero();
        assertThat(afterResolution.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.SKULK)).isTrue();
    }

    @Test
    @DisplayName("An activated boost resolves after Behind the Scenes leaves the battlefield")
    void boostResolvesWithoutSource() {
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new BehindTheScenes());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(enchantment);

        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.SKULK)).isFalse();
    }
}
