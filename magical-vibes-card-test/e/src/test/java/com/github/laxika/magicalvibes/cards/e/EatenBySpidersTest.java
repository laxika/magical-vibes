package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.v.Vorstclaw;
import com.github.laxika.magicalvibes.cards.b.BladedBracers;
import com.github.laxika.magicalvibes.cards.t.TormentorsTrident;
import com.github.laxika.magicalvibes.cards.s.SeraphOfDawn;
import com.github.laxika.magicalvibes.cards.a.AngelicArmaments;
import com.github.laxika.magicalvibes.cards.s.Skullclamp;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EatenBySpiders.class, SeraphOfDawn.class, Vorstclaw.class, BladedBracers.class,
        TormentorsTrident.class, AngelicArmaments.class, Skullclamp.class})
class EatenBySpidersTest extends BaseCardTest {

    @Test
    @DisplayName("Eaten by Spiders destroys a creature with flying")
    void destroysFlyingCreature() {
        harness.addToBattlefield(player2, new SeraphOfDawn());
        harness.setHand(player1, List.of(new EatenBySpiders()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player2, "Seraph of Dawn");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Seraph of Dawn");
        harness.assertInGraveyard(player2, "Seraph of Dawn");
    }

    @Test
    @DisplayName("Eaten by Spiders also destroys Equipment attached to the target")
    void destroysAttachedEquipment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SeraphOfDawn());

        Permanent equip1 = harness.addToBattlefieldAndReturn(player2, new BladedBracers());
        equip1.setAttachedTo(creature.getId());

        Permanent equip2 = harness.addToBattlefieldAndReturn(player2, new TormentorsTrident());
        equip2.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new EatenBySpiders()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertInGraveyard(player2, "Seraph of Dawn");
        harness.assertInGraveyard(player2, "Bladed Bracers");
        harness.assertInGraveyard(player2, "Tormentor's Trident");
    }

    @Test
    @DisplayName("Eaten by Spiders leaves Equipment attached to other creatures alone")
    void leavesOtherEquipmentAlone() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SeraphOfDawn());

        Permanent other = harness.addToBattlefieldAndReturn(player2, new Vorstclaw());

        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new BladedBracers());
        equipment.setAttachedTo(other.getId());

        harness.setHand(player1, List.of(new EatenBySpiders()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Seraph of Dawn");
        harness.assertOnBattlefield(player2, "Bladed Bracers");
        harness.assertOnBattlefield(player2, "Vorstclaw");
    }

    @Test
    @DisplayName("Eaten by Spiders cannot target a creature without flying")
    void cannotTargetNonFlyingCreature() {
        harness.addToBattlefield(player2, new Vorstclaw());
        harness.setHand(player1, List.of(new EatenBySpiders()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        UUID targetId = harness.getPermanentId(player2, "Vorstclaw");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature with flying granted by Equipment is destroyed along with that Equipment")
    void destroysCreatureWithEquipmentGrantedFlying() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Vorstclaw());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new AngelicArmaments());
        equipment.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new EatenBySpiders()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertInGraveyard(player2, "Vorstclaw");
        harness.assertInGraveyard(player2, "Angelic Armaments");
    }

    @Test
    @DisplayName("Skullclamp triggers when it and the equipped creature are destroyed together")
    void equipmentSeesEquippedCreatureDieSimultaneously() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SeraphOfDawn());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new Skullclamp());
        equipment.setAttachedTo(creature.getId());
        Vorstclaw firstDraw = new Vorstclaw();
        Vorstclaw secondDraw = new Vorstclaw();
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(firstDraw, secondDraw));
        harness.setHand(player1, List.of(new EatenBySpiders()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Seraph of Dawn");
        harness.assertInGraveyard(player2, "Skullclamp");
        assertThat(gd.playerHands.get(player2.getId())).containsExactlyInAnyOrder(firstDraw, secondDraw);
    }

    @Test
    @DisplayName("The spell does nothing if the target loses flying before resolution")
    void doesNotDestroyEquipmentWhenTargetLosesFlying() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Vorstclaw());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new AngelicArmaments());
        equipment.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new EatenBySpiders()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0, creature.getId());

        equipment.setAttachedTo(null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Vorstclaw");
        harness.assertOnBattlefield(player2, "Angelic Armaments");
        harness.assertInGraveyard(player1, "Eaten by Spiders");
    }

    @Test
    @DisplayName("Equipment is destroyed regardless of its controller, while unattached Equipment survives")
    void destroysEquipmentControlledByAnotherPlayer() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SeraphOfDawn());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new BladedBracers());
        equipment.setAttachedTo(creature.getId());
        harness.addToBattlefield(player1, new TormentorsTrident());
        harness.setHand(player1, List.of(new EatenBySpiders()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertInGraveyard(player2, "Seraph of Dawn");
        harness.assertInGraveyard(player1, "Bladed Bracers");
        harness.assertOnBattlefield(player1, "Tormentor's Trident");
    }
}
