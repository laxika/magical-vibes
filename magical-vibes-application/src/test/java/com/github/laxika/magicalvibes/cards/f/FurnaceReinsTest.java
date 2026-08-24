package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({FurnaceReins.class, GrizzlyBears.class, Pacifism.class})
class FurnaceReinsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Furnace Reins gains control, untaps, and grants haste")
    void resolvesControlUntapAndHaste() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();
        castFurnaceReins(target);

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("The stolen creature creates a Treasure when it deals combat damage to a player")
    void createsTreasureOnCombatDamageToPlayer() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castFurnaceReins(target);
        target.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(treasuresFor(player1)).hasSize(1);
    }

    @Test
    @DisplayName("The granted Treasure ability expires at end of turn")
    void treasureAbilityExpiresAtEndOfTurn() {
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        castFurnaceReins(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        target.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(treasuresFor(player1)).isEmpty();
    }

    @Test
    @DisplayName("Furnace Reins cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Pacifism());
        harness.setHand(player1, List.of(new FurnaceReins()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private void castFurnaceReins(Permanent target) {
        harness.setHand(player1, List.of(new FurnaceReins()));
        addMana();
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private List<Permanent> treasuresFor(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.TREASURE))
                .toList();
    }
}
