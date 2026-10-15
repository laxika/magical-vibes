package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MonasteryFlock.class, MistfireWeaver.class})
class MonasteryFlockTest extends BaseCardTest {

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUpForBlue() {
        harness.setHand(player1, List.of(new MonasteryFlock()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent flock = findPermanent(player1, "Monastery Flock");
        assertThat(flock.isFaceDown()).isTrue();
        assertThat(flock.getEffectivePower()).isEqualTo(2);
        assertThat(flock.getEffectiveToughness()).isEqualTo(2);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(flock));
        harness.passBothPriorities();

        assertThat(flock.isFaceDown()).isFalse();
        assertThat(flock.getEffectivePower()).isZero();
        assertThat(flock.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    void faceDownFlockCanAttackButCannotBlockAFlyerUntilTurnedFaceUp() {
        harness.setHand(player1, List.of(new MonasteryFlock()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent flock = findPermanent(player1, "Monastery Flock");
        flock.setSummoningSick(false);
        Permanent flyer = addCreatureReady(player2, new MistfireWeaver());
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());

        assertThat(als.canAttack(gd, flock, player1.getId())).isTrue();
        assertThat(bls.canBlockAttacker(gd, flock, flyer, battlefield)).isFalse();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, battlefield.indexOf(flock));

        assertThat(flock.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(als.canAttack(gd, flock, player1.getId())).isFalse();
        assertThat(bls.canBlockAttacker(gd, flock, flyer, battlefield)).isTrue();
    }

    @Test
    void morphCannotBePaidWithColorlessMana() {
        harness.setHand(player1, List.of(new MonasteryFlock()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();
        Permanent flock = findPermanent(player1, "Monastery Flock");
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(flock.isFaceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void castingFaceUpProducesADefenderThatCanBlockFlyers() {
        harness.setHand(player1, List.of(new MonasteryFlock()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent flock = findPermanent(player1, "Monastery Flock");
        flock.setSummoningSick(false);
        Permanent flyer = addCreatureReady(player2, new MistfireWeaver());

        assertThat(flock.isFaceDown()).isFalse();
        assertThat(als.canAttack(gd, flock, player1.getId())).isFalse();
        assertThat(bls.canBlockAttacker(gd, flock, flyer,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
    }
}
