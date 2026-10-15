package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CursedLand;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MarkOfTheVampire;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SovereignsOfLostAlara.class, CursedLand.class, GrizzlyBears.class,
        MarkOfTheVampire.class, WhiteKnight.class})
class SovereignsOfLostAlaraTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking alone: exalted boosts the attacker and it may fetch an Aura attached to it")
    void attacksAloneBoostsAndFetchesAura() {
        harness.addToBattlefield(player1, new SovereignsOfLostAlara());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new MarkOfTheVampire()));

        declareAttackers(player1, List.of(1));

        resolveUntilMayPrompt();
        harness.handleMayAbilityChosen(player1, true);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        Permanent aura = findPermanent(player1, "Mark of the Vampire");
        assertThat(aura).isNotNull();
        assertThat(aura.getAttachedTo()).isEqualTo(attacker.getId());
        // Grizzly Bears 2/2 + exalted +1/+1 + Mark of the Vampire +2/+2 = 5/5.
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(5);
    }

    @Test
    @DisplayName("Declining the search still applies the exalted boost and attaches no Aura")
    void decliningLeavesNoAura() {
        harness.addToBattlefield(player1, new SovereignsOfLostAlara());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new MarkOfTheVampire()));

        declareAttackers(player1, List.of(1));

        resolveUntilMayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Mark of the Vampire")).isEmpty();
        // Exalted +1/+1 still applies to the lone attacker.
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Only Auras that could enchant the attacker are found (an Enchant-land Aura is not)")
    void ineligibleAuraNotFound() {
        harness.addToBattlefield(player1, new SovereignsOfLostAlara());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new CursedLand()));

        declareAttackers(player1, List.of(1));

        resolveUntilMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        // No Aura in the library could enchant the creature, so nothing is put onto the battlefield
        // and no library-search choice is presented.
        assertThat(findPermanents(player1, "Cursed Land")).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Attacking with more than one creature: no exalted boost and no search trigger")
    void noTriggerWhenNotAlone() {
        harness.addToBattlefield(player1, new SovereignsOfLostAlara());
        Permanent one = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new MarkOfTheVampire()));

        declareAttackers(player1, List.of(1, 2)); // two attackers — not alone

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gqs.getEffectivePower(gd, one)).isEqualTo(2);
        assertThat(findPermanents(player1, "Mark of the Vampire")).isEmpty();
    }

    @Test
    @DisplayName("Protection from black excludes black Auras from the search")
    void protectionRestrictsAuraSearch() {
        harness.addToBattlefield(player1, new SovereignsOfLostAlara());
        Permanent attacker = addCreatureReady(player1, new WhiteKnight());
        MarkOfTheVampire aura = new MarkOfTheVampire();
        harness.setLibrary(player1, List.of(aura));

        declareAttackers(player1, List.of(1));
        resolveUntilMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(aura);
        assertThat(findPermanents(player1, "Mark of the Vampire")).isEmpty();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Sovereigns can fetch an Aura for itself when it attacks alone")
    void canEnchantItself() {
        Permanent attacker = addCreatureReady(player1, new SovereignsOfLostAlara());
        harness.setLibrary(player1, List.of(new MarkOfTheVampire()));

        declareAttackers(player1, List.of(0));
        resolveUntilMayPrompt();
        harness.handleMayAbilityChosen(player1, true);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(findPermanent(player1, "Mark of the Vampire").getAttachedTo()).isEqualTo(attacker.getId());
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(8);
    }

    @Test
    @DisplayName("The controller may fail to find even with an eligible Aura in the library")
    void mayFailToFindEligibleAura() {
        harness.addToBattlefield(player1, new SovereignsOfLostAlara());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        MarkOfTheVampire aura = new MarkOfTheVampire();
        harness.setLibrary(player1, List.of(aura));

        declareAttackers(player1, List.of(1));
        resolveUntilMayPrompt();
        harness.handleMayAbilityChosen(player1, true);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(aura);
        assertThat(findPermanents(player1, "Mark of the Vampire")).isEmpty();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's lone attacker does not trigger either ability")
    void opponentAttackingAloneDoesNotTrigger() {
        harness.addToBattlefield(player1, new SovereignsOfLostAlara());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new MarkOfTheVampire()));

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(findPermanents(player1, "Mark of the Vampire")).isEmpty();
    }

    private void resolveUntilMayPrompt() {
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
