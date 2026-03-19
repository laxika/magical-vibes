package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MirriCatWarrior;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BlessingOfBelzenlokTest extends BaseCardTest {

    // ===== Boost applies to any creature =====

    @Test
    @DisplayName("Resolving on non-legendary creature gives +2/+1 but NOT lifelink")
    void nonLegendaryGetsBoostButNotLifelink() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlessingOfBelzenlok()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent bears = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.getToughnessModifier()).isEqualTo(1);
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
        assertThat(bears.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    // ===== Legendary creature gets lifelink =====

    @Test
    @DisplayName("Resolving on legendary creature gives +2/+1 AND lifelink")
    void legendaryGetsBoostAndLifelink() {
        harness.addToBattlefield(player1, new MirriCatWarrior());
        harness.setHand(player1, List.of(new BlessingOfBelzenlok()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player1, "Mirri, Cat Warrior");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        Permanent mirri = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(mirri.getPowerModifier()).isEqualTo(2);
        assertThat(mirri.getToughnessModifier()).isEqualTo(1);
        assertThat(mirri.getEffectivePower()).isEqualTo(4);
        assertThat(mirri.getEffectiveToughness()).isEqualTo(4);
        assertThat(mirri.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    // ===== End of turn cleanup =====

    @Test
    @DisplayName("Boost and lifelink wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new MirriCatWarrior());
        harness.setHand(player1, List.of(new BlessingOfBelzenlok()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player1, "Mirri, Cat Warrior");
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent mirri = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(mirri.getPowerModifier()).isEqualTo(0);
        assertThat(mirri.getToughnessModifier()).isEqualTo(0);
        assertThat(mirri.hasKeyword(Keyword.LIFELINK)).isFalse();
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlessingOfBelzenlok()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Blessing of Belzenlok"));
    }
}
