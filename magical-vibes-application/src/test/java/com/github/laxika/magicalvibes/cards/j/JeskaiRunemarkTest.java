package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JeskaiRunemarkTest extends BaseCardTest {

    @Test
    void enchantedCreatureGetsPlusTwoPlusTwo() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addAura(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    void enchantedCreatureHasFlyingWhileControllerControlsRedPermanent() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addAura(bears);
        harness.addToBattlefield(player1, new GoblinPiker());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    void enchantedCreatureHasFlyingWhileControllerControlsWhitePermanent() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addAura(bears);
        harness.addToBattlefield(player1, new EliteVanguard());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    void enchantedCreatureDoesNotHaveFlyingWithoutRedOrWhitePermanent() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addAura(bears);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    void flyingStopsWhenRedOrWhitePermanentLeaves() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        addAura(bears);
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(goblin);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    private Permanent addAura(Permanent enchantedCreature) {
        Permanent aura = new Permanent(new JeskaiRunemark());
        aura.setAttachedTo(enchantedCreature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }
}
