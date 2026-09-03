package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.ArmorOfFaith;
import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.e.EssenceFlare;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
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

@CardUsed({WordOfUndoing.class, ArmorOfFaith.class, BalduvianBears.class, EssenceFlare.class,
        IcyManipulator.class, WhiteScarab.class})
class WordOfUndoingTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target creature to owner's hand")
    void returnsTargetCreatureToHand() {
        harness.addToBattlefield(player2, new BalduvianBears());
        harness.setHand(player1, List.of(new WordOfUndoing()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Balduvian Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Balduvian Bears");
        harness.assertInHand(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("Also returns white Auras you own attached to the target")
    void returnsOwnedWhiteAurasAttached() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        Permanent whiteAura = harness.addToBattlefieldAndReturn(player1, new ArmorOfFaith());
        whiteAura.setAttachedTo(bears.getId());

        harness.setHand(player1, List.of(new WordOfUndoing()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Balduvian Bears");
        harness.assertNotOnBattlefield(player1, "Armor of Faith");
        harness.assertInHand(player2, "Balduvian Bears");
        harness.assertInHand(player1, "Armor of Faith");
        harness.assertNotInGraveyard(player1, "Armor of Faith");
    }

    @Test
    @DisplayName("Returns all owned white Auras attached to the target")
    void returnsAllOwnedWhiteAurasAttached() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new ArmorOfFaith());
        Permanent whiteScarab = harness.addToBattlefieldAndReturn(player1, new WhiteScarab());
        armor.setAttachedTo(bears.getId());
        whiteScarab.setAttachedTo(bears.getId());

        harness.setHand(player1, List.of(new WordOfUndoing()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Balduvian Bears");
        harness.assertInHand(player1, "Armor of Faith");
        harness.assertInHand(player1, "White Scarab");
        harness.assertNotInGraveyard(player1, "Armor of Faith");
        harness.assertNotInGraveyard(player1, "White Scarab");
    }

    @Test
    @DisplayName("Does not return non-white Auras you own")
    void doesNotReturnOwnedNonWhiteAuras() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        Permanent blueAura = harness.addToBattlefieldAndReturn(player1, new EssenceFlare());
        blueAura.setAttachedTo(bears.getId());

        harness.setHand(player1, List.of(new WordOfUndoing()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Balduvian Bears");
        // Non-white owned Aura dies as an orphan when the creature leaves
        harness.assertNotInHand(player1, "Essence Flare");
        harness.assertInGraveyard(player1, "Essence Flare");
    }

    @Test
    @DisplayName("Does not return opponent's white Aura attached to the target")
    void doesNotReturnOpponentsWhiteAura() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new BalduvianBears());
        Permanent opponentAura = harness.addToBattlefieldAndReturn(player2, new WhiteScarab());
        opponentAura.setAttachedTo(bears.getId());

        harness.setHand(player1, List.of(new WordOfUndoing()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Balduvian Bears");
        assertThat(gd.playerHands.get(player2.getId()))
                .doesNotContain(opponentAura.getOriginalCard());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(opponentAura.getOriginalCard());
    }

    @Test
    @DisplayName("Returns a white Aura you own even if an opponent controls it")
    void returnsOwnedWhiteAuraControlledByOpponent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new BalduvianBears());
        Permanent aura = new Permanent(new WhiteScarab());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player2.getId()).add(aura);
        gd.stolenCreatures.put(aura.getId(), player1.getId());

        harness.setHand(player1, List.of(new WordOfUndoing()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "White Scarab");
        harness.assertNotInGraveyard(player1, "White Scarab");
        harness.assertNotOnBattlefield(player2, "Balduvian Bears");
        harness.assertInHand(player2, "Balduvian Bears");
    }

    @Test
    @DisplayName("Cannot target a non-creature")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new BalduvianBears());
        harness.addToBattlefield(player2, new IcyManipulator());
        harness.setHand(player1, List.of(new WordOfUndoing()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID targetId = harness.getPermanentId(player2, "Icy Manipulator");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
