package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NightmareLashTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1 for each Swamp its equipment controller controls")
    void boostsPerSwamp() {
        Permanent bears = addCreatureReady(player1);
        Permanent lash = addLash(player1);
        lash.setAttachedTo(bears.getId());

        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Swamp boost updates dynamically")
    void updatesDynamicallyWithSwampCount() {
        Permanent bears = addCreatureReady(player1);
        Permanent lash = addLash(player1);
        lash.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        harness.addToBattlefield(player1, new Swamp());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Swamp"));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip pays 3 life and attaches Nightmare Lash to a creature you control")
    void equipPaysLifeAndAttaches() {
        Permanent lash = addLash(player1);
        Permanent bears = addCreatureReady(player1);

        harness.setLife(player1, 20);
        harness.activateAbility(player1, 0, null, bears.getId());

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);

        harness.passBothPriorities();

        assertThat(lash.getAttachedTo()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentCreature() {
        Permanent lash = addLash(player1);
        Permanent opponentBears = addCreatureReady(player2);

        harness.setLife(player1, 20);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(lash.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip cannot be activated without enough life")
    void cannotEquipWithoutEnoughLife() {
        Permanent lash = addLash(player1);
        Permanent bears = addCreatureReady(player1);

        harness.setLife(player1, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
        assertThat(gd.getLife(player1.getId())).isEqualTo(2);
        assertThat(lash.getAttachedTo()).isNull();
    }

    private Permanent addLash(com.github.laxika.magicalvibes.model.Player player) {
        Permanent lash = new Permanent(new NightmareLash());
        gd.playerBattlefields.get(player.getId()).add(lash);
        return lash;
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }
}
