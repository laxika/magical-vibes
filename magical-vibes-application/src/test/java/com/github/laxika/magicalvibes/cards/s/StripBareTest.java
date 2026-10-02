package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BlightSickle;
import com.github.laxika.magicalvibes.cards.s.ShieldOfTheOversoul;
import com.github.laxika.magicalvibes.cards.w.WiltLeafCavaliers;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StripBare.class, WiltLeafCavaliers.class, BlightSickle.class, ShieldOfTheOversoul.class})
class StripBareTest extends BaseCardTest {

    @Test
    @DisplayName("Strip Bare destroys both Auras and Equipment attached to the target creature")
    void destroysAurasAndEquipment() {
        Permanent creature = addCreatureReady(player2, new WiltLeafCavaliers());

        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new BlightSickle());
        equipment.setAttachedTo(creature.getId());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ShieldOfTheOversoul());
        aura.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new StripBare()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        // The creature itself survives.
        harness.assertOnBattlefield(player2, "Wilt-Leaf Cavaliers");
        harness.assertNotOnBattlefield(player2, "Blight Sickle");
        harness.assertInGraveyard(player2, "Blight Sickle");
        harness.assertNotOnBattlefield(player1, "Shield of the Oversoul");
        harness.assertInGraveyard(player1, "Shield of the Oversoul");
    }

    @Test
    @DisplayName("Strip Bare destroys every Aura and Equipment attached to the target creature")
    void destroysAllMatchingAttachments() {
        Permanent creature = addCreatureReady(player2, new WiltLeafCavaliers());

        Permanent firstEquipment = harness.addToBattlefieldAndReturn(player2, new BlightSickle());
        firstEquipment.setAttachedTo(creature.getId());
        Permanent secondEquipment = harness.addToBattlefieldAndReturn(player2, new BlightSickle());
        secondEquipment.setAttachedTo(creature.getId());

        Permanent firstAura = harness.addToBattlefieldAndReturn(player1, new ShieldOfTheOversoul());
        firstAura.setAttachedTo(creature.getId());
        Permanent secondAura = harness.addToBattlefieldAndReturn(player1, new ShieldOfTheOversoul());
        secondAura.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new StripBare()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertOnBattlefield(player2, "Wilt-Leaf Cavaliers");
        harness.assertNotOnBattlefield(player2, "Blight Sickle");
        harness.assertInGraveyard(player2, "Blight Sickle");
        harness.assertNotOnBattlefield(player1, "Shield of the Oversoul");
        harness.assertInGraveyard(player1, "Shield of the Oversoul");
    }

    @Test
    @DisplayName("Strip Bare does not destroy attachments on other creatures")
    void doesNotAffectOtherCreatures() {
        Permanent target = addCreatureReady(player2, new WiltLeafCavaliers());

        Permanent other = addCreatureReady(player2, new WiltLeafCavaliers());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ShieldOfTheOversoul());
        aura.setAttachedTo(other.getId());

        harness.setHand(player1, List.of(new StripBare()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player1, "Shield of the Oversoul");
    }

    @Test
    @DisplayName("Strip Bare cannot target a non-creature")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new BlightSickle());
        harness.setHand(player1, List.of(new StripBare()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        UUID targetId = harness.getPermanentId(player2, "Blight Sickle");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
