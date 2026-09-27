package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FrostOgre;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Shuko.class, FrostOgre.class})
class ShukoTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new FrostOgre());
        Permanent shuko = addShukoReady(player1);
        shuko.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Resolving equip attaches Shuko to target creature without mana")
    void resolvingEquipAttachesWithoutMana() {
        Permanent shuko = addShukoReady(player1);
        Permanent creature = addCreatureReady(player1, new FrostOgre());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(shuko.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Successful equip is not reported as a fizzle")
    void successfulEquipIsNotReportedAsFizzle() {
        addShukoReady(player1);
        Permanent creature = addCreatureReady(player1, new FrostOgre());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gameLogContains("Shuko's equip ability fizzles")).isFalse();
    }

    @Test
    @DisplayName("Equip cannot target a creature controlled by an opponent")
    void equipCannotTargetOpponentsCreature() {
        Permanent shuko = addShukoReady(player1);
        Permanent opponentCreature = addCreatureReady(player2, new FrostOgre());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");

        assertThat(shuko.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip can only be activated during your main phase")
    void equipRequiresSorcerySpeed() {
        Permanent shuko = addShukoReady(player1);
        Permanent creature = addCreatureReady(player1, new FrostOgre());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        assertThat(shuko.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Re-equipping moves Shuko's boost to the new creature")
    void reequippingMovesBoost() {
        Permanent shuko = addShukoReady(player1);
        Permanent firstCreature = addCreatureReady(player1, new FrostOgre());
        Permanent secondCreature = addCreatureReady(player1, new FrostOgre());
        shuko.setAttachedTo(firstCreature.getId());

        harness.activateAbility(player1, 0, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(shuko.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(6);
    }

    @Test
    @DisplayName("Shuko does not affect an unequipped creature")
    void doesNotAffectUnequippedCreature() {
        Permanent equippedCreature = addCreatureReady(player1, new FrostOgre());
        addCreatureReady(player1, new FrostOgre());
        Permanent shuko = addShukoReady(player1);
        shuko.setAttachedTo(equippedCreature.getId());

        Permanent unequippedCreature = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Frost Ogre"))
                .filter(permanent -> !permanent.getId().equals(equippedCreature.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(gqs.getEffectivePower(gd, unequippedCreature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, unequippedCreature)).isEqualTo(3);
    }

    private Permanent addShukoReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new Shuko());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
