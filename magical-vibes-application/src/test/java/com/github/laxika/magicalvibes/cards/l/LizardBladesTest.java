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

@CardUsed({LizardBlades.class, GrizzlyBears.class})
class LizardBladesTest extends BaseCardTest {

    @Test
    void equippedCreatureGainsDoubleStrike() {
        Permanent blades = addReadyBlades();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        blades.setAttachedTo(creature.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.isCreature(gd, blades)).isFalse();
    }

    @Test
    void reconfigureAttachesAndUnattachesTheBlades() {
        Permanent blades = addReadyBlades();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addReconfigureMana();

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(blades.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.isCreature(gd, blades)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isTrue();

        addReconfigureMana();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(blades.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, blades)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void reconfigureCannotTargetAnOpponentsCreature() {
        Permanent blades = addReadyBlades();
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        addReconfigureMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blades.getAttachedTo()).isNull();
    }

    private Permanent addReadyBlades() {
        Permanent blades = new Permanent(new LizardBlades());
        blades.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(blades);
        return blades;
    }

    private void addReconfigureMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
