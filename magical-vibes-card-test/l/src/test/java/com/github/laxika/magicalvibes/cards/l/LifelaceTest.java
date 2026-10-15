package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.j.Jump;
import com.github.laxika.magicalvibes.cards.m.MerfolkOfThePearlTrident;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Lifelace.class, MerfolkOfThePearlTrident.class, Island.class, Jump.class, Unsummon.class})
class LifelaceTest extends BaseCardTest {

    @Test
    @DisplayName("Target permanent becomes green, replacing its previous colors (CR 105.3)")
    void permanentBecomesGreen() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MerfolkOfThePearlTrident());
        harness.setHand(player1, List.of(new Lifelace()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("A noncreature permanent becomes green")
    void noncreaturePermanentBecomesGreen() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new Lifelace()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("The color change has no duration — it does not wear off at end of turn")
    void colorPersistsPastEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MerfolkOfThePearlTrident());
        harness.setHand(player1, List.of(new Lifelace()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);

        // End-of-turn cleanup expires until-end-of-turn floating effects; Lifelace's is permanent.
        gd.expireEndOfTurnFloatingEffects();
        target.resetModifiers();

        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("Targeting a creature spell makes the permanent it becomes green (CR 400.7a)")
    void spellTargetCarriesColorToPermanent() {
        harness.setHand(player1, List.of(new Lifelace(), new MerfolkOfThePearlTrident()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        // Merfolk of the Pearl Trident creature spell goes on the stack (index 1; Lifelace stays at index 0).
        harness.castCreature(player1, 1);
        UUID merfolkSpellId = gd.stack.getFirst().getCard().getId();

        harness.castAndResolveInstant(player1, 0, merfolkSpellId);
        harness.passBothPriorities(); // resolve the Merfolk of the Pearl Trident spell

        Permanent merfolk = findPermanent(player1, "Merfolk of the Pearl Trident");
        assertThat(gqs.getEffectiveColors(gd, merfolk)).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("A nonpermanent spell becomes green while on the stack but does not retain it after leaving")
    void nonpermanentSpellBecomesGreen() {
        Permanent targetCreature = harness.addToBattlefieldAndReturn(player1, new MerfolkOfThePearlTrident());
        harness.setHand(player1, List.of(new Lifelace(), new Jump()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 1, targetCreature.getId());
        Card targetSpell = gd.stack.getFirst().getCard();

        harness.castAndResolveInstant(player1, 0, targetSpell.getId());

        assertThat(gqs.getEffectiveCardColors(gd, targetSpell)).containsExactly(CardColor.GREEN);

        harness.passBothPriorities();

        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(targetSpell));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, targetCreature.getId());

        assertThat(gqs.getEffectiveCardColors(gd, targetSpell)).containsExactly(CardColor.BLUE);

        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A green Island still produces blue mana")
    void colorChangeDoesNotChangeLandManaProduction() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new Lifelace(), new MerfolkOfThePearlTrident()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, island.getId());
        assertThat(gqs.getEffectiveColors(gd, island)).containsExactly(CardColor.GREEN);
        harness.tapPermanent(player1, 0);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Merfolk of the Pearl Trident");
    }

    @Test
    @DisplayName("A permanent loses the color change when it leaves and is cast again")
    void colorChangeDoesNotFollowReturnedPermanent() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new MerfolkOfThePearlTrident());
        harness.setHand(player1, List.of(new Lifelace(), new Unsummon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, merfolk.getId());
        assertThat(gqs.getEffectiveColors(gd, merfolk)).containsExactly(CardColor.GREEN);
        harness.castAndResolveInstant(player1, 0, merfolk.getId());
        harness.assertInHand(player1, "Merfolk of the Pearl Trident");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent returnedMerfolk = findPermanent(player1, "Merfolk of the Pearl Trident");
        assertThat(gqs.getEffectiveColors(gd, returnedMerfolk)).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("Lifelace does not change a target that leaves before resolution")
    void departedTargetIsNotAffected() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player2, new MerfolkOfThePearlTrident());
        harness.setHand(player1, List.of(new Lifelace()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, merfolk.getId());
        harness.castAndResolveInstant(player2, 0, merfolk.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Lifelace");
        harness.assertInHand(player2, "Merfolk of the Pearl Trident");
        assertThat(gqs.getEffectiveCardColors(gd, merfolk.getCard())).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("An opposing creature spell becomes green on the stack and battlefield")
    void opposingSpellBecomesGreen() {
        harness.setHand(player1, List.of(new MerfolkOfThePearlTrident()));
        harness.setHand(player2, List.of(new Lifelace()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        Card spell = gd.stack.getFirst().getCard();
        harness.castAndResolveInstant(player2, 0, spell.getId());
        assertThat(gqs.getEffectiveCardColors(gd, spell)).containsExactly(CardColor.GREEN);
        harness.passBothPriorities();

        Permanent merfolk = findPermanent(player1, "Merfolk of the Pearl Trident");
        assertThat(gqs.getEffectiveColors(gd, merfolk)).containsExactly(CardColor.GREEN);
    }
}
