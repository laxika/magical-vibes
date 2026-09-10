package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScuteSwarm.class, Forest.class})
class ScuteSwarmTest extends BaseCardTest {

    @Test
    void landfallCreatesInsectBelowSixLands() {
        harness.addToBattlefield(player1, new ScuteSwarm());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getName()).isEqualTo("Insect");
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.INSECT);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
    }

    @Test
    void landfallCreatesScuteSwarmCopyAtSixLands() {
        for (int i = 0; i < 5; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.addToBattlefield(player1, new ScuteSwarm());
        harness.setHand(player1, List.of(new Forest(), new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(scuteSwarmTokenCount()).isEqualTo(1);

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(scuteSwarmTokenCount()).isEqualTo(3);
    }

    private long scuteSwarmTokenCount() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Scute Swarm"))
                .count();
    }
}
