package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.h.HexParasite;
import com.github.laxika.magicalvibes.cards.p.PsychicSurgery;
import com.github.laxika.magicalvibes.cards.t.TriumphOfTheHordes;
import com.github.laxika.magicalvibes.cards.w.WarReport;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BucketList.class, DarksteelRelic.class, BlightedAgent.class, PsychicSurgery.class,
        TriumphOfTheHordes.class, WarReport.class, HexParasite.class})
class BucketListTest extends BaseCardTest {

    @Test
    void castingTheFirstSpellOfATypeMarksItAndDraws() {
        Permanent bucketList = harness.addToBattlefieldAndReturn(player1, new BucketList());

        castAndResolve(new DarksteelRelic(), "{0}");

        assertThat(bucketList.getCounters()).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void castingTheSameTypeAgainDoesNotAddAnotherMarkerOrDraw() {
        harness.addToBattlefield(player1, new BucketList());

        castAndResolve(new DarksteelRelic(), "{0}");
        castAndResolve(new DarksteelRelic(), "{0}");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void completingAllFiveTypesSacrificesBucketListAndDrawsAgain() {
        Permanent bucketList = harness.addToBattlefieldAndReturn(player1, new BucketList());

        castAndResolve(new DarksteelRelic(), "{0}");
        castAndResolve(new BlightedAgent(), "{1}{U}");
        castAndResolve(new PsychicSurgery(), "{1}{U}");
        castAndResolve(new WarReport(), "{3}{W}");
        castAndResolve(new TriumphOfTheHordes(), "{2}{G}{G}");

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bucketList);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bucketList.getCard());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    void aMultitypeSpellMarksBothTypesButDrawsOnlyOnce() {
        harness.addToBattlefield(player1, new BucketList());

        castAndResolve(new HexParasite(), "{1}");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        castAndResolve(new DarksteelRelic(), "{0}");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        castAndResolve(new BlightedAgent(), "{1}{U}");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void completingTwoRemainingTypesWithOneSpellDrawsTwoCardsAndSacrifices() {
        Permanent bucketList = harness.addToBattlefieldAndReturn(player1, new BucketList());
        castAndResolve(new PsychicSurgery(), "{1}{U}");
        castAndResolve(new WarReport(), "{3}{W}");
        castAndResolve(new TriumphOfTheHordes(), "{2}{G}{G}");

        castAndResolve(new HexParasite(), "{1}");

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bucketList);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bucketList.getCard());
    }

    @Test
    void hexParasiteCannotRemoveTrackerMarkersOrAllowAnotherDrawForTheSameType() {
        Permanent bucketList = harness.addToBattlefieldAndReturn(player1, new BucketList());
        Permanent parasite = harness.addToBattlefieldAndReturn(player1, new HexParasite());
        castAndResolve(new DarksteelRelic(), "{0}");
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 1, 1, bucketList.getId());
        resolveAllTriggers();

        assertThat(parasite.getPowerModifier()).isZero();
        castAndResolve(new DarksteelRelic(), "{0}");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void anOpponentCastingASpellDoesNotAdvanceTheTrackerOrDraw() {
        harness.addToBattlefield(player1, new BucketList());
        harness.setHand(player1, List.of());

        harness.castFromHand(player2, new WarReport(), "{3}{W}");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        castAndResolve(new WarReport(), "{3}{W}");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void puttingAPermanentOntoTheBattlefieldDoesNotAdvanceTheTracker() {
        harness.addToBattlefield(player1, new BucketList());
        harness.setHand(player1, List.of());

        harness.enterBattlefieldAndReturn(player1, new DarksteelRelic());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        castAndResolve(new DarksteelRelic(), "{0}");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void castAndResolve(Card spell, String manaCost) {
        harness.castFromHand(player1, spell, manaCost);
        resolveAllTriggers();
    }
}
