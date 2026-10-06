package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OgreBerserker.class})
class OgreBerserkerTest extends BaseCardTest {

    @Test
    void canAttackTheTurnItResolves() {
        OgreBerserker card = new OgreBerserker();
        harness.castFromHand(player1, card, "{4}{R}");
        harness.passBothPriorities();

        Permanent berserker = findPermanent(player1, "Ogre Berserker");
        assertThat(berserker.isSummoningSick()).isTrue();

        declareAttackers(List.of(0));

        assertThat(berserker.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    void hasteDoesNotAllowAttackingWhileTapped() {
        harness.castFromHand(player1, new OgreBerserker(), "{4}{R}");
        harness.passBothPriorities();
        Permanent berserker = findPermanent(player1, "Ogre Berserker");
        berserker.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
