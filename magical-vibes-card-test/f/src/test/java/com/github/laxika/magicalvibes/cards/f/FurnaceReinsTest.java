package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BondedHerdbeast;
import com.github.laxika.magicalvibes.cards.i.InvasionOfAlara;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({FurnaceReins.class, BondedHerdbeast.class, InvasionOfAlara.class})
class FurnaceReinsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Furnace Reins gains control, untaps, and grants haste")
    void resolvesControlUntapAndHaste() {
        Permanent target = addCreatureReady(player2, new BondedHerdbeast());
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
        Permanent target = addCreatureReady(player2, new BondedHerdbeast());
        castFurnaceReins(target);
        target.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(treasuresFor(player1)).hasSize(1);
    }

    @Test
    @DisplayName("The granted Treasure ability expires at end of turn")
    void treasureAbilityExpiresAtEndOfTurn() {
        Permanent target = addCreatureReady(player1, new BondedHerdbeast());
        castFurnaceReins(target);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        target.setAttacking(true);
        target.setAttackTarget(player2.getId());
        resolveCombat();
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(treasuresFor(player1)).isEmpty();
    }

    @Test
    @DisplayName("Furnace Reins cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InvasionOfAlara());
        harness.setHand(player1, List.of(new FurnaceReins()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Combat damage to a battle creates one Treasure regardless of damage amount")
    void createsTreasureOnCombatDamageToBattle() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfAlara());
        battle.setProtectorPlayerId(player2.getId());
        battle.setCounterCount(CounterType.DEFENSE, 7);
        Permanent target = addCreatureReady(player2, new BondedHerdbeast());
        castFurnaceReins(target);
        target.setAttacking(true);
        target.setAttackTarget(battle.getId());

        resolveCombat();
        resolveAllTriggers();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(3);
        assertThat(treasuresFor(player1)).hasSize(1);
        assertThat(treasuresFor(player2)).isEmpty();
    }

    @Test
    @DisplayName("Control and haste last through the end step and expire during cleanup")
    void controlAndHasteExpireDuringCleanup() {
        Permanent target = addCreatureReady(player2, new BondedHerdbeast());
        castFurnaceReins(target);

        harness.forceStep(TurnStep.END_STEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Two resolutions grant two independent Treasure abilities")
    void repeatedResolutionsCreateTwoTreasures() {
        Permanent target = addCreatureReady(player2, new BondedHerdbeast());
        castFurnaceReins(target);
        castFurnaceReins(target);
        target.setAttacking(true);
        target.setAttackTarget(player2.getId());

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        assertThat(treasuresFor(player1)).hasSize(2);
        assertThat(treasuresFor(player2)).isEmpty();
    }

    @Test
    @DisplayName("The stolen creature can attack immediately despite the control change")
    void stolenCreatureCanAttackImmediately() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BondedHerdbeast());
        target.tap();
        castFurnaceReins(target);

        declareAttackers(List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 16);
        assertThat(treasuresFor(player1)).hasSize(1);
        assertThat(treasuresFor(player1).getFirst().isTapped()).isFalse();
    }

    private void castFurnaceReins(Permanent target) {
        harness.setHand(player1, List.of(new FurnaceReins()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, target.getId());
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
