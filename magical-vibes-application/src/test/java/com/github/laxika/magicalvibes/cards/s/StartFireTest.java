package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StartFire.class, GrizzlyBears.class})
class StartFireTest extends BaseCardTest {

    @Test
    void startCreatesTwoVigilantWarriors() {
        harness.setHand(player1, List.of(new StartFire()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        List<Permanent> warriors = findPermanents(player1, "Warrior").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(warriors).hasSize(2);
        assertThat(warriors).allSatisfy(warrior -> {
            assertThat(warrior.getCard().getPower()).isEqualTo(1);
            assertThat(warrior.getCard().getToughness()).isEqualTo(1);
            assertThat(gqs.hasKeyword(gd, warrior, Keyword.VIGILANCE)).isTrue();
        });
    }

    @Test
    void fireDividesTwoDamageAmongTwoTargets() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int lifeBefore = gd.getLife(player2.getId());

        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new StartFire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gs.playCard(gd, player1, 0, 1, null,
                Map.of(player2.getId(), 1, bears.getId(), 1),
                List.of(player2.getId(), bears.getId()), List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void fireCanDealBothDamageToOneTarget() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new StartFire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.getLife(player2.getId());
        gs.playCard(gd, player1, 0, 1, null,
                Map.of(player2.getId(), 2), List.of(player2.getId()), List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
    }
}
