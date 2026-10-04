package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.c.CloudkinSeer;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HowlingGiant.class, CloudkinSeer.class, Murder.class})
class HowlingGiantTest extends BaseCardTest {

    @Test
    void enteringBattlefieldCreatesTwoWolfTokens() {
        harness.castFromHand(player1, new HowlingGiant(), "{5}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> wolves = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.WOLF))
                .toList();

        assertThat(wolves).hasSize(2).allSatisfy(wolf -> {
            assertThat(wolf.getCard().getName()).isEqualTo("Wolf");
            assertThat(wolf.getCard().getPower()).isEqualTo(2);
            assertThat(wolf.getCard().getToughness()).isEqualTo(2);
            assertThat(wolf.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(wolf.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(wolf.isTapped()).isFalse();
            assertThat(wolf.isSummoningSick()).isTrue();
        });
        assertThat(countPermanents(player2, "Wolf")).isZero();
    }

    @Test
    void enteringWithoutBeingCastCreatesWolvesForItsController() {
        harness.enterBattlefieldAndReturn(player2, new HowlingGiant());

        assertThat(countPermanents(player2, "Wolf")).isZero();
        resolveAllTriggers();

        assertThat(countPermanents(player2, "Wolf")).isEqualTo(2);
        assertThat(countPermanents(player1, "Wolf")).isZero();
    }

    @Test
    void entryTriggerCreatesWolvesEvenAfterGiantIsDestroyed() {
        harness.castFromHand(player1, new HowlingGiant(), "{5}{G}{G}");
        harness.passBothPriorities();
        Permanent giant = findPermanent(player1, "Howling Giant");
        assertThat(countPermanents(player1, "Wolf")).isZero();

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, giant.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Howling Giant")).isZero();
        assertThat(countPermanents(player1, "Wolf")).isZero();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Wolf")).isEqualTo(2);
    }

    @Test
    void reachAllowsBlockingFlyingCreature() {
        addCreatureReady(player1, new CloudkinSeer());
        Permanent giant = addCreatureReady(player2, new HowlingGiant());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(giant);
        assertThat(countPermanents(player1, "Cloudkin Seer")).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
