package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlluringSuitor.class, GrizzlyBears.class})
class AlluringSuitorTest extends BaseCardTest {

    @Test
    void transformsAndAddsPersistentRedManaWhenExactlyTwoCreaturesAttack() {
        Permanent suitor = addCreatureReady(player1, new AlluringSuitor());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(suitor.isTransformed()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getPersistentMana(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    void doesNotTransformWhenMoreThanTwoCreaturesAttack() {
        Permanent suitor = addCreatureReady(player1, new AlluringSuitor());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1, 2, 3));

        assertThat(suitor.isTransformed()).isFalse();
    }

    @Test
    void deadlyDancerBoostsItselfAndAnotherCreature() {
        Permanent dancer = addTransformedDancer(player1);
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, other.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dancer)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(3);
    }

    @Test
    void deadlyDancerCannotTargetItself() {
        Permanent dancer = addTransformedDancer(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, dancer.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addTransformedDancer(Player player) {
        AlluringSuitor card = new AlluringSuitor();
        Permanent dancer = new Permanent(card);
        dancer.setSummoningSick(false);
        dancer.setCard(card.getBackFaceCard());
        dancer.setTransformed(true);
        gd.playerBattlefields.get(player.getId()).add(dancer);
        return dancer;
    }
}
