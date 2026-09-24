package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.d.DarksteelForge;
import com.github.laxika.magicalvibes.cards.q.QuicksilverBehemoth;
import com.github.laxika.magicalvibes.cards.s.Skullclamp;
import com.github.laxika.magicalvibes.cards.s.Spincrusher;
import com.github.laxika.magicalvibes.cards.s.SwordOfFireAndIce;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Unforge.class, Skullclamp.class, QuicksilverBehemoth.class, Spincrusher.class,
        CrazedGoblin.class, DarksteelForge.class, SwordOfFireAndIce.class})
class UnforgeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target Equipment and deals 2 damage to its attached creature")
    void destroysEquipmentAndDamagesAttachedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new QuicksilverBehemoth());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new Skullclamp());
        equipment.setAttachedTo(creature.getId());
        castUnforge(equipment);

        harness.assertInGraveyard(player2, "Skullclamp");
        harness.assertOnBattlefield(player2, "Quicksilver Behemoth");
        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Deals no damage when target Equipment is unattached")
    void unattachedEquipmentDoesNotDealDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new QuicksilverBehemoth());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new Skullclamp());
        castUnforge(equipment);

        harness.assertInGraveyard(player2, "Skullclamp");
        harness.assertOnBattlefield(player2, "Quicksilver Behemoth");
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Two damage destroys a 0/2 creature after its Equipment is destroyed")
    void damageCanDestroyAttachedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Spincrusher());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new Skullclamp());
        equipment.setAttachedTo(creature.getId());
        castUnforge(equipment);

        harness.assertInGraveyard(player2, "Skullclamp");
        harness.assertInGraveyard(player2, "Spincrusher");
    }

    @Test
    @DisplayName("Does not damage the creature when an indestructible Equipment survives")
    void indestructibleEquipmentSurvivesWithoutDamagingAttachedCreature() {
        harness.addToBattlefield(player2, new DarksteelForge());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new QuicksilverBehemoth());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new Skullclamp());
        equipment.setAttachedTo(creature.getId());
        castUnforge(equipment);

        harness.assertOnBattlefield(player2, "Darksteel Forge");
        harness.assertOnBattlefield(player2, "Skullclamp");
        harness.assertOnBattlefield(player2, "Quicksilver Behemoth");
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Protection from red prevents the damage dealt to the attached creature")
    void protectionFromRedPreventsDamage() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new QuicksilverBehemoth());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new Skullclamp());
        Permanent protectionEquipment = harness.addToBattlefieldAndReturn(player2, new SwordOfFireAndIce());
        equipment.setAttachedTo(creature.getId());
        protectionEquipment.setAttachedTo(creature.getId());
        castUnforge(equipment);

        harness.assertInGraveyard(player2, "Skullclamp");
        harness.assertOnBattlefield(player2, "Sword of Fire and Ice");
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target a non-Equipment permanent")
    void cannotTargetNonEquipment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CrazedGoblin());
        harness.setHand(player1, List.of(new Unforge()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Target must be an Equipment");
    }

    private void castUnforge(Permanent equipment) {
        harness.setHand(player1, List.of(new Unforge()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, equipment.getId());
    }
}
