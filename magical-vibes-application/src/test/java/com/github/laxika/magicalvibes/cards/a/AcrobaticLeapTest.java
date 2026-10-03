package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.m.MalametBrawler;
import com.github.laxika.magicalvibes.cards.o.OrazcaPuzzleDoor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AcrobaticLeap.class, MalametBrawler.class, OrazcaPuzzleDoor.class})
class AcrobaticLeapTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps, boosts, and grants flying to target creature")
    void untapsBoostsAndGrantsFlying() {
        Permanent target = addTappedCreature(player2);

        castAcrobaticLeap(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Boost and flying expire at end of turn")
    void effectsExpireAtEndOfTurn() {
        Permanent target = addTappedCreature(player2);
        castAcrobaticLeap(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player2, new OrazcaPuzzleDoor());
        harness.setHand(player1, List.of(new AcrobaticLeap()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("An untapped creature you control still gets the boost and flying")
    void boostsUntappedOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MalametBrawler());
        Permanent other = addTappedCreature(player1);

        castAcrobaticLeap(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isEqualTo(1);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(target.isSummoningSick()).isTrue();
        assertThat(other.isTapped()).isTrue();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Multiple copies stack their boosts and all temporary effects expire")
    void multipleCopiesStackUntilEndOfTurn() {
        Permanent target = addTappedCreature(player2);

        castAcrobaticLeap(target);
        castAcrobaticLeap(target);

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Does not resolve when its target has left the battlefield")
    void doesNotResolveForRemovedTarget() {
        Permanent target = addTappedCreature(player2);
        Permanent other = addTappedCreature(player2);
        harness.setHand(player1, List.of(new AcrobaticLeap()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, target.getId());

        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setExile(player2, List.of(target.getCard()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof AcrobaticLeap);
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
        assertThat(other.isTapped()).isTrue();
        assertThat(other.getPowerModifier()).isZero();
        assertThat(other.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
    }

    private void castAcrobaticLeap(Permanent target) {
        harness.setHand(player1, List.of(new AcrobaticLeap()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addTappedCreature(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new MalametBrawler());
        perm.setSummoningSick(false);
        perm.tap();
        return perm;
    }
}
