package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CloudsculptArmorer.class, GrizzlyBears.class, Plains.class, Shock.class})
class CloudsculptArmorerTest extends BaseCardTest {

    @Test
    void entersAndPutsShieldCounterOnTargetArtifactOrCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castArmorer(target);

        assertThat(target.getCounterCount(CounterType.SHIELD)).isEqualTo(1);
    }

    @Test
    void seeksANonlandCardWhenACounterIsRemovedFromYourPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card sought = new GrizzlyBears();
        Card land = new Plains();
        harness.setLibrary(player1, List.of(land, sought));
        castArmorer(target);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).contains(sought);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
    }

    @Test
    void doesNotTriggerWhenCountersAreRemovedFromAnOpponentsPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.SHIELD, 1);
        Card sought = new GrizzlyBears();
        harness.setLibrary(player1, List.of(sought));
        Permanent armorer = harness.addToBattlefieldAndReturn(player1, new CloudsculptArmorer());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.SHIELD)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(sought);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sought);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(armorer);
    }

    @Test
    void cannotTargetALandWithItsEnterTheBattlefieldAbility() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setHand(player1, List.of(new CloudsculptArmorer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castArmorer(Permanent target) {
        harness.setHand(player1, List.of(new CloudsculptArmorer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();
    }
}
