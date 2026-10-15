package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.j.JayaVeneratedFiremage;
import com.github.laxika.magicalvibes.cards.p.PouncingLynx;
import com.github.laxika.magicalvibes.cards.t.TotallyLost;
import com.github.laxika.magicalvibes.model.CounterType;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BondOfPassion.class, PouncingLynx.class, TotallyLost.class,
        JayaVeneratedFiremage.class, InvasionOfZendikar.class})
class BondOfPassionTest extends BaseCardTest {

    @Test
    @DisplayName("Gains control, untaps, grants haste, and deals 2 damage to a player")
    void resolvesAllEffectsAgainstPlayer() {
        Permanent target = addCreatureReady(player2, new PouncingLynx());
        target.tap();
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BondOfPassion()));
        addMana();

        harness.castSorcery(player1, 0, List.of(target.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals damage to another creature")
    void dealsDamageToAnotherCreature() {
        Permanent target = addCreatureReady(player2, new PouncingLynx());
        Permanent damageTarget = addCreatureReady(player2, new PouncingLynx());
        harness.setHand(player1, List.of(new BondOfPassion()));
        addMana();

        harness.castSorcery(player1, 0, List.of(target.getId(), damageTarget.getId()));
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(damageTarget.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent target = addCreatureReady(player2, new PouncingLynx());
        harness.setHand(player1, List.of(new BondOfPassion()));
        addMana();

        harness.castSorcery(player1, 0, List.of(target.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    @Test
    @DisplayName("Rejects duplicate targets")
    void rejectsDuplicateTargets() {
        Permanent target = addCreatureReady(player2, new PouncingLynx());
        harness.setHand(player1, List.of(new BondOfPassion()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All targets must be different");
    }

    @Test
    @DisplayName("Deals damage even when the control target leaves the battlefield")
    void damageResolvesWhenControlTargetLeaves() {
        Permanent target = addCreatureReady(player2, new PouncingLynx());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new BondOfPassion()));
        harness.setHand(player2, List.of(new TotallyLost()));
        addMana();
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, List.of(target.getId(), player2.getId()));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Pouncing Lynx");
        harness.assertNotOnBattlefield(player2, "Pouncing Lynx");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(target.getCard());
    }

    @Test
    @DisplayName("Gains control, untaps, and grants haste even when the damage target leaves")
    void controlResolvesWhenDamageTargetLeaves() {
        Permanent target = addCreatureReady(player2, new PouncingLynx());
        Permanent damageTarget = addCreatureReady(player2, new PouncingLynx());
        target.tap();
        harness.setHand(player1, List.of(new BondOfPassion()));
        harness.setHand(player2, List.of(new TotallyLost()));
        addMana();
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, List.of(target.getId(), damageTarget.getId()));
        harness.passPriority(player1);
        harness.castInstant(player2, 0, damageTarget.getId());
        resolveAllTriggers();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(damageTarget.getMarkedDamage()).isZero();
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(damageTarget.getCard());
    }

    @Test
    @DisplayName("A battle is a legal damage target")
    void dealsDamageToBattle() {
        Permanent target = addCreatureReady(player2, new PouncingLynx());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setCounterCount(CounterType.DEFENSE, 3);
        battle.setProtectorPlayerId(player2.getId());
        harness.setHand(player1, List.of(new BondOfPassion()));
        addMana();

        harness.castSorcery(player1, 0, List.of(target.getId(), battle.getId()));
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Deals damage to a planeswalker")
    void dealsDamageToPlaneswalker() {
        Permanent target = addCreatureReady(player2, new PouncingLynx());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JayaVeneratedFiremage());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        harness.setHand(player1, List.of(new BondOfPassion()));
        addMana();

        harness.castSorcery(player1, 0, List.of(target.getId(), planeswalker.getId()));
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(planeswalker);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
