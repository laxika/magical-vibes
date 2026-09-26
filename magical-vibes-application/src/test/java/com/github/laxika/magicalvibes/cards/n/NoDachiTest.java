package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NoDachi.class, WanderingOnes.class})
class NoDachiTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +2/+0 and first strike")
    void equippedCreatureBoostedAndHasFirstStrike() {
        Permanent creature = addCreatureReady(player1, new WanderingOnes());
        Permanent noDachi = addNoDachiReady(player1);
        noDachi.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Equip {3} attaches No-Dachi to a creature you control")
    void equipAttachesToCreature() {
        Permanent noDachi = addNoDachiReady(player1);
        Permanent creature = addCreatureReady(player1, new WanderingOnes());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(noDachi.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equip costs three mana")
    void equipCostsThreeMana() {
        addNoDachiReady(player1);
        Permanent creature = addCreatureReady(player1, new WanderingOnes());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Equip cannot be activated outside your main phase")
    void equipOnlyActivatesAtSorcerySpeed() {
        addNoDachiReady(player1);
        Permanent creature = addCreatureReady(player1, new WanderingOnes());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Equip cannot target a creature controlled by an opponent")
    void equipOnlyTargetsCreatureYouControl() {
        Permanent noDachi = addNoDachiReady(player1);
        Permanent opponentCreature = addCreatureReady(player2, new WanderingOnes());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
        assertThat(noDachi.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Unattached No-Dachi boosts nothing")
    void unattachedBoostsNothing() {
        Permanent creature = addCreatureReady(player1, new WanderingOnes());
        addNoDachiReady(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Boost and first strike end when No-Dachi leaves the battlefield")
    void boostEndsWhenEquipmentLeaves() {
        Permanent creature = addCreatureReady(player1, new WanderingOnes());
        Permanent noDachi = addNoDachiReady(player1);
        noDachi.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(noDachi);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    private Permanent addNoDachiReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new NoDachi());
    }
}
