package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BronzeplateBoar.class, GrizzlyBears.class})
class BronzeplateBoarTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsBoostAndTrample() {
        Permanent boar = addReadyBoar();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        boar.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.isCreature(gd, boar)).isFalse();
    }

    @Test
    void reconfigureAttachesAndUnattachesTheBoar() {
        Permanent boar = addReadyBoar();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addReconfigureMana();

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(boar.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.isCreature(gd, boar)).isFalse();

        addReconfigureMana();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(boar.getAttachedTo()).isNull();
        assertThat(gqs.isCreature(gd, boar)).isTrue();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void reconfigureCannotTargetAnOpponentsCreature() {
        Permanent boar = addReadyBoar();
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        addReconfigureMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(boar.getAttachedTo()).isNull();
    }

    private Permanent addReadyBoar() {
        Permanent boar = new Permanent(new BronzeplateBoar());
        boar.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(boar);
        return boar;
    }

    private void addReconfigureMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
