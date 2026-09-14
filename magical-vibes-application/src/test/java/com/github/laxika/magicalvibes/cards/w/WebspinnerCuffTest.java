package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WebspinnerCuff.class, GrizzlyBears.class})
class WebspinnerCuffTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsBoostAndReach() {
        Permanent cuff = addReadyCuff();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        cuff.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
        assertThat(gqs.isCreature(gd, cuff)).isFalse();
    }

    @Test
    void reconfigureAttachesAndUnattachesTheCuff() {
        Permanent cuff = addReadyCuff();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
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
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();
    }

    @Test
    void reconfigureCannotTargetAnOpponentsCreature() {
        Permanent cuff = addReadyCuff();
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        addReconfigureMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(cuff.getAttachedTo()).isNull();
    }

    private Permanent addReadyCuff() {
        Permanent cuff = new Permanent(new WebspinnerCuff());
        cuff.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(cuff);
        return cuff;
    }

    private void addReconfigureMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
