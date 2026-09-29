package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloakOfTheBat.class, GrizzlyBears.class})
class CloakOfTheBatTest extends BaseCardTest {

    @Test
    void equippedCreatureHasFlyingAndHaste() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent cloak = harness.addToBattlefieldAndReturn(player1, new CloakOfTheBat());
        cloak.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
    }

    @Test
    void equipMovesTheKeywordGrantsToAnotherCreature() {
        Permanent cloak = harness.addToBattlefieldAndReturn(player1, new CloakOfTheBat());
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());
        cloak.setAttachedTo(firstCreature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, secondCreature, Keyword.HASTE)).isTrue();
    }
}
