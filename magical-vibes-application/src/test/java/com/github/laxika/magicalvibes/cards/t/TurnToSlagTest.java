package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.e.EngulfingSlagwurm;
import com.github.laxika.magicalvibes.cards.s.StriderHarness;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AccordersShield.class, CopperMyr.class, EngulfingSlagwurm.class,
        StriderHarness.class, TurnToSlag.class})
class TurnToSlagTest extends BaseCardTest {

    @Test
    @DisplayName("Turn to Slag deals 5 damage and kills a small creature")
    void deals5DamageAndKillsCreature() {
        harness.addToBattlefield(player2, new CopperMyr());
        harness.setHand(player1, List.of(new TurnToSlag()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Copper Myr");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Copper Myr");
        harness.assertInGraveyard(player2, "Copper Myr");
    }

    @Test
    @DisplayName("Turn to Slag destroys equipment attached to the target creature")
    void destroysEquipmentAttachedToTarget() {
        Permanent creature = addCreatureReady(player2, new CopperMyr());

        Permanent equipment = new Permanent(new AccordersShield());
        equipment.setSummoningSick(false);
        equipment.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).add(equipment);

        harness.setHand(player1, List.of(new TurnToSlag()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Copper Myr");
        harness.assertInGraveyard(player2, "Copper Myr");
        harness.assertNotOnBattlefield(player2, "Accorder's Shield");
        harness.assertInGraveyard(player2, "Accorder's Shield");
    }

    @Test
    @DisplayName("Turn to Slag destroys multiple equipment attached to the target creature")
    void destroysMultipleEquipment() {
        Permanent creature = addCreatureReady(player2, new EngulfingSlagwurm());

        Permanent equip1 = new Permanent(new AccordersShield());
        equip1.setSummoningSick(false);
        equip1.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).add(equip1);

        Permanent equip2 = new Permanent(new StriderHarness());
        equip2.setSummoningSick(false);
        equip2.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).add(equip2);

        harness.setHand(player1, List.of(new TurnToSlag()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Engulfing Slagwurm is 7/7, survives 5 damage
        harness.assertOnBattlefield(player2, "Engulfing Slagwurm");
        harness.assertNotOnBattlefield(player2, "Accorder's Shield");
        harness.assertInGraveyard(player2, "Accorder's Shield");
        harness.assertNotOnBattlefield(player2, "Strider Harness");
        harness.assertInGraveyard(player2, "Strider Harness");
    }

    @Test
    @DisplayName("Turn to Slag does not destroy equipment attached to other creatures")
    void doesNotDestroyEquipmentOnOtherCreatures() {
        Permanent targetCreature = addCreatureReady(player2, new CopperMyr());

        Permanent otherCreature = addCreatureReady(player2, new EngulfingSlagwurm());

        Permanent equipment = new Permanent(new AccordersShield());
        equipment.setSummoningSick(false);
        equipment.setAttachedTo(otherCreature.getId());
        gd.playerBattlefields.get(player2.getId()).add(equipment);

        harness.setHand(player1, List.of(new TurnToSlag()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, targetCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Copper Myr");
        harness.assertInGraveyard(player2, "Copper Myr");
        // Equipment on other creature should be unaffected
        harness.assertOnBattlefield(player2, "Accorder's Shield");
    }

    @Test
    @DisplayName("Turn to Slag fizzles when target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new CopperMyr());
        harness.setHand(player1, List.of(new TurnToSlag()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Copper Myr");
        harness.castSorcery(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Turn to Slag");
    }

    @Test
    @DisplayName("Turn to Slag cannot target non-creatures")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new AccordersShield());
        harness.setHand(player1, List.of(new TurnToSlag()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Accorder's Shield");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Turn to Slag goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new CopperMyr());
        harness.setHand(player1, List.of(new TurnToSlag()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Copper Myr");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Turn to Slag");
    }
}
