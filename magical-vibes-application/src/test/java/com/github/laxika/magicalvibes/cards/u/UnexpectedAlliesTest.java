package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnexpectedAllies.class, GrizzlyBears.class})
class UnexpectedAlliesTest extends BaseCardTest {

    @Test
    void boostsTargetAndGrantsDoubleTeamWithoutFirstStrikeForAnUnmatchedName() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(target);

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_TEAM)).isTrue();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void grantsFirstStrikeWhenAnotherControlledCreatureSharesTheName() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        cast(target);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void grantsFirstStrikeWhenACreatureCardWithTheSameNameIsInTheGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        cast(target);

        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void effectsWearOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(target);

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.DOUBLE_TEAM)).isFalse();
    }

    @Test
    void canTargetOnlyNontokenCreaturesYouControl() {
        Card token = new Card();
        token.setName("Soldier Token");
        token.setType(CardType.CREATURE);
        token.setColor(CardColor.WHITE);
        token.setPower(1);
        token.setToughness(1);
        token.setToken(true);
        Permanent tokenPermanent = harness.addToBattlefieldAndReturn(player1, token);
        harness.setHand(player1, List.of(new UnexpectedAllies()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, tokenPermanent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nontoken creature you control");
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new UnexpectedAllies()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
