package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StarryEyedSkyrider.class, GrizzlyBears.class})
class StarryEyedSkyriderTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking grants flying to another creature you control until end of turn")
    void grantsFlyingToAnotherCreatureYouControl() {
        addCreatureReady(player1, new StarryEyedSkyrider());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, otherCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Attacking tokens you control have flying")
    void attackingTokensYouControlHaveFlying() {
        addCreatureReady(player1, new StarryEyedSkyrider());
        Permanent ownToken = addTokenCreature(player1);
        Permanent opponentToken = addTokenCreature(player2);
        ownToken.setAttacking(true);
        ownToken.setAttackTarget(player2.getId());
        opponentToken.setAttacking(true);
        opponentToken.setAttackTarget(player1.getId());

        assertThat(gqs.hasKeyword(gd, ownToken, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentToken, Keyword.FLYING)).isFalse();

        ownToken.setAttacking(false);
        assertThat(gqs.hasKeyword(gd, ownToken, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot target itself")
    void cannotTargetItself() {
        Permanent skyrider = addCreatureReady(player1, new StarryEyedSkyrider());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, skyrider.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addTokenCreature(Player player) {
        Card tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        return addCreatureReady(player, tokenCard);
    }
}
