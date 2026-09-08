package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Twinstrike.class, GrizzlyBears.class, GiantSpider.class, HillGiant.class, GloriousAnthem.class})
class TwinstrikeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to each target creature when the controller has cards in hand")
    void dealsDamageWithCardsInHand() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new GrizzlyBears(), new Twinstrike()));
        giveMana();

        harness.castInstant(player1, 1, List.of(spider.getId(), giant.getId()));
        harness.passBothPriorities();

        assertThat(spider.getMarkedDamage()).isEqualTo(2);
        assertThat(giant.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Giant Spider");
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Destroys both target creatures with an empty hand")
    void destroysTargetsWithEmptyHand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new Twinstrike()));
        giveMana();

        harness.castInstant(player1, 0, List.of(
                harness.getPermanentId(player2, "Grizzly Bears"),
                harness.getPermanentId(player2, "Hill Giant")));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    @DisplayName("Requires exactly two creature targets")
    void rejectsWrongTargetCountAndType() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Twinstrike()));
        giveMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.addToBattlefield(player2, new GloriousAnthem());
        UUID anthemId = harness.getPermanentId(player2, "Glorious Anthem");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(bears.getId(), anthemId)))
                .isInstanceOf(IllegalStateException.class);
    }

    private void giveMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
