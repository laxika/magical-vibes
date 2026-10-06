package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.c.CoastlineChimera;
import com.github.laxika.magicalvibes.cards.d.DauntlessOnslaught;
import com.github.laxika.magicalvibes.cards.g.Griptide;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShreddingWinds.class, BronzeSable.class,
        CoastlineChimera.class, DauntlessOnslaught.class, Griptide.class})
class ShreddingWindsTest extends BaseCardTest {

    @Test
    void dealsSevenDamageToTargetCreatureWithFlying() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CoastlineChimera());
        harness.setHand(player1, List.of(new ShreddingWinds()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
    }

    @Test
    void cannotTargetCreatureWithoutFlying() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BronzeSable());
        harness.setHand(player1, List.of(new ShreddingWinds()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void dealsExactlySevenDamageToOwnFlyingCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CoastlineChimera());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new CoastlineChimera());
        harness.setHand(player1, List.of(new DauntlessOnslaught(), new DauntlessOnslaught(),
                new ShreddingWinds()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player1, 0, List.of(target.getId()));
        harness.castAndResolveInstant(player1, 0, List.of(target.getId()));
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.getMarkedDamage()).isEqualTo(7);
        assertThat(other.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Shredding Winds");
    }

    @Test
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new ShreddingWinds()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNotDealDamageWhenTargetLeavesBattlefieldBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CoastlineChimera());
        harness.setHand(player1, List.of(new ShreddingWinds(), new Griptide()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertNotOnBattlefield(player2, "Coastline Chimera");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(target.getCard());
        harness.assertInGraveyard(player1, "Shredding Winds");
        assertThat(gd.stack).isEmpty();
    }
}
