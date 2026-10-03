package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BovineIntervention;
import com.github.laxika.magicalvibes.cards.b.BristlepackSentry;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CausticBronco.class, BovineIntervention.class, BristlepackSentry.class, Swamp.class})
class CausticBroncoTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking unsaddled reveals the top card, puts it into hand, and loses its mana value in life")
    void attacksUnsaddled() {
        Card topCard = new CausticBronco();
        Permanent bronco = addCreatureReady(player1, new CausticBronco());
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(bronco.isSaddled()).isFalse();
    }

    @Test
    @DisplayName("Attacking saddled makes each opponent lose the revealed card's mana value in life")
    void attacksSaddled() {
        Card topCard = new CausticBronco();
        Permanent bronco = addCreatureReady(player1, new CausticBronco());
        Permanent saddler = addCreatureReady(player1, new BristlepackSentry());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(bronco.isSaddled()).isTrue();
        assertThat(saddler.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void revealingLandDoesNotLoseLife(boolean saddled) {
        Card topCard = new Swamp();
        addCreatureReady(player1, new CausticBronco());
        if (saddled) {
            addCreatureReady(player1, new BristlepackSentry());
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void emptyLibraryDoesNotCauseLifeLossOrDrawingLoss(boolean saddled) {
        addCreatureReady(player1, new CausticBronco());
        if (saddled) {
            addCreatureReady(player1, new BristlepackSentry());
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void lifeLossUsesLastKnownSaddleStatusAfterBroncoIsDestroyed(boolean saddled) {
        Card topCard = new CausticBronco();
        Permanent bronco = addCreatureReady(player1, new CausticBronco());
        if (saddled) {
            addCreatureReady(player1, new BristlepackSentry());
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player2, List.of(new BovineIntervention()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            assertThat(gd.stack).hasSize(1);
            harness.addMana(player2, ManaColor.WHITE, 1);
            harness.addMana(player2, ManaColor.COLORLESS, 1);
            harness.castInstant(player2, 0, bronco.getId());
            resolveAllTriggers();
        });

        harness.assertNotOnBattlefield(player1, "Caustic Bronco");
        harness.assertInGraveyard(player1, "Caustic Bronco");
        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        harness.assertLife(player1, saddled ? 20 : 18);
        harness.assertLife(player2, saddled ? 18 : 20);
    }

    @Test
    void summoningSickCreatureCanPaySaddleCost() {
        Permanent bronco = addCreatureReady(player1, new CausticBronco());
        Permanent saddler = harness.addToBattlefieldAndReturn(player1, new BristlepackSentry());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(saddler.isTapped()).isTrue();
        assertThat(bronco.isSaddled()).isTrue();
        assertThat(bronco.isTapped()).isFalse();
    }

    @Test
    void broncosOwnPowerCannotPaySaddleCost() {
        Permanent bronco = addCreatureReady(player1, new CausticBronco());
        Permanent other = addCreatureReady(player1, new CausticBronco());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(bronco.isSaddled()).isFalse();
        assertThat(bronco.isTapped()).isFalse();
        assertThat(other.isTapped()).isFalse();
    }

    @Test
    void saddleCannotBeActivatedDuringCombat() {
        Permanent bronco = addCreatureReady(player1, new CausticBronco());
        Permanent saddler = addCreatureReady(player1, new BristlepackSentry());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(bronco.isSaddled()).isFalse();
        assertThat(saddler.isTapped()).isFalse();
    }

    @Test
    void multipleCreaturesCanCombinePowerToPaySaddleCost() {
        Permanent bronco = addCreatureReady(player1, new CausticBronco());
        Permanent first = addCreatureReady(player1, new CausticBronco());
        Permanent second = addCreatureReady(player1, new CausticBronco());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(bronco.isTapped()).isFalse();
        assertThat(bronco.isSaddled()).isTrue();
    }
}
