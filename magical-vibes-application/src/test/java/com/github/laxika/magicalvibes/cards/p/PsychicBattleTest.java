package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BenalishTrapper;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RazorfootGriffin;
import com.github.laxika.magicalvibes.cards.r.Repulse;
import com.github.laxika.magicalvibes.cards.s.SwayOfIllusion;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PsychicBattle.class, Repulse.class, RazorfootGriffin.class, Island.class,
        SwayOfIllusion.class, BenalishTrapper.class})
class PsychicBattleTest extends BaseCardTest {

    @Test
    void uniqueHighestManaValuePlayerMayChangeTheTarget() {
        harness.addToBattlefield(player1, new PsychicBattle());
        var originalTarget = harness.addToBattlefieldAndReturn(player2, new RazorfootGriffin());
        var alternateTarget = harness.addToBattlefieldAndReturn(player1, new RazorfootGriffin());
        harness.setLibrary(player1, List.of(new SwayOfIllusion()));
        harness.setLibrary(player2, List.of(new Island()));
        harness.setHand(player2, List.of(new Repulse()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player2, 0, originalTarget.getId());
        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Psychic Battle"));

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, alternateTarget.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Psychic Battle"));

        StackEntry spell = gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals("Repulse"))
                .findFirst()
                .orElseThrow();
        assertThat(spell.getTargetId()).isEqualTo(alternateTarget.getId());
    }

    @Test
    void tiedHighestManaValuesLeaveTargetsUnchanged() {
        harness.addToBattlefield(player1, new PsychicBattle());
        var originalTarget = harness.addToBattlefieldAndReturn(player2, new RazorfootGriffin());
        harness.addToBattlefield(player1, new RazorfootGriffin());
        harness.setLibrary(player1, List.of(new Island()));
        harness.setLibrary(player2, List.of(new Island()));
        harness.setHand(player2, List.of(new Repulse()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player2, 0, originalTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        StackEntry spell = gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals("Repulse"))
                .findFirst()
                .orElseThrow();
        assertThat(spell.getTargetId()).isEqualTo(originalTarget.getId());
    }

    @Test
    void uniqueHighestManaValuePlayerMayDeclineToChangeTheTarget() {
        harness.addToBattlefield(player1, new PsychicBattle());
        var originalTarget = harness.addToBattlefieldAndReturn(player2, new RazorfootGriffin());
        harness.addToBattlefield(player1, new RazorfootGriffin());
        harness.setLibrary(player1, List.of(new SwayOfIllusion()));
        harness.setLibrary(player2, List.of(new Island()));
        harness.setHand(player2, List.of(new Repulse()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player2, 0, originalTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        StackEntry spell = gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals("Repulse"))
                .findFirst()
                .orElseThrow();
        assertThat(spell.getTargetId()).isEqualTo(originalTarget.getId());
    }

    @Test
    void emptyLibraryDoesNotParticipateInManaValueComparison() {
        harness.addToBattlefield(player1, new PsychicBattle());
        var originalTarget = harness.addToBattlefieldAndReturn(player1, new RazorfootGriffin());
        var alternateTarget = harness.addToBattlefieldAndReturn(player2, new RazorfootGriffin());
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(new Island()));
        harness.setHand(player2, List.of(new Repulse()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player2, 0, originalTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player2, alternateTarget.getId());

        StackEntry spell = gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals("Repulse"))
                .findFirst()
                .orElseThrow();
        assertThat(spell.getTargetId()).isEqualTo(alternateTarget.getId());
    }

    @Test
    void playerMayChangeSomeTargetsOfAMultiTargetSpell() {
        harness.addToBattlefield(player1, new PsychicBattle());
        var originalTargetOne = harness.addToBattlefieldAndReturn(player2, new RazorfootGriffin());
        var originalTargetTwo = harness.addToBattlefieldAndReturn(player2, new RazorfootGriffin());
        var alternateTargetOne = harness.addToBattlefieldAndReturn(player1, new RazorfootGriffin());
        harness.addToBattlefieldAndReturn(player1, new RazorfootGriffin());
        harness.setLibrary(player1, List.of(new PsychicBattle()));
        harness.setLibrary(player2, List.of(new Island()));
        harness.setHand(player2, List.of(new SwayOfIllusion()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player2, 0, List.of(originalTargetOne.getId(), originalTargetTwo.getId()));
        StackEntry spell = gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals("Sway of Illusion"))
                .findFirst()
                .orElseThrow();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, alternateTargetOne.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(spell.getDeclaredTargetIds()).containsExactly(alternateTargetOne.getId(), originalTargetTwo.getId());
    }

    @Test
    void activatedAbilityRetargetingOffersOnlyTargetsLegalForTheAbility() {
        harness.addToBattlefield(player1, new PsychicBattle());
        var originalTarget = harness.addToBattlefieldAndReturn(player1, new RazorfootGriffin());
        var alternateTarget = harness.addToBattlefieldAndReturn(player2, new RazorfootGriffin());
        var land = harness.addToBattlefieldAndReturn(player1, new Island());
        var trapper = addCreatureReady(player2, new BenalishTrapper());
        harness.setLibrary(player1, List.of(new SwayOfIllusion()));
        harness.setLibrary(player2, List.of(new Island()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.activateAbility(player2, 1, null, originalTarget.getId());
        StackEntry ability = gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals("Benalish Trapper"))
                .findFirst()
                .orElseThrow();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(alternateTarget.getId(), trapper.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(land.getId());
        assertThat(choice.validPlayerIds()).isEmpty();

        harness.handlePermanentChosen(player1, alternateTarget.getId());
        assertThat(ability.getTargetId()).isEqualTo(alternateTarget.getId());
    }
}
