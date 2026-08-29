package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Deathmark;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MaleficScytheTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a soul counter")
    void entersWithSoulCounter() {
        harness.setHand(player1, List.of(new MaleficScythe()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent scythe = findPermanent(player1, "Malefic Scythe");
        assertThat(scythe.getCounterCount(CounterType.SOUL)).isEqualTo(1);
    }

    @Test
    @DisplayName("Equipped creature gets +1/+1 for each soul counter")
    void equippedCreatureGetsBoostPerSoulCounter() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scythe = addScytheReady(player1);
        scythe.setCounterCount(CounterType.SOUL, 3);
        scythe.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Adds a soul counter when the equipped creature dies")
    void addsSoulCounterWhenEquippedCreatureDies() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent scythe = addScytheReady(player1);
        scythe.setCounterCount(CounterType.SOUL, 1);
        scythe.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Deathmark()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castSorcery(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(scythe.getCounterCount(CounterType.SOUL)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip attaches to a creature you control")
    void equipAttaches() {
        Permanent scythe = addScytheReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(scythe.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addScytheReady(Player player) {
        Permanent scythe = new Permanent(new MaleficScythe());
        scythe.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(scythe);
        return scythe;
    }
}
