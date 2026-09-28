package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheBeastDeathlessPrince.class, GrizzlyBears.class})
class TheBeastDeathlessPrinceTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped with six stun counters")
    void entersTappedWithStunCounters() {
        TheBeastDeathlessPrince card = new TheBeastDeathlessPrince();
        card.setOwnerId(player1.getId());

        Permanent beast = harness.enterBattlefieldAndReturn(player1, card);

        assertThat(beast.isTapped()).isTrue();
        assertThat(beast.getCounterCount(CounterType.STUN)).isEqualTo(6);
    }

    @Test
    @DisplayName("On cast steals, untaps and grants menace and haste to a creature")
    void castTriggerTemporarilyImprovesTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        harness.setHand(player1, List.of(new TheBeastDeathlessPrince()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.hasKeyword(Keyword.MENACE)).isTrue();
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Draws and removes a stun counter when a creature damages its owner")
    void drawsWhenCreatureDamagesItsOwner() {
        TheBeastDeathlessPrince card = new TheBeastDeathlessPrince();
        card.setOwnerId(player1.getId());
        Permanent beast = harness.enterBattlefieldAndReturn(player1, card);

        GrizzlyBears ownedByPlayer2 = new GrizzlyBears();
        ownedByPlayer2.setOwnerId(player2.getId());
        Permanent attacker = addCreatureReady(player1, ownedByPlayer2);
        attacker.setAttacking(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
        assertThat(beast.getCounterCount(CounterType.STUN)).isEqualTo(5);
        assertThat(beast.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not trigger when a creature damages another player")
    void doesNotTriggerWhenCreatureDamagesAnotherPlayer() {
        TheBeastDeathlessPrince card = new TheBeastDeathlessPrince();
        card.setOwnerId(player1.getId());
        Permanent beast = harness.enterBattlefieldAndReturn(player1, card);

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
        assertThat(beast.getCounterCount(CounterType.STUN)).isEqualTo(6);
        assertThat(beast.isTapped()).isTrue();
    }
}
