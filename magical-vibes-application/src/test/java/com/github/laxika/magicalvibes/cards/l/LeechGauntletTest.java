package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LeechGauntlet.class, GrizzlyBears.class})
class LeechGauntletTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsLifelink() {
        Permanent gauntlet = addCreatureReady(player1, new LeechGauntlet());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        gauntlet.setAttachedTo(creature.getId());

        assertThat(gqs.isCreature(gd, gauntlet)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void reconfigureAttachesAndUnattachesTheGauntlet() {
        Permanent gauntlet = addCreatureReady(player1, new LeechGauntlet());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gauntlet.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.isCreature(gd, gauntlet)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gauntlet.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, gauntlet)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    void reconfigureCannotTargetAnOpponentsCreature() {
        Permanent gauntlet = addCreatureReady(player1, new LeechGauntlet());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gauntlet.getAttachedTo()).isNull();
    }
}
