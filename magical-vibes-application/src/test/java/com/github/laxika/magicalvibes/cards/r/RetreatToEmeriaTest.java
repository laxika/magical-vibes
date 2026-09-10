package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RetreatToEmeria.class, Forest.class, GrizzlyBears.class})
class RetreatToEmeriaTest extends BaseCardTest {

    private static final String CREATE_TOKEN = "Create a 1/1 white Kor Ally creature token.";
    private static final String BOOST_CREATURES = "Creatures you control get +1/+1 until end of turn.";

    @Test
    void landfallCreatesKorAllyToken() {
        harness.addToBattlefield(player1, new RetreatToEmeria());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handleListChoice(player1, CREATE_TOKEN);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Kor Ally"))
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.KOR, CardSubtype.ALLY);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    void landfallBoostsCreaturesYouControlUntilEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new RetreatToEmeria());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handleListChoice(player1, BOOST_CREATURES);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    void landfallBoostWearsOffAtEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new RetreatToEmeria());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.handleListChoice(player1, BOOST_CREATURES);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
    }

    @Test
    void opponentLandDoesNotTriggerLandfall() {
        harness.addToBattlefield(player1, new RetreatToEmeria());
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);

        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count()).isZero();
    }
}
