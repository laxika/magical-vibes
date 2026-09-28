package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NagaOracle;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EyeOfDuskmantle.class, NagaOracle.class, Shock.class, Forest.class, GrizzlyBears.class})
class EyeOfDuskmantleTest extends BaseCardTest {

    private Card[] surveilThree(Card cardToGraveyard) {
        Card top1 = new GrizzlyBears();
        Card top2 = new GrizzlyBears();
        harness.setLibrary(player1, List.of(cardToGraveyard, top1, top2));
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new NagaOracle()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1, 2)));
        harness.clearPriorityPassed();
        return new Card[]{cardToGraveyard, top1, top2};
    }

    @Test
    @DisplayName("casts a card surveilled this turn by paying life equal to its mana value")
    void castsSurveilledSpellByPayingLife() {
        var eye = harness.addToBattlefieldAndReturn(player1, new EyeOfDuskmantle());
        Shock shock = new Shock();
        surveilThree(shock);

        harness.castFromGraveyardTargeting(player1, 0, eye.getId());

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("cannot cast a graveyard card that was not surveilled this turn")
    void cannotCastUnsurveilledCard() {
        harness.addToBattlefield(player1, new EyeOfDuskmantle());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyardTargeting(player1, 0,
                harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("can play a land surveilled this turn")
    void playsSurveilledLand() {
        harness.addToBattlefield(player1, new EyeOfDuskmantle());
        Forest forest = new Forest();
        surveilThree(forest);

        harness.playGraveyardLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Forest"));
    }
}
