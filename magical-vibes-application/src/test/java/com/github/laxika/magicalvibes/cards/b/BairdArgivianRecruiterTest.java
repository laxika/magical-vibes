package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BairdArgivianRecruiter.class, GrizzlyBears.class})
class BairdArgivianRecruiterTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Soldier token when you control a creature with greater power than base")
    void createsSoldierWhenYouControlModifiedCreature() {
        addBaird();
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.setPowerModifier(1);

        resolveEndStep();

        Permanent token = findToken();
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
    }

    @Test
    @DisplayName("Does not create a token when your creatures have only base power")
    void doesNotCreateSoldierWhenNoModifiedCreature() {
        addBaird();
        addCreatureReady(player1, new GrizzlyBears());

        resolveEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("An opponent's modified creature does not satisfy the condition")
    void opponentModifiedCreatureDoesNotSatisfyCondition() {
        addBaird();
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());
        opponentBear.setPowerModifier(1);

        resolveEndStep();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    private void addBaird() {
        harness.addToBattlefield(player1, new BairdArgivianRecruiter());
    }

    private Permanent findToken() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
    }

    private void resolveEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
