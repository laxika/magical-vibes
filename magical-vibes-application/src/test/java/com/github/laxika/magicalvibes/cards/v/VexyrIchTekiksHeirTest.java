package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.ChoiceOfFortunes;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LurkerInTheDeep;
import com.github.laxika.magicalvibes.cards.n.NornsFetchling;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VexyrIchTekiksHeir.class, LurkerInTheDeep.class, Island.class, GrizzlyBears.class,
        NornsFetchling.class, ChoiceOfFortunes.class})
class VexyrIchTekiksHeirTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Phyrexian Golem whenever you seek one or more cards")
    void createsGolemWhenYouSeek() {
        harness.addToBattlefield(player1, new VexyrIchTekiksHeir());
        harness.setLibrary(player1, List.of(new Island(), new GrizzlyBears()));
        harness.castFromHand(player1, new LurkerInTheDeep(), "{3}{U}{U}{U}");
        resolveAllTriggers();

        Permanent golem = findPermanent(player1, "Phyrexian Golem");
        assertThat(golem.getCard().getSubtypes())
                .contains(CardSubtype.PHYREXIAN, CardSubtype.GOLEM);
        assertThat(golem.getEffectivePower()).isEqualTo(3);
        assertThat(golem.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Golems you control have vigilance")
    void golemsHaveVigilance() {
        harness.addToBattlefield(player1, new VexyrIchTekiksHeir());
        harness.setLibrary(player1, List.of(new Island(), new GrizzlyBears()));
        harness.castFromHand(player1, new LurkerInTheDeep(), "{3}{U}{U}{U}");
        resolveAllTriggers();

        Permanent golem = findPermanent(player1, "Phyrexian Golem");
        Permanent lurker = findPermanent(player1, "Lurker in the Deep");
        assertThat(gqs.hasKeyword(gd, golem, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, lurker, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void doesNotCreateGolemWhenNoCardMatchesSeek() {
        harness.addToBattlefield(player1, new VexyrIchTekiksHeir());
        harness.setLibrary(player1, List.of(new Island()));
        harness.castFromHand(player1, new LurkerInTheDeep(), "{3}{U}{U}{U}");
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Phyrexian Golem")).isZero();
    }

    @Test
    void opponentsSeekDoesNotCreateGolem() {
        harness.addToBattlefield(player1, new VexyrIchTekiksHeir());
        harness.setLibrary(player2, List.of(new VexyrIchTekiksHeir()));
        harness.enterBattlefieldAndReturn(player2, new LurkerInTheDeep());
        resolveAllTriggers();

        harness.assertInHand(player2, "Vexyr, Ich-Tekik's Heir");
        assertThat(countPermanents(player1, "Phyrexian Golem")).isZero();
        assertThat(countPermanents(player2, "Phyrexian Golem")).isZero();
    }

    @Test
    void createsGolemForSeekDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new VexyrIchTekiksHeir());
        harness.setLibrary(player1, List.of(new VexyrIchTekiksHeir()));
        harness.forceActivePlayer(player2);
        harness.enterBattlefieldAndReturn(player1, new LurkerInTheDeep());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Phyrexian Golem")).isEqualTo(1);
    }

    @Test
    void golemLosesVigilanceWhenVexyrLeavesBattlefield() {
        Permanent vexyr = harness.addToBattlefieldAndReturn(player1, new VexyrIchTekiksHeir());
        harness.setLibrary(player1, List.of(new VexyrIchTekiksHeir()));
        harness.castFromHand(player1, new LurkerInTheDeep(), "{3}{U}{U}{U}");
        resolveAllTriggers();
        Permanent golem = findPermanent(player1, "Phyrexian Golem");
        assertThat(gqs.hasKeyword(gd, golem, Keyword.VIGILANCE)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, vexyr));

        assertThat(gqs.hasKeyword(gd, golem, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void createsGolemWhenNornsFetchlingSeeks() {
        harness.addToBattlefield(player1, new VexyrIchTekiksHeir());
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.setLibrary(player1, List.of(new VexyrIchTekiksHeir()));
        harness.castFromHand(player1, new NornsFetchling(), "{1}{W}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertInHand(player1, "Vexyr, Ich-Tekik's Heir");
        assertThat(countPermanents(player1, "Phyrexian Golem")).isEqualTo(1);
    }

    @Test
    void seekingTwoCardsTogetherCreatesOnlyOneGolem() {
        harness.addToBattlefield(player1, new VexyrIchTekiksHeir());
        harness.setLibrary(player1, List.of(new VexyrIchTekiksHeir(), new VexyrIchTekiksHeir()));
        harness.castFromHand(player1, new ChoiceOfFortunes(), "{2}{U}");
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(countPermanents(player1, "Phyrexian Golem")).isEqualTo(1);
    }

    @Test
    void createdGolemIsColorlessArtifactCreatureAndVigilanceOnlyAppliesToItsController() {
        harness.addToBattlefield(player1, new VexyrIchTekiksHeir());
        harness.setLibrary(player1, List.of(new VexyrIchTekiksHeir()));
        harness.castFromHand(player1, new LurkerInTheDeep(), "{3}{U}{U}{U}");
        resolveAllTriggers();
        Permanent golem = findPermanent(player1, "Phyrexian Golem");

        assertThat(golem.getCard().isToken()).isTrue();
        assertThat(golem.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(golem.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        assertThat(golem.getCard().getColors()).isEmpty();
        assertThat(golem.getCard().getColor()).isNull();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(golem);
        gd.playerBattlefields.get(player2.getId()).add(golem);

        assertThat(gqs.hasKeyword(gd, golem, Keyword.VIGILANCE)).isFalse();
    }
}
