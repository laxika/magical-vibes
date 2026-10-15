package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.t.TragicSlip;
import com.github.laxika.magicalvibes.cards.y.YoungWolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Gravecrawler.class, YoungWolf.class, TragicSlip.class})
class GravecrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Can cast from graveyard while controlling a Zombie")
    void canCastFromGraveyardWithZombie() {
        harness.setGraveyard(player1, List.of(new Gravecrawler()));
        harness.addToBattlefield(player1, new Gravecrawler());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFromGraveyard(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Gravecrawler");
    }

    @Test
    @DisplayName("Cannot cast from graveyard without controlling a Zombie")
    void cannotCastFromGraveyardWithoutZombie() {
        harness.setGraveyard(player1, List.of(new Gravecrawler()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card cannot be cast from graveyard");
    }

    @Test
    @DisplayName("Opponent's Zombie does not enable casting from graveyard")
    void opponentZombieDoesNotEnableGraveyardCast() {
        harness.setGraveyard(player1, List.of(new Gravecrawler()));
        harness.addToBattlefield(player2, new Gravecrawler());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card cannot be cast from graveyard");
    }

    @Test
    @DisplayName("Resolves onto battlefield from graveyard and is not exiled")
    void resolvesFromGraveyardOntoBattlefield() {
        harness.setGraveyard(player1, List.of(new Gravecrawler()));
        harness.addToBattlefield(player1, new Gravecrawler());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gravecrawler");
        assertThat(countPermanents(player1, "Gravecrawler")).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Gravecrawler"));
    }

    @Test
    @DisplayName("Gravecrawler cannot be declared as a blocker")
    void cannotBeDeclaredAsBlocker() {
        addCreatureReady(player2, new Gravecrawler());

        Permanent attacker = addCreatureReady(player1, new YoungWolf());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("A non-Zombie creature does not enable casting from the graveyard")
    void nonZombieDoesNotEnableGraveyardCast() {
        harness.setGraveyard(player1, List.of(new Gravecrawler()));
        harness.addToBattlefield(player1, new YoungWolf());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card cannot be cast from graveyard");
        harness.assertInGraveyard(player1, "Gravecrawler");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Zombie still enables casting from the graveyard")
    void tappedZombieEnablesGraveyardCast() {
        harness.setGraveyard(player1, List.of(new Gravecrawler()));
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new Gravecrawler());
        zombie.tap();
        zombie.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Gravecrawler")).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Gravecrawler");
    }

    @Test
    @DisplayName("Graveyard casting still requires black mana")
    void cannotCastFromGraveyardWithOnlyColorlessMana() {
        harness.setGraveyard(player1, List.of(new Gravecrawler()));
        harness.addToBattlefield(player1, new Gravecrawler());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        harness.assertInGraveyard(player1, "Gravecrawler");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Graveyard permission does not allow casting during combat")
    void cannotCastFromGraveyardDuringCombat() {
        harness.setGraveyard(player1, List.of(new Gravecrawler()));
        harness.addToBattlefield(player1, new Gravecrawler());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cast sorcery-speed spell from graveyard now");
        harness.assertInGraveyard(player1, "Gravecrawler");
    }

    @Test
    @DisplayName("Graveyard permission does not allow casting on the opponent's turn")
    void cannotCastFromGraveyardOnOpponentsTurn() {
        harness.setGraveyard(player1, List.of(new Gravecrawler()));
        harness.addToBattlefield(player1, new Gravecrawler());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cast sorcery-speed spell from graveyard now");
        harness.assertInGraveyard(player1, "Gravecrawler");
    }

    @Test
    @DisplayName("Graveyard permission does not allow casting while the stack is nonempty")
    void cannotCastFromGraveyardWithNonemptyStack() {
        harness.setGraveyard(player1, List.of(new Gravecrawler(), new Gravecrawler()));
        harness.addToBattlefield(player1, new Gravecrawler());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castFromGraveyard(player1, 0);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cast sorcery-speed spell from graveyard now");
        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Gravecrawler");
    }

    @Test
    @DisplayName("Casting from hand does not require another Zombie")
    void canCastFromHandWithoutZombie() {
        harness.setHand(player1, List.of(new Gravecrawler()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gravecrawler");
        harness.assertNotInHand(player1, "Gravecrawler");
    }

    @Test
    @DisplayName("Losing the other Zombie after casting does not prevent resolution")
    void resolvesAfterOtherZombieDies() {
        harness.setGraveyard(player1, List.of(new Gravecrawler()));
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new Gravecrawler());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromGraveyard(player1, 0);

        harness.setHand(player2, List.of(new TragicSlip()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player2, 0, zombie.getId());

        harness.assertNotOnBattlefield(player1, "Gravecrawler");
        harness.assertInGraveyard(player1, "Gravecrawler");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Gravecrawler")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Gravecrawler cast from the graveyard can be cast again after dying")
    void canCastAgainAfterDying() {
        harness.setGraveyard(player1, List.of(new Gravecrawler()));
        harness.addToBattlefield(player1, new Gravecrawler());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();
        Permanent returned = findPermanents(player1, "Gravecrawler").getLast();

        harness.setHand(player2, List.of(new TragicSlip()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player2, 0, returned.getId());

        harness.assertInGraveyard(player1, "Gravecrawler");
        assertThat(countPermanents(player1, "Gravecrawler")).isEqualTo(1);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Gravecrawler")).isEqualTo(2);
        harness.assertNotInGraveyard(player1, "Gravecrawler");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
