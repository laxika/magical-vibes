package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.b.BrineElemental;
import com.github.laxika.magicalvibes.cards.o.OpalGuardian;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PentarchPaladin.class, AshcoatBear.class, BrineElemental.class, OpalGuardian.class})
class PentarchPaladinTest extends BaseCardTest {

    @Test
    @DisplayName("Pentarch Paladin asks for a color as it enters")
    void choosesColorAsItEnters() {
        harness.setHand(player1, List.of(new PentarchPaladin()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "GREEN");

        assertThat(findPermanent(player1, "Pentarch Paladin").getChosenColor()).isEqualTo(CardColor.GREEN);
    }

    @Test
    @DisplayName("Flanking weakens a blocker without flanking")
    void flankingWeakensNonFlankingBlocker() {
        addCreatureReady(player1, new PentarchPaladin());
        Permanent blocker = addCreatureReady(player2, new AshcoatBear());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getEffectivePower()).isEqualTo(1);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Pentarch Paladin destroys a permanent of the chosen color")
    void destroysPermanentOfChosenColor() {
        Permanent paladin = addReadyPaladin(CardColor.GREEN);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, bear.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ashcoat Bear");
        harness.assertInGraveyard(player2, "Ashcoat Bear");
        assertThat(paladin.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Pentarch Paladin can destroy a noncreature permanent of the chosen color")
    void destroysNoncreaturePermanentOfChosenColor() {
        Permanent paladin = addReadyPaladin(CardColor.WHITE);
        Permanent guardian = harness.addToBattlefieldAndReturn(player2, new OpalGuardian());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, guardian.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Opal Guardian");
        harness.assertInGraveyard(player2, "Opal Guardian");
        assertThat(paladin.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Pentarch Paladin cannot target a permanent of another color")
    void rejectsPermanentOfAnotherColor() {
        Permanent paladin = addReadyPaladin(CardColor.GREEN);
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new BrineElemental());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, elemental.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(paladin.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Flanking does not weaken a blocker with flanking")
    void flankingDoesNotWeakenFlankingBlocker() {
        addCreatureReady(player1, new PentarchPaladin());
        Permanent blocker = addCreatureReady(player2, new PentarchPaladin());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).isEmpty();
        assertThat(blocker.getEffectivePower()).isEqualTo(3);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Pentarch Paladin can destroy itself when white was chosen")
    void canDestroyItself() {
        Permanent paladin = addReadyPaladin(CardColor.WHITE);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, paladin.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pentarch Paladin");
        harness.assertInGraveyard(player1, "Pentarch Paladin");
    }

    @Test
    @DisplayName("The chosen color remains available after the ability's source is destroyed")
    void abilityResolvesAfterSourceIsDestroyed() {
        Permanent paladin = addReadyPaladin(CardColor.GREEN);
        Permanent opposingPaladin = addCreatureReady(player2, new PentarchPaladin());
        opposingPaladin.setChosenColor(CardColor.WHITE);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, bear.getId());
        harness.activateAbility(player2, 0, 0, null, paladin.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Pentarch Paladin");
        harness.assertInGraveyard(player1, "Pentarch Paladin");
        harness.assertOnBattlefield(player2, "Ashcoat Bear");

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ashcoat Bear");
        harness.assertInGraveyard(player2, "Ashcoat Bear");
    }

    @Test
    @DisplayName("Pentarch Paladin cannot pay its ability cost with one white and one colorless mana")
    void requiresTwoWhiteMana() {
        Permanent paladin = addReadyPaladin(CardColor.GREEN);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bear.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(paladin.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Ashcoat Bear");
    }

    private Permanent addReadyPaladin(CardColor chosenColor) {
        Permanent paladin = addCreatureReady(player1, new PentarchPaladin());
        paladin.setChosenColor(chosenColor);
        return paladin;
    }
}
