package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FancyFootwork.class, GrizzlyBears.class, Mountain.class})
class FancyFootworkTest extends BaseCardTest {

    @Test
    @DisplayName("Both target creatures untap and get +2/+2")
    void twoTargetsUntapAndBoost() {
        Permanent first = addTappedCreature();
        Permanent second = addTappedCreature();

        cast(first, second);

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    @Test
    @DisplayName("May target only one creature")
    void singleTargetAllowed() {
        Permanent creature = addTappedCreature();

        cast(creature);

        assertThat(creature.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void wearsOff() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);

        creature.resetModifiers();
        gd.expireEndOfTurnFloatingEffects();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a non-creature")
    void cannotTargetNonCreature() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new FancyFootwork()));
        addMana();

        UUID mountainId = mountain.getId();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(mountainId)))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addTappedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();
        return creature;
    }

    private void cast(Permanent... targets) {
        harness.setHand(player1, List.of(new FancyFootwork()));
        addMana();
        List<UUID> targetIds = targets.length == 1
                ? List.of(targets[0].getId())
                : List.of(targets[0].getId(), targets[1].getId());
        harness.castInstant(player1, 0, targetIds);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
