package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ElspethUndauntedHero.class, GrizzlyBears.class, SuntailHawk.class})
class ElspethUndauntedHeroTest extends BaseCardTest {

    @Test
    @DisplayName("+2 puts a +1/+1 counter on up to two target creatures")
    void plusTwoCountersTwoTargetCreatures() {
        addReadyElspeth(4);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(ownCreature.getId(), opposingCreature.getId()));
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanent(player1, "Elspeth, Undaunted Hero")
                .getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("-2 lets you choose the named card from the library or graveyard")
    void minusTwoSearchesLibraryOrGraveyard() {
        Permanent elspeth = addReadyElspeth(4);
        Card libraryHoplite = namedSunlitHoplite();
        Card graveyardHoplite = namedSunlitHoplite();
        Card handHoplite = namedSunlitHoplite();
        harness.setLibrary(player1, List.of(libraryHoplite));
        harness.setGraveyard(player1, List.of(graveyardHoplite));
        harness.setHand(player1, List.of(handHoplite));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.SearchLibraryAndOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(libraryHoplite.getId(), graveyardHoplite.getId());
        assertThat(choice.validCardIds()).doesNotContain(handHoplite.getId());

        harness.handleMultipleCardsChosen(player1, List.of(libraryHoplite.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .map(Permanent::getCard)).contains(libraryHoplite);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardHoplite);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handHoplite);
        assertThat(elspeth.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("-8 boosts your creatures by white devotion and grants flying until end of turn")
    void minusEightUsesWhiteDevotion() {
        addReadyElspeth(8);
        addCreatureReady(player1, new SuntailHawk());
        addCreatureReady(player1, new SuntailHawk());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.FLYING)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.FLYING)).isFalse();
    }

    private Permanent addReadyElspeth(int loyalty) {
        Permanent permanent = new Permanent(new ElspethUndauntedHero());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private Card namedSunlitHoplite() {
        Card card = new Card();
        card.setName("Sunlit Hoplite");
        card.setType(CardType.CREATURE);
        card.setManaCost("{W}");
        card.setColor(CardColor.WHITE);
        card.setPower(2);
        card.setToughness(1);
        return card;
    }
}
