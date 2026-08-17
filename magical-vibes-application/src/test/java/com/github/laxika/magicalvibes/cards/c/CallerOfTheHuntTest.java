package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CallerOfTheHuntTest extends BaseCardTest {

    @Test
    void countsCreaturesOfTheChosenTypeAcrossTheBattlefield() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CallerOfTheHunt()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreatureWithChosenType(player1, 0, CardSubtype.BEAR);
        harness.passBothPriorities();

        Permanent caller = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof CallerOfTheHunt)
                .findFirst()
                .orElseThrow();
        assertThat(gqs.getEffectivePower(gd, caller)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, caller)).isEqualTo(2);

        harness.addToBattlefield(player2, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, caller)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, caller)).isEqualTo(3);
    }

    @Test
    void cannotBeCastWithoutChoosingAType() {
        harness.setHand(player1, List.of(new CallerOfTheHunt()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
