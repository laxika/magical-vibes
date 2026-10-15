package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TrueBeliever;
import com.github.laxika.magicalvibes.cards.w.WingedCoatl;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PredatoryAdvantage.class, GrizzlyBears.class, Shock.class, WingedCoatl.class, TrueBeliever.class})
class PredatoryAdvantageTest extends BaseCardTest {

    private void advanceToEndStepTrigger(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        harness.passBothPriorities(); // resolve trigger
    }

    private long lizardTokens(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Lizard"))
                .count();
    }

    @Test
    @DisplayName("Creates a Lizard on an opponent's end step when they didn't cast a creature spell")
    void createsTokenOnOpponentEndStepWithoutCreatureSpell() {
        harness.addToBattlefield(player1, new PredatoryAdvantage());

        advanceToEndStepTrigger(player2);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().isToken()
                        && p.getCard().getName().equals("Lizard")
                        && p.getCard().hasType(CardType.CREATURE)
                        && p.getCard().getColor() == CardColor.GREEN
                        && p.getCard().getSubtypes().contains(CardSubtype.LIZARD)
                        && p.getCard().getPower() == 2
                        && p.getCard().getToughness() == 2);
        assertThat(lizardTokens(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("No token when the end-step opponent cast a creature spell this turn")
    void noTokenWhenOpponentCastCreatureSpell() {
        harness.addToBattlefield(player1, new PredatoryAdvantage());
        gd.recordSpellCast(player2.getId(), new GrizzlyBears());

        advanceToEndStepTrigger(player2);

        assertThat(lizardTokens(player1)).isZero();
    }

    @Test
    @DisplayName("A noncreature spell cast by the opponent still yields a token")
    void noncreatureSpellStillCreatesToken() {
        harness.addToBattlefield(player1, new PredatoryAdvantage());
        gd.recordSpellCast(player2.getId(), new Shock());

        advanceToEndStepTrigger(player2);

        assertThat(lizardTokens(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger on the controller's own end step")
    void noTokenOnControllersOwnEndStep() {
        harness.addToBattlefield(player1, new PredatoryAdvantage());

        advanceToEndStepTrigger(player1);

        assertThat(lizardTokens(player1)).isZero();
    }

    @Test
    @DisplayName("A creature entering without being cast does not prevent the token")
    void creatureEnteringWithoutBeingCastStillCreatesToken() {
        harness.addToBattlefield(player1, new PredatoryAdvantage());
        harness.enterBattlefieldAndReturn(player2, new WingedCoatl());

        advanceToEndStepTrigger(player2);

        assertThat(lizardTokens(player1)).isEqualTo(1);
        assertThat(lizardTokens(player2)).isZero();
    }

    @Test
    @DisplayName("The controller casting a creature does not prevent the opponent's end-step token")
    void controllersCreatureSpellDoesNotPreventToken() {
        harness.addToBattlefield(player1, new PredatoryAdvantage());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.castFromHand(player1, new WingedCoatl(), "{1}{G}{U}");
        harness.passBothPriorities();

        advanceToEndStepTrigger(player2);

        assertThat(lizardTokens(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature cast in response makes the intervening condition false at resolution")
    void creatureCastInResponsePreventsToken() {
        harness.addToBattlefield(player1, new PredatoryAdvantage());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.castFromHand(player2, new WingedCoatl(), "{1}{G}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(lizardTokens(player1)).isZero();
    }

    @Test
    @DisplayName("Each copy creates its own token")
    void multipleCopiesCreateMultipleTokens() {
        harness.addToBattlefield(player1, new PredatoryAdvantage());
        harness.addToBattlefield(player1, new PredatoryAdvantage());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(lizardTokens(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's shroud does not stop this nontargeted ability")
    void opponentShroudDoesNotPreventToken() {
        harness.addToBattlefield(player1, new PredatoryAdvantage());
        harness.addToBattlefield(player2, new TrueBeliever());

        advanceToEndStepTrigger(player2);

        assertThat(lizardTokens(player1)).isEqualTo(1);
    }
}
