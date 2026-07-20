package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KefnetsMonumentTest extends BaseCardTest {

    // ===== Cost reduction =====

    @Test
    @DisplayName("Blue creature spells cost {1} less")
    void blueCreatureSpellsCostOneLess() {
        harness.addToBattlefield(player1, new KefnetsMonument());
        // Air Elemental costs {3}{U}{U} — with the {1} reduction it should cost {2}{U}{U} (4 mana)
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(e -> e.getCard().getName().equals("Air Elemental"));
    }

    @Test
    @DisplayName("Cannot cast a blue creature short of the reduced cost")
    void cannotCastBlueCreatureWithoutEnoughMana() {
        harness.addToBattlefield(player1, new KefnetsMonument());
        // Reduced cost is {2}{U}{U} (4 mana); 3 mana is not enough
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Non-blue creature spells are not reduced")
    void nonBlueCreaturesNotReduced() {
        harness.addToBattlefield(player1, new KefnetsMonument());
        // Grizzly Bears costs {1}{G} — not blue, so {G} alone is not enough
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Creature-cast skip-untap trigger =====

    @Test
    @DisplayName("Casting a creature locks a chosen opponent creature out of its next untap step")
    void castingCreatureSkipsOpponentUntap() {
        harness.addToBattlefield(player1, new KefnetsMonument());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        // The trigger prompts for a "creature an opponent controls" target
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities(); // resolve the trigger

        assertThat(opponentCreature.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("The trigger can only target a creature an opponent controls, not your own")
    void triggerCannotTargetOwnCreature() {
        harness.addToBattlefield(player1, new KefnetsMonument());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        // No opponent creature exists, so the trigger has no legal target and is not put on the stack
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
        assertThat(ownCreature.getSkipUntapCount()).isZero();
    }

    // ===== Non-creature spell does not trigger =====

    @Test
    @DisplayName("Casting a non-creature spell does not trigger the skip-untap")
    void nonCreatureSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new KefnetsMonument());
        addCreatureReady(player2, new GrizzlyBears()); // a legal target exists
        // Spellbook is a {2} artifact — not a creature spell
        harness.setHand(player1, List.of(new Spellbook()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }
}
