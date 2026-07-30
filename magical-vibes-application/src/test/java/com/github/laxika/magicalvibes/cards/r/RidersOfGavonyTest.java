package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RidersOfGavonyTest extends BaseCardTest {

    private static Card creature(String name, int power, int toughness, CardColor color, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(subtypes));
        return card;
    }

    private Permanent addRiders(com.github.laxika.magicalvibes.model.Player controller, CardSubtype chosen) {
        Permanent riders = new Permanent(new RidersOfGavony());
        riders.setSummoningSick(false);
        riders.setChosenSubtype(chosen);
        gd.playerBattlefields.get(controller.getId()).add(riders);
        return riders;
    }

    @Test
    @DisplayName("Resolving Riders of Gavony awaits a creature type choice, which is stored on it")
    void entersAndChoosesCreatureType() {
        harness.setHand(player1, List.of(new RidersOfGavony()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "GOBLIN");

        assertThat(findPermanent(player1, "Riders of Gavony").getChosenSubtype())
                .isEqualTo(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("A Human you control takes no combat damage from a creature of the chosen type")
    void humanIsProtectedFromChosenTypeDamage() {
        Permanent attacker = new Permanent(creature("Goblin Brute", 3, 3, CardColor.RED, CardSubtype.GOBLIN));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(creature("Village Militia", 1, 1, CardColor.WHITE, CardSubtype.HUMAN));
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        addRiders(player2, CardSubtype.GOBLIN);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // The Goblin's 3 damage is prevented; the 1/1 Human survives.
        harness.assertOnBattlefield(player2, "Village Militia");
        harness.assertOnBattlefield(player1, "Goblin Brute");
    }

    @Test
    @DisplayName("Riders of Gavony protects itself — it is a Human creature you control")
    void ridersProtectsItself() {
        Permanent attacker = new Permanent(creature("Goblin Brute", 3, 3, CardColor.RED, CardSubtype.GOBLIN));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent riders = addRiders(player2, CardSubtype.GOBLIN);
        riders.setBlocking(true);
        riders.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Riders' 3 damage kills the 3/3 Goblin; the Goblin's damage to Riders is prevented.
        harness.assertNotOnBattlefield(player1, "Goblin Brute");
        harness.assertOnBattlefield(player2, "Riders of Gavony");
    }

    @Test
    @DisplayName("A non-Human creature you control is not protected")
    void nonHumanIsNotProtected() {
        Permanent attacker = new Permanent(creature("Goblin Brute", 3, 3, CardColor.RED, CardSubtype.GOBLIN));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(creature("Llanowar Elves", 1, 1, CardColor.GREEN, CardSubtype.ELF));
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        addRiders(player2, CardSubtype.GOBLIN);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Creatures of other types still damage your Humans")
    void otherTypesAreNotProtectedAgainst() {
        Permanent attacker = new Permanent(creature("Elvish Brute", 3, 3, CardColor.GREEN, CardSubtype.ELF));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(creature("Village Militia", 1, 1, CardColor.WHITE, CardSubtype.HUMAN));
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        addRiders(player2, CardSubtype.GOBLIN);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Village Militia");
    }

    @Test
    @DisplayName("A creature of the chosen type can't block your attacking Human")
    void chosenTypeCannotBlockYourHuman() {
        Permanent attacker = new Permanent(creature("Village Militia", 2, 2, CardColor.WHITE, CardSubtype.HUMAN));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        addRiders(player1, CardSubtype.GOBLIN);

        Permanent blocker = new Permanent(creature("Goblin Brute", 3, 3, CardColor.RED, CardSubtype.GOBLIN));
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Humans an opponent controls are not protected")
    void opponentHumansAreNotProtected() {
        Permanent attacker = new Permanent(creature("Goblin Brute", 3, 3, CardColor.RED, CardSubtype.GOBLIN));
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(creature("Village Militia", 1, 1, CardColor.WHITE, CardSubtype.HUMAN));
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        addRiders(player1, CardSubtype.GOBLIN);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Village Militia");
    }
}
