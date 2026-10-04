package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BoomerScrapper.class})
class BoomerScrapperTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield loses 1 life and creates a Junk token")
    void entersWithJunkAndLifeLoss() {
        harness.castFromHand(player1, new BoomerScrapper(), "{1}{B}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(findPermanents(player1, "Junk")).hasSize(1);
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
        harness.castFromHand(player1, new BoomerScrapper(), "{1}{B}{R}");
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
        Permanent otherScrapper = addCreatureReady(player1, new BoomerScrapper());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, otherScrapper));
        resolveAllTriggers();

        assertThat(scrapper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void junkSacrificeAddsCounterAndAllowsPaidCastingFromExile() {
        harness.castFromHand(player1, new BoomerScrapper(), "{1}{B}{R}");
        resolveAllTriggers();
        Permanent scrapper = findPermanent(player1, "Boomer Scrapper");
        Permanent junk = findPermanent(player1, "Junk");
        BoomerScrapper topCard = new BoomerScrapper();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(junk), null, null);
        assertThat(findPermanents(player1, "Junk")).isEmpty();
        resolveAllTriggers();

        assertThat(scrapper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, topCard.getId());
        resolveAllTriggers();
        assertThat(findPermanents(player1, "Boomer Scrapper")).hasSize(2);
        harness.assertLife(player1, 18);
    }

    @Test
    void junkCannotBeActivatedOutsideSorceryTiming() {
        harness.castFromHand(player1, new BoomerScrapper(), "{1}{B}{R}");
        resolveAllTriggers();
        Permanent junk = findPermanent(player1, "Junk");
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(junk), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanents(player1, "Junk")).containsExactly(junk);
    }

    @Test
    void opponentsTokenLeavingDoesNotAddCounter() {
        Permanent scrapper = addCreatureReady(player1, new BoomerScrapper());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new BoomerScrapper(), "{1}{B}{R}");
        resolveAllTriggers();
        Permanent junk = findPermanent(player2, "Junk");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, junk));
        resolveAllTriggers();

        assertThat(scrapper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player2, "Boomer Scrapper")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void tokenReturningToHandAddsCounter() {
        harness.castFromHand(player1, new BoomerScrapper(), "{1}{B}{R}");
        resolveAllTriggers();
        Permanent scrapper = findPermanent(player1, "Boomer Scrapper");
        Permanent junk = findPermanent(player1, "Junk");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, junk));
        resolveAllTriggers();

        assertThat(scrapper.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
