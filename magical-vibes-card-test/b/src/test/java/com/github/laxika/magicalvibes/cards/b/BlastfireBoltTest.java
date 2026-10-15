package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AvariceAmulet;
import com.github.laxika.magicalvibes.cards.b.BrawlersPlate;
import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.d.DivineFavor;
import com.github.laxika.magicalvibes.cards.s.ShieldOfTheAvatar;
import com.github.laxika.magicalvibes.cards.s.SoulOfNewPhyrexia;
import com.github.laxika.magicalvibes.model.GameLogEntry;
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

@CardUsed({AvariceAmulet.class, BlastfireBolt.class, BrawlersPlate.class, BronzeSable.class,
        DivineFavor.class, ShieldOfTheAvatar.class, SoulOfNewPhyrexia.class})
class BlastfireBoltTest extends BaseCardTest {

    @Test
    @DisplayName("Blastfire Bolt deals 5 damage and kills a small creature")
    void deals5DamageAndKillsCreature() {
        harness.addToBattlefield(player2, new BronzeSable());
        harness.setHand(player1, List.of(new BlastfireBolt()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID targetId = harness.getPermanentId(player2, "Bronze Sable");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Bronze Sable");
        harness.assertInGraveyard(player2, "Bronze Sable");
        harness.assertInGraveyard(player1, "Blastfire Bolt");
    }

    @Test
    @DisplayName("Blastfire Bolt destroys all Equipment attached to the target creature")
    void destroysAllEquipmentOnTarget() {
        Permanent creature = addCreatureReady(player2, new SoulOfNewPhyrexia());

        Permanent equip1 = harness.addToBattlefieldAndReturn(player2, new AvariceAmulet());
        equip1.setSummoningSick(false);
        equip1.setAttachedTo(creature.getId());

        Permanent equip2 = harness.addToBattlefieldAndReturn(player2, new BrawlersPlate());
        equip2.setSummoningSick(false);
        equip2.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new BlastfireBolt()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Soul of New Phyrexia is 6/6, survives 5 damage
        harness.assertOnBattlefield(player2, "Soul of New Phyrexia");
        harness.assertInGraveyard(player2, "Avarice Amulet");
        harness.assertInGraveyard(player2, "Brawler's Plate");
    }

    @Test
    @DisplayName("Blastfire Bolt leaves Equipment attached to other creatures alone")
    void doesNotDestroyEquipmentOnOtherCreatures() {
        Permanent targetCreature = addCreatureReady(player2, new BronzeSable());

        Permanent otherCreature = addCreatureReady(player2, new SoulOfNewPhyrexia());

        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new AvariceAmulet());
        equipment.setSummoningSick(false);
        equipment.setAttachedTo(otherCreature.getId());

        harness.setHand(player1, List.of(new BlastfireBolt()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstant(player1, 0, targetCreature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Bronze Sable");
        harness.assertOnBattlefield(player2, "Avarice Amulet");
    }

    @Test
    @DisplayName("Blastfire Bolt fizzles when its target leaves the battlefield")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new BronzeSable());
        harness.setHand(player1, List.of(new BlastfireBolt()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID targetId = harness.getPermanentId(player2, "Bronze Sable");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Blastfire Bolt");
    }

    @Test
    @DisplayName("Blastfire Bolt cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new AvariceAmulet());
        harness.setHand(player1, List.of(new BlastfireBolt()));
        harness.addMana(player1, ManaColor.RED, 6);

        UUID targetId = harness.getPermanentId(player2, "Avarice Amulet");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Attached Equipment prevents damage before Blastfire Bolt destroys it")
    void appliesEquipmentPreventionBeforeDestroyingEquipment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SoulOfNewPhyrexia());
        Permanent shield = harness.addToBattlefieldAndReturn(player2, new ShieldOfTheAvatar());
        shield.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new BlastfireBolt()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Soul of New Phyrexia");
        assertThat(creature.getMarkedDamage()).isEqualTo(4);
        harness.assertInGraveyard(player2, "Shield of the Avatar");
    }

    @Test
    @DisplayName("Blastfire Bolt destroys attached Equipment even when the damage is lethal")
    void destroysEquipmentControlledByOpponentOfTargetController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BronzeSable());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new BrawlersPlate());
        equipment.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new BlastfireBolt()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Bronze Sable");
        harness.assertInGraveyard(player1, "Brawler's Plate");
    }

    @Test
    @DisplayName("Blastfire Bolt does not destroy an Aura attached to a surviving creature")
    void leavesAttachedAuraAlone() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SoulOfNewPhyrexia());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new DivineFavor());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new BlastfireBolt()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Soul of New Phyrexia");
        assertThat(creature.getMarkedDamage()).isEqualTo(5);
        harness.assertOnBattlefield(player2, "Divine Favor");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Indestructible Equipment survives Blastfire Bolt")
    void cannotDestroyIndestructibleEquipment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SoulOfNewPhyrexia());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new BrawlersPlate());
        equipment.setAttachedTo(creature.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 5);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new BlastfireBolt()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Soul of New Phyrexia");
        assertThat(creature.getMarkedDamage()).isEqualTo(5);
        harness.assertOnBattlefield(player2, "Brawler's Plate");
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
    }
}
