package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.FanBearer;
import com.github.laxika.magicalvibes.cards.m.MagmaSpray;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LabyrinthGuardian.class, MagmaSpray.class, FanBearer.class})
class LabyrinthGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself when it becomes the target of a spell")
    void sacrificesWhenTargetedBySpell() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new LabyrinthGuardian());

        harness.setHand(player2, List.of(new MagmaSpray()));
        harness.addMana(player2, ManaColor.RED, 1);
        // The sacrifice trigger resolves before Magma Spray can mark the creature for exile.
        harness.castAndResolveInstant(player2, 0, guardian.getId());

        harness.assertNotOnBattlefield(player1, "Labyrinth Guardian");
        harness.assertInGraveyard(player1, "Labyrinth Guardian");
    }

    private void setUpEmbalm() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new LabyrinthGuardian()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("Embalm exiles the source card from the graveyard as a cost")
    void embalmExilesSourceAsCost() {
        setUpEmbalm();

        harness.activateGraveyardAbility(player1, 0);

        harness.assertNotInGraveyard(player1, "Labyrinth Guardian");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Labyrinth Guardian"));
    }

    @Test
    @DisplayName("Embalm creates a white Zombie Illusion Warrior token copy with no mana cost")
    void embalmCreatesWhiteZombieTokenCopy() {
        setUpEmbalm();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities(); // resolve the Embalm ability

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Labyrinth Guardian") && p.getCard().isToken())
                .findFirst().orElseThrow();

        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getColors()).contains(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes())
                .contains(CardSubtype.ZOMBIE, CardSubtype.ILLUSION, CardSubtype.WARRIOR);
        assertThat(token.getCard().getManaCost()).isEmpty();
    }

    @Test
    @DisplayName("A spell controlled by the Guardian's controller also triggers sacrifice")
    void sacrificesWhenTargetedByOwnSpell() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new LabyrinthGuardian());
        harness.setHand(player1, List.of(new MagmaSpray()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, guardian.getId());

        harness.assertNotOnBattlefield(player1, "Labyrinth Guardian");
        harness.assertInGraveyard(player1, "Labyrinth Guardian");
    }

    @Test
    @DisplayName("Being targeted by an activated ability does not trigger sacrifice")
    void survivesTargetedAbility() {
        Permanent guardian = harness.addToBattlefieldAndReturn(player1, new LabyrinthGuardian());
        Permanent bearer = harness.addToBattlefieldAndReturn(player2, new FanBearer());
        bearer.setSummoningSick(false);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, guardian.getId());
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Labyrinth Guardian");
        harness.assertNotInGraveyard(player1, "Labyrinth Guardian");
        assertThat(guardian.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The embalmed token retains the spell-target sacrifice trigger")
    void embalmedTokenSacrificesWhenTargeted() {
        setUpEmbalm();
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new MagmaSpray()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Labyrinth Guardian"));

        harness.assertNotOnBattlefield(player1, "Labyrinth Guardian");
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Embalm cannot be activated on the opponent's turn")
    void embalmRequiresOwnTurn() {
        setUpEmbalm();
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Labyrinth Guardian");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Embalm cannot be activated outside a main phase")
    void embalmRequiresMainPhase() {
        setUpEmbalm();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Labyrinth Guardian");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Embalm requires an empty stack")
    void embalmRequiresEmptyStack() {
        setUpEmbalm();
        harness.setHand(player1, List.of(new LabyrinthGuardian()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Labyrinth Guardian");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Embalm requires four mana before exiling the card")
    void embalmRequiresEnoughMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new LabyrinthGuardian()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Labyrinth Guardian");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Embalm requires blue mana")
    void embalmRequiresBlueMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(new LabyrinthGuardian()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Labyrinth Guardian");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
