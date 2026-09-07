package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmptyShrineKannushi.class, EagerFirstYear.class})
class EmptyShrineKannushiTest extends BaseCardTest {

    private static Card createCreature(String name, CardColor color, String manaCost) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private Permanent attackWithKannushi() {
        Permanent attacker = new Permanent(new EmptyShrineKannushi());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        return attacker;
    }

    @Test
    @DisplayName("A white creature cannot block Empty-Shrine Kannushi, which is itself white")
    void whiteCreatureCannotBlock() {
        attackWithKannushi();

        Permanent blocker = new Permanent(createCreature("White Bear", CardColor.WHITE, "{1}{W}"));
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("A red creature can block while its controller controls no red permanent")
    void redCreatureCanBlockWithoutRedPermanent() {
        attackWithKannushi();

        Permanent blocker = new Permanent(createCreature("Red Bear", CardColor.RED, "{1}{R}"));
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Gaining a red permanent extends the protection to red creatures")
    void redCreatureCannotBlockOnceControllerHasRedPermanent() {
        attackWithKannushi();

        Permanent redOwn = new Permanent(createCreature("Red Ally", CardColor.RED, "{1}{R}"));
        gd.playerBattlefields.get(player1.getId()).add(redOwn);

        Permanent blocker = new Permanent(createCreature("Red Bear", CardColor.RED, "{1}{R}"));
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("An opponent's red permanent does not grant protection from red")
    void opponentRedPermanentDoesNotGrantProtection() {
        attackWithKannushi();

        Permanent blocker = new Permanent(createCreature("Red Bear", CardColor.RED, "{1}{R}"));
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        gd.playerBattlefields.get(player2.getId()).add(
                new Permanent(createCreature("Red Onlooker", CardColor.RED, "{1}{R}")));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can die simultaneously with another creature without reevaluating protection off the battlefield")
    void canDieSimultaneouslyWithAnotherCreature() {
        Permanent kannushi = harness.addToBattlefieldAndReturn(player1, new EmptyShrineKannushi());
        Permanent eagerFirstYear = harness.addToBattlefieldAndReturn(player1, new EagerFirstYear());
        kannushi.setMarkedDamage(1);
        eagerFirstYear.setMarkedDamage(2);

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Empty-Shrine Kannushi", "Eager First-Year");
    }

    @Test
    @DisplayName("Can die in combat while damage triggers are collected")
    void canDieInCombatWhileDamageTriggersAreCollected() {
        Permanent kannushi = harness.addToBattlefieldAndReturn(player1, new EmptyShrineKannushi());
        kannushi.setSummoningSick(false);
        kannushi.setAttacking(true);

        Permanent blocker = new Permanent(createCreature("Red Bear", CardColor.RED, "{1}{R}"));
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Empty-Shrine Kannushi");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.getMarkedDamage()).isEqualTo(1));
    }
}
