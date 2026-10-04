package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
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

        cast(target, false);

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
    @DisplayName("May demonstrate")
    void mayDemonstrate() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(target, true);

        assertThat(gd.stack).hasSize(3);
    }

    @Test
    @DisplayName("Can destroy your own permanent and give you the Treasures")
    void canDestroyOwnPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(target, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
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
        harness.handleMayAbilityChosen(player1, false);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        resolveAllTriggers();

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        harness.assertInGraveyard(player1, "Excavation Technique");
    }

    @Test
    @DisplayName("Demonstrate offers the caster a chance to choose a new copy target")
    void demonstrateOffersNewTargets() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        cast(target, true);

        assertThat(gd.interaction.isAwaitingInput())
                .as("The caster must be offered a choice of new targets before the opponent copies the spell")
                .isTrue();
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
        harness.handleMayAbilityChosen(player1, true);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, spell.getId());
        harness.assertInGraveyard(player1, "Excavation Technique");
        harness.passBothPriorities();

        assertThat(gd.stack.stream().anyMatch(entry -> entry.isCopy())
                || gd.interaction.isAwaitingInput())
                .as("Demonstrate must create a copy or offer its new-target choice even after the original is countered")
                .isTrue();
    }

    @Test
    @DisplayName("The decision to demonstrate is made when the trigger resolves")
    void demonstrateDecisionWaitsForTriggerResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ExcavationTechnique()));
        addMana();

        harness.castSorcery(player1, 0, target.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    private void cast(Permanent target, boolean demonstrate) {
        harness.setHand(player1, List.of(new ExcavationTechnique()));
        addMana();
        harness.castSorcery(player1, 0, target.getId());
        harness.handleMayAbilityChosen(player1, demonstrate);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
