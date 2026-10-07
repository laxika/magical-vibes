package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BlackCarriage;
import com.github.laxika.magicalvibes.cards.b.BeastWalkers;
import com.github.laxika.magicalvibes.cards.b.BindingGrasp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpectralBears.class, BeastWalkers.class, BlackCarriage.class, BindingGrasp.class, Card.class})
class SpectralBearsTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking into a defender with no black permanents locks Spectral Bears' next untap")
    void locksUntapWhenDefenderHasNoBlackPermanents() {
        Permanent bears = addCreatureReady(player1, new SpectralBears());
        addCreatureReady(player2, new BeastWalkers());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(bears.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("No untap lock when the defending player controls a black nontoken permanent")
    void noLockWhenDefenderHasBlackNontokenPermanent() {
        Permanent bears = addCreatureReady(player1, new SpectralBears());
        addCreatureReady(player2, new BlackCarriage());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(bears.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("A black token the defender controls does not stop the untap lock")
    void blackTokenDoesNotCount() {
        Permanent bears = addCreatureReady(player1, new SpectralBears());
        Card token = createCreature("Black Token", 1, 1, CardColor.BLACK);
        token.setToken(true);
        harness.addToBattlefield(player2, token);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(bears.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Only the defending player's permanents matter — the attacker's own black permanent is ignored")
    void controllersBlackPermanentIsIgnored() {
        Permanent bears = addCreatureReady(player1, new SpectralBears());
        addCreatureReady(player1, new BlackCarriage());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(bears.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability triggers when the defending player controls no permanents")
    void locksUntapWhenDefenderControlsNoPermanents() {
        Permanent bears = addCreatureReady(player1, new SpectralBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(bears.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("The lock lasts through only the next untap step")
    void locksOnlyNextUntapStep() {
        Permanent bears = addCreatureReady(player1, new SpectralBears());
        addCreatureReady(player2, new BeastWalkers());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.performUntapStep(player1);
        assertThat(bears.isTapped()).isTrue();

        harness.performUntapStep(player1);
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The condition is checked again if a black nontoken permanent enters before resolution")
    void conditionLostBeforeResolutionDoesNotLockBears() {
        Permanent bears = addCreatureReady(player1, new SpectralBears());

        declareAttackers(player1, List.of(0));
        addCreatureReady(player2, new BlackCarriage());
        resolveAllTriggers();

        assertThat(bears.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("Losing the defending player's black permanent does not create a missed attack trigger")
    void blackPermanentLeavingAfterAttackDoesNotCreateTrigger() {
        Permanent bears = addCreatureReady(player1, new SpectralBears());
        Permanent carriage = addCreatureReady(player2, new BlackCarriage());

        declareAttackers(player1, List.of(0));
        assertThat(gd.stack).isEmpty();
        gd.playerBattlefields.get(player2.getId()).remove(carriage);
        gd.playerGraveyards.get(player2.getId()).add(carriage.getCard());
        resolveAllTriggers();

        harness.performUntapStep(player1);
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Changing control after resolution does not restrict the new controller's untap step")
    void newControllerCanUntapBearsBeforeOriginalControllersNextUntap() {
        Permanent bears = attackThenStealBears();

        harness.performUntapStep(player1);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The restriction expires at the original controller's next untap even while Bears are stolen")
    void restrictionExpiresWhileAnotherPlayerControlsBears() {
        Permanent bears = attackThenStealBears();

        harness.performUntapStep(player2);
        assertThat(bears.isTapped()).isTrue();
        harness.performUntapStep(player1);

        assertThat(bears.isTapped()).isFalse();
    }

    private Permanent attackThenStealBears() {
        Permanent bears = addCreatureReady(player2, new SpectralBears());
        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BindingGrasp()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(bears.isTapped()).isTrue();
        return bears;
    }

    private static Card createCreature(String name, int power, int toughness, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }
}
