package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CursedLand;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MarkOfTheVampire;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

        Permanent aura = findByName(player1, "Mark of the Vampire");
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

        assertThat(findByName(player1, "Mark of the Vampire")).isNull();
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
        assertThat(findByName(player1, "Cursed Land")).isNull();
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
        assertThat(findByName(player1, "Mark of the Vampire")).isNull();
    }

    private void resolveUntilMayPrompt() {
        for (int i = 0; i < 4 && !(gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice); i++) {
            harness.passBothPriorities();
        }
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player, attackerIndices);
    }

    private Permanent findByName(Player player, String cardName) {
        for (Permanent p : gd.playerBattlefields.get(player.getId())) {
            if (p.getCard().getName().equals(cardName)) {
                return p;
            }
        }
        return null;
    }
}
