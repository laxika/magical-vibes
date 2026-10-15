package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void unattachedCloakDoesNotGrantKeywords() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new CloakOfTheBat());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    void removingCloakRemovesBothKeywordGrants() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent cloak = harness.addToBattlefieldAndReturn(player1, new CloakOfTheBat());
        cloak.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(cloak);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    void equipCannotTargetAnOpponentsCreature() {
        Permanent cloak = harness.addToBattlefieldAndReturn(player1, new CloakOfTheBat());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(cloak.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipCannotBeActivatedOutsideMainPhase() {
        Permanent cloak = harness.addToBattlefieldAndReturn(player1, new CloakOfTheBat());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(cloak.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void equipRequiresTwoMana() {
        Permanent cloak = harness.addToBattlefieldAndReturn(player1, new CloakOfTheBat());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(cloak.isAttached()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void failedReequipLeavesCloakOnOriginalCreature() {
        Permanent cloak = harness.addToBattlefieldAndReturn(player1, new CloakOfTheBat());
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new GrizzlyBears());
        cloak.setAttachedTo(firstCreature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, secondCreature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(secondCreature);

        harness.passBothPriorities();

        assertThat(cloak.getAttachedTo()).isEqualTo(firstCreature.getId());
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, firstCreature, Keyword.HASTE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
