package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BarrenMoor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElvishPioneer.class, Forest.class, ElvishWarrior.class, BarrenMoor.class})
class ElvishPioneerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Elvish Pioneer puts it on the stack")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new ElvishPioneer()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Elvish Pioneer");
    }

    @Test
    @DisplayName("Resolving Elvish Pioneer enters battlefield and presents may prompt")
    void resolvingPresentsMayPrompt() {
        harness.setHand(player1, List.of(new ElvishPioneer(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.assertOnBattlefield(player1, "Elvish Pioneer");

        harness.passBothPriorities(); // resolve MayEffect → may prompt
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may prompt and choosing a basic land puts it onto the battlefield tapped")
    void choosingBasicLandPutsItTapped() {
        harness.setHand(player1, List.of(new ElvishPioneer(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // accept → HandCardChoice inline
        harness.handleCardChosen(player1, 0);

        Permanent land = findPermanent(player1, "Forest");
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only basic land cards in hand are valid choices")
    void onlyBasicLandsAreValidChoices() {
        harness.setHand(player1, List.of(new ElvishPioneer(), new ElvishWarrior(), new Forest(), new BarrenMoor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // accept → HandCardChoice inline

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        // After casting Elvish Pioneer, hand is [ElvishWarrior, Forest, BarrenMoor]; only Forest (index 1) is a basic land.
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices()).containsExactly(1);
    }

    @Test
    @DisplayName("Accepting with no basic land in hand does nothing")
    void acceptingWithoutBasicLandDoesNothing() {
        harness.setHand(player1, List.of(new ElvishPioneer(), new ElvishWarrior(), new BarrenMoor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature spell -> may on stack
        harness.passBothPriorities(); // resolve MayEffect -> may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Elvish Warrior");
        harness.assertInHand(player1, "Barren Moor");
        harness.assertNotOnBattlefield(player1, "Barren Moor");
    }

    @Test
    @DisplayName("Declining the may prompt leaves the hand unchanged")
    void decliningLeavesHandUnchanged() {
        harness.setHand(player1, List.of(new ElvishPioneer(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Forest");
    }
}
