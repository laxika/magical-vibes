package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AardvarkSloth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeylineOfAnticipation;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheBlueSpirit.class, GrizzlyBears.class, LlanowarElves.class, LeylineOfAnticipation.class,
        AardvarkSloth.class, RaiseTheAlarm.class})
class TheBlueSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("The first creature spell each turn can be cast as though it had flash")
    void firstCreatureSpellEachTurnHasFlash() {
        harness.addToBattlefield(player1, new TheBlueSpirit());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Only the first creature spell each turn gets flash")
    void onlyFirstCreatureSpellGetsFlash() {
        harness.addToBattlefield(player1, new TheBlueSpirit());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A nontoken creature entering during combat draws a card")
    void nontokenCreatureEnteringDuringCombatDraws() {
        harness.addToBattlefield(player1, new TheBlueSpirit());
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(1)
                .anyMatch(LlanowarElves.class::isInstance);
    }

    @Test
    @DisplayName("The Blue Spirit entering during combat draws a card")
    void ownEntryDuringCombatDraws() {
        harness.addToBattlefield(player1, new LeylineOfAnticipation());
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.setHand(player1, List.of(new TheBlueSpirit()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(1)
                .anyMatch(LlanowarElves.class::isInstance);
    }

    @Test
    @DisplayName("A creature entering outside combat does not draw a card")
    void creatureEnteringOutsideCombatDoesNotDraw() {
        harness.addToBattlefield(player1, new TheBlueSpirit());
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Creature tokens entering during combat do not draw cards")
    void tokensEnteringDuringCombatDoNotDraw() {
        harness.addToBattlefield(player1, new TheBlueSpirit());
        harness.setLibrary(player1, List.of(new AardvarkSloth(), new AardvarkSloth()));
        harness.setHand(player1, List.of(new RaiseTheAlarm()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's nontoken creature entering during combat does not trigger a draw")
    void opponentsCreatureDoesNotDraw() {
        harness.addToBattlefield(player1, new TheBlueSpirit());
        harness.setLibrary(player1, List.of(new AardvarkSloth()));
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        harness.enterBattlefieldAndReturn(player2, new AardvarkSloth());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each nontoken creature entry triggers a draw, including during an opponent's combat")
    void multipleEntriesDuringOpponentsCombatDrawSeparately() {
        harness.addToBattlefield(player1, new TheBlueSpirit());
        harness.setLibrary(player1, List.of(new AardvarkSloth(), new AardvarkSloth()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_OF_COMBAT);

        harness.enterBattlefieldAndReturn(player1, new AardvarkSloth());
        harness.enterBattlefieldAndReturn(player1, new AardvarkSloth());

        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Casting The Blue Spirit consumes the first creature spell permission for that turn")
    void creatureCastBeforeSourceEnteredStillCounts() {
        harness.setHand(player1, List.of(new TheBlueSpirit(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "The Blue Spirit");
    }

    @Test
    @DisplayName("Noncreature spells do not consume the first creature spell flash permission")
    void noncreatureSpellDoesNotConsumePermission() {
        harness.addToBattlefield(player1, new TheBlueSpirit());
        harness.setLibrary(player1, List.of(new AardvarkSloth()));
        harness.setHand(player1, List.of(new RaiseTheAlarm(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        harness.castAndResolveInstant(player1, 0);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(1).anyMatch(AardvarkSloth.class::isInstance);
    }

    @Test
    @DisplayName("The flash permission applies only to The Blue Spirit's controller")
    void opponentDoesNotGainFlashPermission() {
        harness.addToBattlefield(player1, new TheBlueSpirit());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The first creature spell permission resets on each player's turn")
    void flashPermissionResetsOnNextTurn() {
        harness.addToBattlefield(player1, new TheBlueSpirit());
        harness.setLibrary(player2, List.of(new AardvarkSloth()));
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's creature spell does not consume the controller's flash permission")
    void opponentsCreatureCastDoesNotConsumePermission() {
        harness.addToBattlefield(player1, new TheBlueSpirit());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new AardvarkSloth()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.castCreature(player2, 0);
        resolveAllTriggers();

        harness.setHand(player1, List.of(new AardvarkSloth()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Putting a creature onto the battlefield does not consume the first creature spell permission")
    void uncastCreatureEntryDoesNotConsumePermission() {
        harness.addToBattlefield(player1, new TheBlueSpirit());
        harness.enterBattlefieldAndReturn(player1, new AardvarkSloth());
        harness.setHand(player1, List.of(new AardvarkSloth()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The flash permission does not allow casting a noncreature spell during combat")
    void noncreatureSpellDoesNotGainFlash() {
        harness.addToBattlefield(player1, new TheBlueSpirit());
        harness.setHand(player1, List.of(new LeylineOfAnticipation()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
