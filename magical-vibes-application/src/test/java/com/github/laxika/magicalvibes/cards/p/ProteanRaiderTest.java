package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProteanRaider.class, AngelOfMercy.class})
class ProteanRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("Raid lets Protean Raider enter as a copy of a creature")
    void copiesCreatureWithRaid() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AngelOfMercy());
        castProteanRaider();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(findRaider()).isNotNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Protean Raider does not offer the copy when its controller did not attack")
    void doesNotCopyWithoutRaid() {
        harness.addToBattlefield(player2, new AngelOfMercy());
        castProteanRaider();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(findRaider()).isNotNull();
    }

    @Test
    @DisplayName("An opponent's attack does not satisfy Protean Raider's raid")
    void opponentAttackDoesNotEnableRaid() {
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());
        harness.addToBattlefield(player2, new AngelOfMercy());
        castProteanRaider();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(findRaider()).isNotNull();
    }

    @Test
    @DisplayName("Declining the raid copy leaves Protean Raider as itself without triggering")
    void entersNormallyWhenRaidCopyIsDeclined() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        harness.addToBattlefield(player2, new AngelOfMercy());
        castProteanRaider();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(findRaider()).isNotNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Raid with no creature to copy lets Protean Raider enter normally")
    void entersNormallyWithRaidAndNoCreatures() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        castProteanRaider();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(findRaider()).isNotNull();
    }

    @Test
    @DisplayName("Raid can copy your own creature without copying its counters or tapped status")
    void copiesOwnCreatureWithoutCountersOrTappedStatus() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        Permanent original = harness.addToBattlefieldAndReturn(player1, new AngelOfMercy());
        original.tap();
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        castProteanRaider();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, original.getId());
        harness.passBothPriorities();

        Permanent copy = findRaider();
        assertThat(copy).isNotNull();
        assertThat(copy.isTapped()).isFalse();
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertLife(player1, 23);
        assertThat(original.isTapped()).isTrue();
        assertThat(original.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void castProteanRaider() {
        harness.castFromHand(player1, new ProteanRaider(), "{1}{U}{R}");
    }

    private Permanent findRaider() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof ProteanRaider)
                .findFirst()
                .orElse(null);
    }
}
