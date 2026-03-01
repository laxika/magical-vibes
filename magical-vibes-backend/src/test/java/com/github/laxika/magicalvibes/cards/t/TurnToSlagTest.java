package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;
import com.github.laxika.magicalvibes.cards.s.SeaMonster;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEquipmentAttachedToTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TurnToSlagTest extends BaseCardTest {

    @Test
    @DisplayName("Turn to Slag has correct effects")
    void hasCorrectEffects() {
        TurnToSlag card = new TurnToSlag();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(DestroyEquipmentAttachedToTargetCreatureEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(DealDamageToTargetCreatureEffect.class);
        DealDamageToTargetCreatureEffect damageEffect = (DealDamageToTargetCreatureEffect) card.getEffects(EffectSlot.SPELL).get(1);
        assertThat(damageEffect.damage()).isEqualTo(5);
    }

    @Test
    @DisplayName("Turn to Slag deals 5 damage and kills a small creature")
    void deals5DamageAndKillsCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TurnToSlag()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Turn to Slag destroys equipment attached to the target creature")
    void destroysEquipmentAttachedToTarget() {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(creature);

        Permanent equipment = new Permanent(new LeoninScimitar());
        equipment.setSummoningSick(false);
        equipment.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).add(equipment);

        harness.setHand(player1, List.of(new TurnToSlag()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Turn to Slag destroys multiple equipment attached to the target creature")
    void destroysMultipleEquipment() {
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

        harness.setHand(player1, List.of(new TurnToSlag()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Sea Monster is 6/6, survives 5 damage
        harness.assertOnBattlefield(player2, "Sea Monster");
        harness.assertNotOnBattlefield(player2, "Leonin Scimitar");
        harness.assertInGraveyard(player2, "Leonin Scimitar");
        harness.assertNotOnBattlefield(player2, "Loxodon Warhammer");
        harness.assertInGraveyard(player2, "Loxodon Warhammer");
    }

    @Test
    @DisplayName("Turn to Slag does not destroy equipment attached to other creatures")
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

        harness.setHand(player1, List.of(new TurnToSlag()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, targetCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        // Equipment on other creature should be unaffected
        harness.assertOnBattlefield(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Turn to Slag fizzles when target is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TurnToSlag()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Turn to Slag");
    }

    @Test
    @DisplayName("Turn to Slag cannot target non-creatures")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.setHand(player1, List.of(new TurnToSlag()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Leonin Scimitar");
        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Turn to Slag goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TurnToSlag()));
        harness.addMana(player1, ManaColor.RED, 5);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Turn to Slag");
    }
}
