package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BladeOfTheOni.class, GrizzlyBears.class})
class BladeOfTheOniTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsBladeOfTheOnisStaticEffects() {
        Permanent blade = addReadyBlade();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        blade.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.MENACE)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, creature)).contains(CardColor.BLACK);
        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).contains(CardSubtype.DEMON);
        assertThat(gqs.isCreature(gd, blade)).isFalse();
    }

    @Test
    void reconfigureAttachesAndUnattachesTheBlade() {
        Permanent blade = addReadyBlade();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addReconfigureMana();

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.isCreature(gd, blade)).isFalse();

        addReconfigureMana();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, blade)).isTrue();
    }

    @Test
    void reconfigureCannotTargetAnOpponentsCreature() {
        Permanent blade = addReadyBlade();
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        addReconfigureMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blade.getAttachedTo()).isNull();
    }

    private Permanent addReadyBlade() {
        Permanent blade = new Permanent(new BladeOfTheOni());
        blade.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(blade);
        return blade;
    }

    private void addReconfigureMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
