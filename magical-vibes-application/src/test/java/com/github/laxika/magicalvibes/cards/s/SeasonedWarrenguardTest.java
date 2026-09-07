package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeasonedWarrenguard.class, GrizzlyBears.class})
class SeasonedWarrenguardTest extends BaseCardTest {

    @Test
    void getsPlusTwoPowerWhenAttackingWithToken() {
        Permanent warrenguard = addCreatureReady(player1, new SeasonedWarrenguard());
        harness.addToBattlefield(player1, createToken());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(warrenguard.getEffectivePower()).isEqualTo(3);
        assertThat(warrenguard.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void doesNotGetBoostWhenOpponentControlsTheToken() {
        Permanent warrenguard = addCreatureReady(player1, new SeasonedWarrenguard());
        harness.addToBattlefield(player2, createToken());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(warrenguard.getEffectivePower()).isEqualTo(1);
    }

    @Test
    void doesNotGetBoostIfTokenIsLostBeforeTriggerResolves() {
        Permanent warrenguard = addCreatureReady(player1, new SeasonedWarrenguard());
        Permanent token = harness.addToBattlefieldAndReturn(player1, createToken());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(token);
        resolveAllTriggers();

        assertThat(warrenguard.getEffectivePower()).isEqualTo(1);
    }

    @Test
    void boostWearsOffAtEndOfTurn() {
        Permanent warrenguard = addCreatureReady(player1, new SeasonedWarrenguard());
        harness.addToBattlefield(player1, createToken());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(warrenguard.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(warrenguard.getEffectivePower()).isEqualTo(1);
    }

    private static Card createToken() {
        Card card = new Card();
        card.setName("Rabbit Token");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
