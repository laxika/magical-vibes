package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.ArmorOfFaith;
import com.github.laxika.magicalvibes.cards.e.EssenceFlare;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WordOfUndoingTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target creature to owner's hand")
    void returnsTargetCreatureToHand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WordOfUndoing()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Also returns white Auras you own attached to the target")
    void returnsOwnedWhiteAurasAttached() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent whiteAura = harness.addToBattlefieldAndReturn(player1, new ArmorOfFaith());
        whiteAura.setAttachedTo(bears.getId());

        harness.setHand(player1, List.of(new WordOfUndoing()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Armor of Faith");
        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Armor of Faith");
        harness.assertNotInGraveyard(player1, "Armor of Faith");
    }

    @Test
    @DisplayName("Returns all owned white Auras attached to the target")
    void returnsAllOwnedWhiteAurasAttached() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new ArmorOfFaith());
        Permanent strength = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        armor.setAttachedTo(bears.getId());
        strength.setAttachedTo(bears.getId());

        harness.setHand(player1, List.of(new WordOfUndoing()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInHand(player1, "Armor of Faith");
        harness.assertInHand(player1, "Holy Strength");
        harness.assertNotInGraveyard(player1, "Armor of Faith");
        harness.assertNotInGraveyard(player1, "Holy Strength");
    }

    @Test
    @DisplayName("Does not return non-white Auras you own")
    void doesNotReturnOwnedNonWhiteAuras() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent blueAura = harness.addToBattlefieldAndReturn(player1, new EssenceFlare());
        blueAura.setAttachedTo(bears.getId());

        harness.setHand(player1, List.of(new WordOfUndoing()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        // Non-white owned Aura dies as an orphan when the creature leaves
        harness.assertNotInHand(player1, "Essence Flare");
        harness.assertInGraveyard(player1, "Essence Flare");
    }

    @Test
    @DisplayName("Does not return opponent's white Aura attached to the target")
    void doesNotReturnOpponentsWhiteAura() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentAura = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        opponentAura.setAttachedTo(bears.getId());

        harness.setHand(player1, List.of(new WordOfUndoing()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId()))
                .doesNotContain(opponentAura.getOriginalCard());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(opponentAura.getOriginalCard());
    }

    @Test
    @DisplayName("Cannot target a non-creature")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Pacifism());
        harness.setHand(player1, List.of(new WordOfUndoing()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Pacifism");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
