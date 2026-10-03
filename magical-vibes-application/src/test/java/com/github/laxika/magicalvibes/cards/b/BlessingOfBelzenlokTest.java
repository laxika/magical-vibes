package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.cards.y.YargleGluttonOfUrborg;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlessingOfBelzenlok.class, PrimordialWurm.class, YargleGluttonOfUrborg.class})
class BlessingOfBelzenlokTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving on non-legendary creature gives +2/+1 but NOT lifelink")
    void nonLegendaryGetsBoostButNotLifelink() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        harness.setHand(player1, List.of(new BlessingOfBelzenlok()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
        assertThat(target.getEffectivePower()).isEqualTo(9);
        assertThat(target.getEffectiveToughness()).isEqualTo(7);
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Resolving on legendary creature gives +2/+1 AND lifelink")
    void legendaryGetsBoostAndLifelink() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new YargleGluttonOfUrborg());
        harness.setHand(player1, List.of(new BlessingOfBelzenlok()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(1);
        assertThat(target.getEffectivePower()).isEqualTo(11);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Boost and lifelink wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new YargleGluttonOfUrborg());
        harness.setHand(player1, List.of(new BlessingOfBelzenlok()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        harness.setHand(player1, List.of(new BlessingOfBelzenlok()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Blessing of Belzenlok");
    }

    @Test
    @DisplayName("An opponent's legendary creature receives both effects")
    void canTargetOpponentsLegendaryCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YargleGluttonOfUrborg());
        harness.setHand(player1, List.of(new BlessingOfBelzenlok()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(11);
        assertThat(target.getEffectiveToughness()).isEqualTo(4);
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isTrue();
        harness.assertInGraveyard(player1, "Blessing of Belzenlok");
    }

    @Test
    @DisplayName("Two blessings stack their boosts and both expire at cleanup")
    void multipleBlessingsStackAndExpire() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new YargleGluttonOfUrborg());
        harness.setHand(player1, List.of(new BlessingOfBelzenlok(), new BlessingOfBelzenlok()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.hasKeyword(Keyword.LIFELINK)).isFalse();
    }
}
