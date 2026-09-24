package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IceFangCoatl.class, SnowCoveredForest.class, GrizzlyBears.class})
class IceFangCoatlTest extends BaseCardTest {

    @Test
    void enteringTheBattlefieldDrawsACard() {
        harness.setHand(player1, List.of(new IceFangCoatl()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void gainsDeathtouchWithAtLeastThreeOtherSnowPermanents() {
        Permanent coatl = harness.addToBattlefieldAndReturn(player1, new IceFangCoatl());

        assertThat(gqs.hasKeyword(gd, coatl, Keyword.DEATHTOUCH)).isFalse();

        addSnowPermanents(player1, 3);

        assertThat(gqs.hasKeyword(gd, coatl, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void losesDeathtouchWhenFewerThanThreeOtherSnowPermanentsRemain() {
        Permanent coatl = harness.addToBattlefieldAndReturn(player1, new IceFangCoatl());
        addSnowPermanents(player1, 3);
        List<Permanent> snowPermanents = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SnowCoveredForest)
                .toList();

        assertThat(gqs.hasKeyword(gd, coatl, Keyword.DEATHTOUCH)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(snowPermanents.getFirst());

        assertThat(gqs.hasKeyword(gd, coatl, Keyword.DEATHTOUCH)).isFalse();
    }

    private void addSnowPermanents(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new SnowCoveredForest());
        }
    }
}
