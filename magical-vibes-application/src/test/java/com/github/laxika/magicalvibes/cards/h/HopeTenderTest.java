package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HopeTenderTest extends BaseCardTest {

    // ===== {1}, {T}: Untap target land =====

    @Test
    @DisplayName("First ability untaps a tapped land and does not exert")
    void firstAbilityUntapsLandWithoutExert() {
        Permanent tender = addReadyTender(player1);
        Permanent forest = addForest(player1);
        forest.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(forest.isTapped()).isFalse();
        assertThat(tender.isTapped()).isTrue();
        assertThat(tender.getSkipUntapCount()).isZero();
    }

    @Test
    @DisplayName("First ability cannot target a non-land")
    void firstAbilityCannotTargetCreature() {
        addReadyTender(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(player2.getId()).get(0);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    // ===== {1}, {T}, Exert: Untap two target lands =====

    @Test
    @DisplayName("Second ability untaps two tapped lands and exerts")
    void secondAbilityUntapsTwoLandsAndExerts() {
        Permanent tender = addReadyTender(player1);
        Permanent forest1 = addForest(player1);
        Permanent forest2 = addForest(player1);
        forest1.tap();
        forest2.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(forest1.getId(), forest2.getId()));
        harness.passBothPriorities();

        assertThat(forest1.isTapped()).isFalse();
        assertThat(forest2.isTapped()).isFalse();
        assertThat(tender.isTapped()).isTrue();
        assertThat(tender.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability can target opponent's lands")
    void secondAbilityCanTargetOpponentsLands() {
        addReadyTender(player1);
        Permanent ownForest = addForest(player1);
        Permanent oppForest = addForest(player2);
        ownForest.tap();
        oppForest.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(ownForest.getId(), oppForest.getId()));
        harness.passBothPriorities();

        assertThat(ownForest.isTapped()).isFalse();
        assertThat(oppForest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Second ability cannot target a creature as either land")
    void secondAbilityCannotTargetCreature() {
        addReadyTender(player1);
        Permanent forest = addForest(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(player2.getId()).get(0);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(forest.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyTender(Player player) {
        Permanent perm = new Permanent(new HopeTender());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addForest(Player player) {
        Permanent perm = new Permanent(new Forest());
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
