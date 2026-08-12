package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.w.WallOfAir;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KinTreeInvocationTest extends BaseCardTest {

    private List<Permanent> spiritWarriors() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Spirit Warrior"))
                .toList();
    }

    @Test
    void createsOneTokenWithPowerAndToughnessEqualToGreatestControlledToughness() {
        harness.setHand(player1, List.of(new KinTreeInvocation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addToBattlefield(player1, new WallOfAir());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(spiritWarriors()).singleElement()
                .satisfies(token -> assertThat(token.getCard().getPower()).isEqualTo(5))
                .satisfies(token -> assertThat(token.getCard().getToughness()).isEqualTo(5));
    }

    @Test
    void opponentCreaturesDoNotCount() {
        harness.setHand(player1, List.of(new KinTreeInvocation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addToBattlefield(player2, new WallOfAir());

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(spiritWarriors()).isEmpty();
    }
}
