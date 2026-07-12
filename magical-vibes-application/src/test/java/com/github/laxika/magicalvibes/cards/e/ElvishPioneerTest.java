package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AdarkarWastes;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Elvish Pioneer"));

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

        Permanent land = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .findFirst()
                .orElseThrow();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only basic land cards in hand are valid choices")
    void onlyBasicLandsAreValidChoices() {
        harness.setHand(player1, List.of(new ElvishPioneer(), new GrizzlyBears(), new Forest(), new AdarkarWastes()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt
        harness.handleMayAbilityChosen(player1, true); // accept → HandCardChoice inline

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        // After casting Elvish Pioneer, hand is [GrizzlyBears, Forest, AdarkarWastes]; only Forest (index 1) is a basic land.
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).validIndices()).containsExactly(1);
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

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));
    }
}
