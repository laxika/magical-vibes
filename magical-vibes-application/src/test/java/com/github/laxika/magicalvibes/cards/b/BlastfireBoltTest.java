package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;
import com.github.laxika.magicalvibes.cards.s.SeaMonster;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BlastfireBoltTest extends BaseCardTest {

    @Test
    @DisplayName("Blastfire Bolt deals 5 damage and kills a small creature")
    void deals5DamageAndKillsCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlastfireBolt()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Blastfire Bolt");
    }

    @Test
    @DisplayName("Blastfire Bolt destroys all Equipment attached to the target creature")
    void destroysAllEquipmentOnTarget() {
        Permanent creature = new Permanent(new SeaMonster());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(creature);

        Permanent equip1 = new Permanent(new LeoninScimitar());
        equip1.setSummoningSick(false);
        equip1.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).add(equip1);

        Permanent equip2 = new Permanent(new LoxodonWarhammer());
        equip2.setSummoningSick(false);
        equip2.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).add(equip2);

        harness.setHand(player1, List.of(new BlastfireBolt()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Sea Monster is 6/6, survives 5 damage
        harness.assertOnBattlefield(player2, "Sea Monster");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Loxodon Warhammer");
    }

    @Test
    @DisplayName("Blastfire Bolt leaves Equipment attached to other creatures alone")
    void doesNotDestroyEquipmentOnOtherCreatures() {
        Permanent targetCreature = new Permanent(new GrizzlyBears());
        targetCreature.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(targetCreature);

        Permanent otherCreature = new Permanent(new SeaMonster());
        otherCreature.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(otherCreature);

        Permanent equipment = new Permanent(new LeoninScimitar());
        equipment.setSummoningSick(false);
        equipment.setAttachedTo(otherCreature.getId());
        gd.playerBattlefields.get(player2.getId()).add(equipment);

        harness.setHand(player1, List.of(new BlastfireBolt()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstant(player1, 0, targetCreature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Blastfire Bolt fizzles when its target leaves the battlefield")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlastfireBolt()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Blastfire Bolt");
    }

    @Test
    @DisplayName("Blastfire Bolt cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new BlastfireBolt()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
