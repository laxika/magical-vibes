package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ExcavationTechnique.class, GrizzlyBears.class, Plains.class, Cancel.class, Unsummon.class})
class ExcavationTechniqueTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a nonland permanent and creates two Treasures for its controller")
    void destroysNonlandPermanentAndCreatesTwoTreasuresForItsController() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolveDemonstrate(target, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
        assertThat(findPermanents(player2, "Treasure")).hasSize(2);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new ExcavationTechnique()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Can destroy your own permanent and give you the Treasures")
    void canDestroyOwnPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castAndResolveDemonstrate(target, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Demonstrate goes on the stack as a trigger when the spell is cast, without asking yet")
    void demonstrateTriggersOnCastWithoutAsking() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ExcavationTechnique()));
        addMana();

        harness.castSorcery(player1, 0, target.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Excavation Technique");
    }

    @Test
    @DisplayName("The decision to demonstrate is made when the trigger resolves")
    void demonstrateDecisionWaitsForTriggerResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ExcavationTechnique()));
        addMana();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .extracting(PendingInteraction.MayAbilityChoice::playerId)
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Declining demonstrate creates no copies")
    void decliningCreatesNoCopies() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolveDemonstrate(target, false);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack).noneMatch(StackEntry::isCopy);
    }

    @Test
    @DisplayName("Demonstrating copies the spell for the caster and for the opponent")
    void demonstrateCopiesForCasterAndOpponent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolveDemonstrate(target, true);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).hasSize(3);
        List<StackEntry> copies = gd.stack.stream().filter(StackEntry::isCopy).toList();
        assertThat(copies).extracting(StackEntry::getControllerId)
                .containsExactly(player1.getId(), player2.getId());
        assertThat(copies).extracting(StackEntry::getTargetId)
                .containsOnly(target.getId());
    }

    @Test
    @DisplayName("Caster chooses new targets for their copy before the opponent does")
    void casterThenOpponentMayChooseNewTargets() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent casterBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentOther = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolveDemonstrate(target, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .extracting(PendingInteraction.MayAbilityChoice::playerId)
                .isEqualTo(player1.getId());
        assertThat(gd.stack).filteredOn(StackEntry::isCopy)
                .extracting(StackEntry::getControllerId)
                .as("The opponent's copy is created only after the caster's copy has its targets")
                .containsExactly(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, opponentOther.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .extracting(PendingInteraction.MayAbilityChoice::playerId)
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, casterBears.getId());

        assertThat(copyControlledBy(player1).getTargetId()).isEqualTo(opponentOther.getId());
        assertThat(copyControlledBy(player2).getTargetId()).isEqualTo(casterBears.getId());

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target, opponentOther);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(casterBears);
        assertThat(findPermanents(player2, "Treasure")).hasSize(4);
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    @DisplayName("Copies keep the original target when new targets are declined")
    void copiesKeepTargetWhenRetargetDeclined() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castAndResolveDemonstrate(target, true);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(copyControlledBy(player1).getTargetId()).isEqualTo(target.getId());
        assertThat(copyControlledBy(player2).getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Demonstrate still copies a spell countered before the trigger resolves")
    void demonstrateCopiesCounteredOriginal() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        ExcavationTechnique spell = new ExcavationTechnique();
        harness.setHand(player1, List.of(spell));
        harness.setHand(player2, List.of(new Cancel()));
        addMana();
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spell.getId());
        harness.assertInGraveyard(player1, "Excavation Technique");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
    }

    @Test
    @DisplayName("Does not create Treasures when the only target leaves the battlefield")
    void targetLeavingBattlefieldPreventsTreasures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ExcavationTechnique()));
        harness.setHand(player2, List.of(new Unsummon()));
        addMana();
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        resolveAllTriggers();

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        harness.assertInGraveyard(player1, "Excavation Technique");
    }

    /** Casts Excavation Technique at {@code target} and resolves its demonstrate trigger. */
    private void castAndResolveDemonstrate(Permanent target, boolean demonstrate) {
        harness.setHand(player1, List.of(new ExcavationTechnique()));
        addMana();
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, demonstrate);
    }

    private StackEntry copyControlledBy(Player player) {
        return gd.stack.stream()
                .filter(entry -> entry.isCopy() && entry.getControllerId().equals(player.getId()))
                .findFirst()
                .orElseThrow();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
