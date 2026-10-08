package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BearerOfMemory;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WebspinnerCuff.class, BearerOfMemory.class})
class WebspinnerCuffTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsBoostAndReach() {
        Permanent cuff = addReadyCuff();
        Permanent creature = addCreatureReady(player1, new BearerOfMemory());
        cuff.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
        assertThat(gqs.isCreature(gd, cuff)).isFalse();
    }

    @Test
    void reconfigureAttachesAndUnattachesTheCuff() {
        Permanent cuff = addReadyCuff();
        Permanent creature = addCreatureReady(player1, new BearerOfMemory());
        addReconfigureMana();

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(cuff.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.isCreature(gd, cuff)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();

        addReconfigureMana();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(cuff.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, cuff)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();
    }

    @Test
    void reconfigureCannotTargetAnOpponentsCreature() {
        Permanent cuff = addReadyCuff();
        Permanent opponentCreature = addCreatureReady(player2, new BearerOfMemory());
        addReconfigureMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(cuff.getAttachedTo()).isNull();
    }

    @Test
    void reconfigureCannotTargetItself() {
        Permanent cuff = addReadyCuff();
        addReconfigureMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, cuff.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(cuff.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void unattachCannotBeActivatedWhileUnattached() {
        Permanent cuff = addReadyCuff();
        addReconfigureMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(cuff.getAttachedTo()).isNull();
    }

    @Test
    void bothReconfigureAbilitiesRequireSorceryTiming() {
        Permanent cuff = addReadyCuff();
        Permanent creature = addCreatureReady(player1, new BearerOfMemory());
        addReconfigureMana();
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        cuff.setAttachedTo(creature.getId());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(cuff.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void reconfigureMovesDirectlyBetweenCreatures() {
        Permanent cuff = addReadyCuff();
        Permanent first = addCreatureReady(player1, new BearerOfMemory());
        Permanent second = addCreatureReady(player1, new BearerOfMemory());
        cuff.setAttachedTo(first.getId());
        addReconfigureMana();

        harness.activateAbility(player1, 0, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(cuff.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.isCreature(gd, cuff)).isFalse();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.REACH)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, second, Keyword.REACH)).isTrue();
    }

    @Test
    void reconfigureCanBeActivatedWithSummoningSickness() {
        Permanent cuff = harness.addToBattlefieldAndReturn(player1, new WebspinnerCuff());
        Permanent creature = addCreatureReady(player1, new BearerOfMemory());
        addReconfigureMana();

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(cuff.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.isCreature(gd, cuff)).isFalse();
    }

    private Permanent addReadyCuff() {
        return addCreatureReady(player1, new WebspinnerCuff());
    }

    private void addReconfigureMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
