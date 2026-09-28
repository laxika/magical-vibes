package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoomerScrapper.class, GrizzlyBears.class})
class BoomerScrapperTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield loses 1 life and creates a Junk token")
    void entersWithJunkAndLifeLoss() {
        harness.setHand(player1, List.of(new BoomerScrapper()));
        addBoomerMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(findPermanent(player1, "Junk").getCard().getSubtypes())
                .contains(CardSubtype.JUNK);
    }

    @Test
    @DisplayName("Attacking loses 1 life and creates a Junk token")
    void attacksWithJunkAndLifeLoss() {
        Permanent scrapper = addCreatureReady(player1, new BoomerScrapper());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(scrapper)));
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        assertThat(findPermanents(player1, "Junk")).hasSize(1);
    }

    @Test
    @DisplayName("A token leaving under its controller's control puts a +1/+1 counter on Boomer Scrapper")
    void tokenLeavingAddsCounter() {
        harness.setHand(player1, List.of(new BoomerScrapper()));
        addBoomerMana();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent scrapper = findPermanent(player1, "Boomer Scrapper");
        Permanent token = findPermanent(player1, "Junk");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, token));
        resolveAllTriggers();

        assertThat(scrapper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A nontoken permanent leaving does not put a counter on Boomer Scrapper")
    void nontokenLeavingDoesNotAddCounter() {
        Permanent scrapper = addCreatureReady(player1, new BoomerScrapper());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bears));
        resolveAllTriggers();

        assertThat(scrapper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void addBoomerMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
