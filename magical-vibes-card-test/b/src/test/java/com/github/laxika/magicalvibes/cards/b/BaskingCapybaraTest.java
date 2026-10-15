package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.a.Abrade;
import com.github.laxika.magicalvibes.cards.s.SorcerousSpyglass;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BaskingCapybara.class, Plains.class, Abrade.class, SorcerousSpyglass.class})
class BaskingCapybaraTest extends BaseCardTest {

    @Test
    void getsPlusThreePowerWithFourPermanentCardsInControllerGraveyard() {
        harness.setGraveyard(player1, List.of(new Plains(), new Plains(), new Plains(), new Plains()));
        Permanent capybara = harness.addToBattlefieldAndReturn(player1, new BaskingCapybara());

        assertThat(gqs.getEffectivePower(gd, capybara)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, capybara)).isEqualTo(3);
    }

    @Test
    void doesNotCountNonpermanentCardsOrOpponentsGraveyard() {
        harness.setGraveyard(player1, List.of(new Plains(), new Plains(), new Plains(), new Abrade()));
        harness.setGraveyard(player2, List.of(new Plains(), new Plains(), new Plains(), new Plains()));
        Permanent capybara = harness.addToBattlefieldAndReturn(player1, new BaskingCapybara());

        assertThat(gqs.getEffectivePower(gd, capybara)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, capybara)).isEqualTo(3);
    }

    @Test
    void losesBoostWhenControllerGraveyardDropsBelowFourPermanentCards() {
        harness.setGraveyard(player1, List.of(new Plains(), new Plains(), new Plains(), new Plains()));
        Permanent capybara = harness.addToBattlefieldAndReturn(player1, new BaskingCapybara());

        assertThat(gqs.getEffectivePower(gd, capybara)).isEqualTo(4);

        harness.setGraveyard(player1, List.of(new Plains(), new Plains(), new Plains()));

        assertThat(gqs.getEffectivePower(gd, capybara)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, capybara)).isEqualTo(3);
    }

    @Test
    void gainsBoostAsMixedPermanentCardsReachThresholdAndBoostsOnlyItself() {
        Permanent capybara = harness.addToBattlefieldAndReturn(player1, new BaskingCapybara());
        Permanent opposingCapybara = harness.addToBattlefieldAndReturn(player2, new BaskingCapybara());
        harness.setGraveyard(player1, List.of(new Plains(), new BaskingCapybara(), new SorcerousSpyglass()));

        assertThat(gqs.getEffectivePower(gd, capybara)).isEqualTo(1);

        harness.setGraveyard(player1, List.of(new Plains(), new Plains(), new BaskingCapybara(),
                new SorcerousSpyglass(), new Abrade()));

        assertThat(gqs.getEffectivePower(gd, capybara)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, capybara)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingCapybara)).isEqualTo(1);

        harness.setGraveyard(player1, List.of(new Plains(), new Plains(), new Plains(),
                new BaskingCapybara(), new SorcerousSpyglass()));

        assertThat(gqs.getEffectivePower(gd, capybara)).isEqualTo(4);
    }
}
