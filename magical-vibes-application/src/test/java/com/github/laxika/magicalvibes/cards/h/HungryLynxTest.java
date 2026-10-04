package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.Cat;
import com.github.laxika.magicalvibes.cards.r.RancidRats;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({HungryLynx.class, Cat.class, RancidRats.class, Shock.class})
class HungryLynxTest extends BaseCardTest {

    @Test
    @DisplayName("Cats you control have protection from Rats")
    void catsYouControlHaveProtectionFromRats() {
        Permanent lynx = harness.addToBattlefieldAndReturn(player1, new HungryLynx());
        Permanent ownCat = harness.addToBattlefieldAndReturn(player1, new Cat());
        Permanent opposingRat = harness.addToBattlefieldAndReturn(player2, new RancidRats());

        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, lynx, opposingRat)).isTrue();
        assertThat(gqs.hasProtectionFromSourceSubtypes(gd, ownCat, opposingRat)).isTrue();
    }

    @Test
    @DisplayName("At your end step, target opponent creates a deathtouch Rat token")
    void targetOpponentCreatesRatTokenAtEndStep() {
        harness.addToBattlefield(player1, new HungryLynx());

        advanceToEndStep(player1);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPlayerIds()).containsExactly(player2.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        Permanent rat = findPermanents(player2, "Rat").getFirst();
        assertThat(rat.getCard().getSubtypes()).contains(CardSubtype.RAT);
        assertThat(gqs.hasKeyword(gd, rat, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    @DisplayName("A Rat dying puts a +1/+1 counter on each Cat you control")
    void ratDeathPutsCountersOnControlledCats() {
        Permanent lynx = harness.addToBattlefieldAndReturn(player1, new HungryLynx());
        Permanent ownCat = harness.addToBattlefieldAndReturn(player1, new Cat());
        Permanent opposingCat = harness.addToBattlefieldAndReturn(player2, new Cat());
        Permanent rat = harness.addToBattlefieldAndReturn(player2, new RancidRats());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, rat.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(lynx.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingCat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void advanceToEndStep(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
