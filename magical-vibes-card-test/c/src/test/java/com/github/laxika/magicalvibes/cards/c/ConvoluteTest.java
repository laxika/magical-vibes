package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.Watchwolf;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Convolute.class, Watchwolf.class, Char.class, Forest.class})
class ConvoluteTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the spell when its controller declines to pay {4}")
    void countersWhenControllerDeclinesToPay() {
        Watchwolf watchwolf = new Watchwolf();
        harness.setHand(player1, List.of(watchwolf));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.setHand(player2, List.of(new Convolute()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, watchwolf.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Watchwolf");
        harness.assertNotOnBattlefield(player1, "Watchwolf");
    }

    @Test
    @DisplayName("Spell resolves when its controller pays {4}")
    void resolvesWhenControllerPaysFour() {
        Watchwolf watchwolf = new Watchwolf();
        harness.setHand(player1, List.of(watchwolf));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.setHand(player2, List.of(new Convolute()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, watchwolf.getId());

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Watchwolf");
    }

    @Test
    @DisplayName("Counters a noncreature spell when its controller cannot pay {4}")
    void countersNoncreatureSpellWhenControllerCannotPay() {
        Char charSpell = new Char();
        harness.setHand(player1, List.of(charSpell));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.setHand(player2, List.of(new Convolute()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, charSpell.getId());

        assertThat(harness.getGameData().interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Char");
        harness.assertInGraveyard(player2, "Convolute");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Fizzles when the target spell is no longer on the stack")
    void fizzlesIfTargetSpellRemoved() {
        Watchwolf watchwolf = new Watchwolf();
        harness.setHand(player1, List.of(watchwolf));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.setHand(player2, List.of(new Convolute()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, watchwolf.getId());

        GameData gd = harness.getGameData();
        gd.stack.removeIf(se -> se.getCard().getName().equals("Watchwolf"));

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Convolute");
    }

    @Test
    @DisplayName("Three available mana is insufficient to save the spell")
    void countersWhenControllerHasOnlyThreeMana() {
        Watchwolf watchwolf = new Watchwolf();
        harness.setHand(player1, List.of(watchwolf));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setHand(player2, List.of(new Convolute()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, watchwolf.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Watchwolf");
        harness.assertNotOnBattlefield(player1, "Watchwolf");
        harness.assertInGraveyard(player2, "Convolute");
    }

    @Test
    @DisplayName("Can counter a spell controlled by its own controller")
    void canCounterOwnSpell() {
        Watchwolf watchwolf = new Watchwolf();
        harness.setHand(player1, List.of(watchwolf, new Convolute()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.castAndResolveInstant(player1, 0, watchwolf.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Watchwolf");
        harness.assertNotOnBattlefield(player1, "Watchwolf");
        harness.assertInGraveyard(player1, "Convolute");
    }

    @Test
    @DisplayName("Can activate mana abilities during resolution to pay the four mana")
    void canTapLandsDuringResolutionToPay() {
        Watchwolf watchwolf = new Watchwolf();
        harness.setHand(player1, List.of(watchwolf));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player2, List.of(new Convolute()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, watchwolf.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.assertNotInGraveyard(player1, "Watchwolf");
        for (int i = 0; i < 4; i++) {
            gs.tapPermanent(gd, player1, i);
        }
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Watchwolf");
        harness.assertNotInGraveyard(player1, "Watchwolf");
        harness.assertInGraveyard(player2, "Convolute");
    }
}
