package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.v.ViridianLongbow;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RazorBarrier.class, Forest.class, GoldMyr.class, Shatter.class, ViridianLongbow.class})
class RazorBarrierTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing a color grants protection from that color to any permanent you control")
    void choosingColorGrantsProtectionToAnyControlledPermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new RazorBarrier()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, land.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "BLUE");

        assertThat(land.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLUE);
    }

    @Test
    @DisplayName("Choosing ARTIFACT grants protection from artifacts")
    void choosingArtifactGrantsProtectionFromArtifacts() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoldMyr());
        harness.setHand(player1, List.of(new RazorBarrier()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.handleListChoice(player1, "ARTIFACT");

        assertThat(creature.getProtectionFromCardTypes()).contains(CardType.ARTIFACT);
    }

    @Test
    @DisplayName("Cannot target a permanent controlled by an opponent")
    void cannotTargetOpponentsPermanent() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GoldMyr());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.setHand(player1, List.of(new RazorBarrier()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a permanent you control");
    }

    @Test
    @DisplayName("Protection is cleared at end of turn")
    void protectionWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoldMyr());
        harness.setHand(player1, List.of(new RazorBarrier()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.handleListChoice(player1, "RED");

        assertThat(creature.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(creature.getProtectionFromColorsUntilEndOfTurn()).isEmpty();
    }

    @Test
    @DisplayName("Choosing red in response makes Shatter's target illegal")
    void chosenColorStopsSpellAlreadyOnStack() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoldMyr());
        harness.setHand(player1, List.of(new RazorBarrier()));
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, creature.getId());
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.handleListChoice(player1, "RED");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gold Myr");
        harness.assertInGraveyard(player2, "Shatter");
    }

    @Test
    @DisplayName("Protection from artifacts does not protect an artifact from Shatter")
    void artifactProtectionDoesNotStopNonartifactSpell() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoldMyr());
        harness.setHand(player1, List.of(new RazorBarrier()));
        harness.setHand(player2, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.handleListChoice(player1, "ARTIFACT");
        harness.castAndResolveInstant(player2, 0, creature.getId());

        harness.assertNotOnBattlefield(player1, "Gold Myr");
        harness.assertInGraveyard(player1, "Gold Myr");
    }

    @Test
    @DisplayName("Protection from artifacts detaches Equipment without destroying it")
    void artifactProtectionDetachesEquipment() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoldMyr());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new ViridianLongbow());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();
        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
        harness.setHand(player1, List.of(new RazorBarrier()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.handleListChoice(player1, "ARTIFACT");

        assertThat(equipment.getAttachedTo()).isNull();
        harness.assertOnBattlefield(player1, "Viridian Longbow");
        harness.assertOnBattlefield(player1, "Gold Myr");
    }

    @Test
    @DisplayName("Protection from artifacts expires at end of turn")
    void artifactProtectionWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GoldMyr());
        harness.setHand(player1, List.of(new RazorBarrier()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.handleListChoice(player1, "ARTIFACT");
        assertThat(creature.getProtectionFromCardTypes()).contains(CardType.ARTIFACT);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(creature.getProtectionFromCardTypes()).isEmpty();
    }
}
