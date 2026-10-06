package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.o.OsseousExhale;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SinkholeSurveyor.class, OsseousExhale.class})
class SinkholeSurveyorTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking loses 1 life and lets Sinkhole Surveyor endure with a counter")
    void enduresWithCounter() {
        Permanent surveyor = addCreatureReady(player1, new SinkholeSurveyor());
        harness.setLife(player1, 20);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Put 1 +1/+1 counter on this permanent");

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(surveyor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
    }

    @Test
    @DisplayName("Attacking loses 1 life and lets Sinkhole Surveyor endure with a Spirit")
    void enduresWithSpirit() {
        addCreatureReady(player1, new SinkholeSurveyor());
        harness.setLife(player1, 20);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Create a 1/1 white Spirit creature token");

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    void createsSpiritAndLosesLifeWhenSurveyorDiesBeforeTriggerResolves() {
        Permanent surveyor = addCreatureReady(player1, new SinkholeSurveyor());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new OsseousExhale()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        harness.castAndResolveInstant(player2, 0, surveyor.getId());
        harness.assertInGraveyard(player1, "Sinkhole Surveyor");
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
        Permanent spirit = findPermanent(player1, "Spirit");
        assertThat(spirit.getCard().isToken()).isTrue();
        assertThat(spirit.getCard().getPower()).isEqualTo(1);
        assertThat(spirit.getCard().getToughness()).isEqualTo(1);
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
    }

    @Test
    void opponentSurveyorBenefitsItsControllerWhenItAttacks() {
        Permanent surveyor = addCreatureReady(player2, new SinkholeSurveyor());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Put 1 +1/+1 counter on this permanent");

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        assertThat(surveyor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(findPermanents(player2, "Spirit")).isEmpty();
    }
}
