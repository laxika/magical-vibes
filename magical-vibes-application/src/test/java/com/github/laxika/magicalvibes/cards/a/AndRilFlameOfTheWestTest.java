package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AndRilFlameOfTheWest.class, GrizzlyBears.class, IsamaruHoundOfKonda.class})
class AndRilFlameOfTheWestTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsPlusThreePlusOne() {
        Permanent anduril = addAnduril();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        anduril.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void attackingWithNonlegendaryCreatureCreatesUntappedNonattackingSpirits() {
        Permanent anduril = addAnduril();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        anduril.setAttachedTo(creature.getId());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        List<Permanent> spirits = findPermanents(player1, "Spirit");
        assertThat(spirits).hasSize(2);
        assertThat(spirits).allSatisfy(spirit -> {
            assertThat(spirit.isTapped()).isFalse();
            assertThat(spirit.isAttackedThisTurn()).isFalse();
        });
    }

    @Test
    void attackingWithLegendaryCreatureCreatesTappedAttackingSpirits() {
        Permanent anduril = addAnduril();
        Permanent creature = addCreatureReady(player1, new IsamaruHoundOfKonda());
        anduril.setAttachedTo(creature.getId());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        List<Permanent> spirits = findPermanents(player1, "Spirit");
        assertThat(spirits).hasSize(2);
        assertThat(spirits).allSatisfy(spirit -> {
            assertThat(spirit.isTapped()).isTrue();
            assertThat(spirit.isAttackedThisTurn()).isTrue();
        });
    }

    private Permanent addAnduril() {
        return harness.addToBattlefieldAndReturn(player1, new AndRilFlameOfTheWest());
    }
}
