package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SpidersilkNet.class, StoneworkPuma.class})
class SpidersilkNetTest extends BaseCardTest {

    @Test
    @DisplayName("Equip attaches Spidersilk Net to a creature")
    void equipsCreature() {
        Permanent net = addNetReady(player1);
        Permanent creature = addCreatureReady(player1, new StoneworkPuma());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(net.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature gets +0/+2 and reach")
    void equippedCreatureGetsBoostAndReach() {
        Permanent creature = addCreatureReady(player1, new StoneworkPuma());
        Permanent net = addNetReady(player1);
        net.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Spidersilk Net does not affect other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent creature = addCreatureReady(player1, new StoneworkPuma());
        Permanent otherCreature = addCreatureReady(player1, new StoneworkPuma());
        Permanent net = addNetReady(player1);
        net.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Equipped creature loses the boost and reach when Spidersilk Net is removed")
    void effectsStopWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new StoneworkPuma());
        Permanent net = addNetReady(player1);
        net.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(net);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();
    }

    @Test
    void reequippingMovesBoostAndReachToNewCreature() {
        Permanent net = addNetReady(player1);
        Permanent original = addCreatureReady(player1, new StoneworkPuma());
        Permanent replacement = addCreatureReady(player1, new StoneworkPuma());
        net.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, replacement.getId());

        assertThat(net.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(net.getAttachedTo()).isEqualTo(replacement.getId());
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, original, Keyword.REACH)).isFalse();
        assertThat(gqs.getEffectivePower(gd, replacement)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, replacement)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, replacement, Keyword.REACH)).isTrue();
    }

    @Test
    void cannotEquipOpponentsCreature() {
        Permanent net = addNetReady(player1);
        Permanent creature = addCreatureReady(player2, new StoneworkPuma());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");

        assertThat(net.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotEquipOutsideMainPhase() {
        addNetReady(player1);
        Permanent creature = addCreatureReady(player1, new StoneworkPuma());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void failedReequipLeavesOriginalCreatureEquipped() {
        Permanent net = addNetReady(player1);
        Permanent original = addCreatureReady(player1, new StoneworkPuma());
        Permanent replacement = addCreatureReady(player1, new StoneworkPuma());
        net.setAttachedTo(original.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, replacement.getId());
        gd.playerBattlefields.get(player1.getId()).remove(replacement);
        harness.passBothPriorities();

        assertThat(net.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, original, Keyword.REACH)).isTrue();
    }

    private Permanent addNetReady(Player player) {
        Permanent net = harness.addToBattlefieldAndReturn(player, new SpidersilkNet());
        net.setSummoningSick(false);
        return net;
    }

}
