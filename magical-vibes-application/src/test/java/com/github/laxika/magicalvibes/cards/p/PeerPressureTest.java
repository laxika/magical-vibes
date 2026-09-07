package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PeerPressure.class, AvianChangeling.class, GrizzlyBears.class, HillGiant.class})
class PeerPressureTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control of all creatures of the chosen type when you have the most")
    void gainsControlWhenControllerHasMoreOfChosenType() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opposingGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        castAndChoose("BEAR");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownBear, opposingBear);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .doesNotContain(opposingBear)
                .contains(opposingGiant);
    }

    @Test
    @DisplayName("Does nothing when the chosen type is tied")
    void doesNothingOnTie() {
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        castAndChoose("BEAR");

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingBear);
    }

    @Test
    @DisplayName("Does nothing when an opponent controls more of the chosen type")
    void doesNothingWhenOpponentHasMore() {
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        castAndChoose("BEAR");

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingBear);
    }

    @Test
    @DisplayName("Counts a changeling as the chosen creature type")
    void changelingCountsAsChosenType() {
        harness.addToBattlefield(player1, new AvianChangeling());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndChoose("BEAR");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opposingBear);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingBear);
    }

    private void castAndChoose(String creatureType) {
        harness.setHand(player1, List.of(new PeerPressure()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, creatureType);
    }
}
