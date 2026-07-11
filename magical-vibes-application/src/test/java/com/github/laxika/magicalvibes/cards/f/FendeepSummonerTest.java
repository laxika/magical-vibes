package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FendeepSummonerTest extends BaseCardTest {

    @Test
    @DisplayName("Animates two target Swamps into 3/5 Treefolk Warriors that are still lands")
    void animatesTwoSwamps() {
        addReadySummoner(player1);
        Permanent swampA = addSwamp(player1);
        Permanent swampB = addSwamp(player1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(swampA.getId(), swampB.getId()));
        harness.passBothPriorities();

        for (Permanent swamp : List.of(swampA, swampB)) {
            assertThat(swamp.isAnimatedUntilEndOfTurn()).isTrue();
            assertThat(gqs.isCreature(gd, swamp)).isTrue();
            assertThat(gqs.getEffectivePower(gd, swamp)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, swamp)).isEqualTo(5);
            assertThat(swamp.getTransientSubtypes()).contains(CardSubtype.TREEFOLK, CardSubtype.WARRIOR);
            // Types are additive — the Swamp is still a land.
            assertThat(swamp.getCard().hasType(CardType.LAND)).isTrue();
        }
    }

    @Test
    @DisplayName("Up to two — a single target is legal")
    void animatesSingleSwamp() {
        addReadySummoner(player1);
        Permanent swamp = addSwamp(player1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(swamp.getId()));
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, swamp)).isTrue();
        assertThat(gqs.getEffectivePower(gd, swamp)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, swamp)).isEqualTo(5);
    }

    @Test
    @DisplayName("Animation wears off at end of turn")
    void animationWearsOff() {
        addReadySummoner(player1);
        Permanent swamp = addSwamp(player1);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(swamp.getId()));
        harness.passBothPriorities();

        swamp.resetModifiers();

        assertThat(swamp.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, swamp)).isFalse();
        assertThat(swamp.getTransientSubtypes()).doesNotContain(CardSubtype.TREEFOLK);
    }

    @Test
    @DisplayName("Cannot target a permanent that is not a Swamp")
    void cannotTargetNonSwamp() {
        addReadySummoner(player1);
        Permanent bear = addReadyCreature(player1, new GrizzlyBears());

        assertThatThrownBy(() ->
                harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private Permanent addReadySummoner(Player player) {
        Permanent perm = new Permanent(new FendeepSummoner());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addSwamp(Player player) {
        Permanent perm = new Permanent(new Swamp());
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
