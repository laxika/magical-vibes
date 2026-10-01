package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AzoriusFirstWing;
import com.github.laxika.magicalvibes.cards.c.CoilingOracle;
import com.github.laxika.magicalvibes.cards.s.SimicRagworm;
import com.github.laxika.magicalvibes.cards.s.SimicSignet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Voidslime.class, AzoriusFirstWing.class, SimicRagworm.class,
        CoilingOracle.class, SimicSignet.class})
class VoidslimeTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a target spell")
    void countersSpell() {
        AzoriusFirstWing firstWing = new AzoriusFirstWing();

        harness.setHand(player1, List.of(new Voidslime()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, firstWing, "{W}{U}");
        harness.passPriority(player2);
        harness.castInstant(player1, 0, firstWing.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Azorius First-Wing");
        harness.assertNotOnBattlefield(player2, "Azorius First-Wing");
    }

    @Test
    @DisplayName("Counters a target non-mana activated ability")
    void countersActivatedAbility() {
        SimicRagworm ragworm = new SimicRagworm();
        Permanent ragwormPermanent = harness.addToBattlefieldAndReturn(player2, ragworm);
        ragwormPermanent.tap();
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.setHand(player1, List.of(new Voidslime()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, null);
        harness.passPriority(player2);

        harness.castInstant(player1, 0, ragworm.getId());
        harness.passBothPriorities();

        assertThat(ragwormPermanent.isTapped()).isTrue();
        assertThat(harness.getGameData().stack).isEmpty();
    }

    @Test
    @DisplayName("Counters a target triggered ability")
    void countersTriggeredAbility() {
        CoilingOracle oracle = new CoilingOracle();
        harness.setHand(player1, List.of(new Voidslime()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, oracle, "{G}{U}");
        harness.passBothPriorities();
        StackEntry trigger = harness.getGameData().stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .findFirst()
                .orElseThrow();
        harness.passPriority(player2);

        harness.castInstant(player1, 0, trigger.getCard().getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Coiling Oracle");
        assertThat(harness.getGameData().stack).noneMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Cannot target a mana ability")
    void cannotTargetManaAbility() {
        SimicSignet signet = new SimicSignet();
        harness.addToBattlefield(player2, signet);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.setHand(player1, List.of(new Voidslime()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.forceActivePlayer(player2);
        harness.activateAbility(player2, 0, null, null);
        harness.passPriority(player2);

        assertThat(harness.getGameData().stack).isEmpty();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, signet.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new Voidslime()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
