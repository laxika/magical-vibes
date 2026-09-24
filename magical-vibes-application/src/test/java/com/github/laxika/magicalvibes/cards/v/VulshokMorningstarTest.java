package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.t.TelJiladWolf;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VulshokMorningstar.class, TelJiladWolf.class})
class VulshokMorningstarTest extends BaseCardTest {

    @Test
    @DisplayName("Activating equip ability targets the creature and consumes mana")
    void activatingEquip() {
        harness.addToBattlefieldAndReturn(player1, new VulshokMorningstar());
        Permanent creature = addCreatureReady(player1, new TelJiladWolf());
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, creature.getId());

        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Resolving equip attaches equipment to target creature")
    void resolvingEquipAttaches() {
        Permanent star = harness.addToBattlefieldAndReturn(player1, new VulshokMorningstar());
        Permanent creature = addCreatureReady(player1, new TelJiladWolf());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(star.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equipped creature gets +2/+2")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new TelJiladWolf());
        Permanent star = harness.addToBattlefieldAndReturn(player1, new VulshokMorningstar());
        star.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost is removed when equipment leaves the battlefield")
    void boostRemovedWhenEquipmentRemoved() {
        Permanent creature = addCreatureReady(player1, new TelJiladWolf());
        Permanent star = harness.addToBattlefieldAndReturn(player1, new VulshokMorningstar());
        star.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(star);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipment does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new TelJiladWolf());
        Permanent other = addCreatureReady(player1, new TelJiladWolf());
        Permanent star = harness.addToBattlefieldAndReturn(player1, new VulshokMorningstar());
        star.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipCannotTargetOpponentsCreature() {
        harness.addToBattlefieldAndReturn(player1, new VulshokMorningstar());
        Permanent creature = addCreatureReady(player2, new TelJiladWolf());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Equip cannot target a noncreature permanent")
    void equipCannotTargetNoncreaturePermanent() {
        harness.addToBattlefieldAndReturn(player1, new VulshokMorningstar());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new VulshokMorningstar());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Equip can only be activated at sorcery speed")
    void equipIsSorcerySpeedOnly() {
        harness.addToBattlefieldAndReturn(player1, new VulshokMorningstar());
        Permanent creature = addCreatureReady(player1, new TelJiladWolf());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
