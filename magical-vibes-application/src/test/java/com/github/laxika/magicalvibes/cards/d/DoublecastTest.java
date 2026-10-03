package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.o.OreskosSwiftclaw;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.r.Redirect;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Doublecast.class, Shock.class, OreskosSwiftclaw.class, Divination.class, Cancel.class})
class DoublecastTest extends BaseCardTest {

    @Test
    @DisplayName("sets up a copy of the next instant or sorcery cast this turn")
    void setsUpPendingCopy() {
        harness.setHand(player1, List.of(new Doublecast()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount.get(player1.getId())).isEqualTo(1);
        harness.assertInGraveyard(player1, "Doublecast");
    }

    @Test
    @DisplayName("copies only the next instant or sorcery spell")
    void copiesNextInstantOnly() {
        harness.setHand(player1, List.of(new Doublecast()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().contains("Copy Shock"));
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount).doesNotContainKey(player1.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("does not copy a creature spell")
    void ignoresCreatureSpell() {
        harness.setHand(player1, List.of(new Doublecast()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.setHand(player1, List.of(new OreskosSwiftclaw()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getDescription().startsWith("Copy "));
        assertThat(gd.pendingNextInstantSorceryCopyThisTurnCount.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void mayRetargetCopyWithoutChangingOriginal() {
        harness.castFromHand(player1, new Doublecast(), "{R}{R}");
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
    }

    @Test
    void copiesUntargetedSorcery() {
        harness.castFromHand(player1, new Doublecast(), "{R}{R}");
        harness.passBothPriorities();
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock(), new Shock()));
        harness.castFromHand(player1, new Divination(), "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
    }

    @Test
    void stillCopiesSpellCounteredBeforeDelayedTriggerResolves() {
        harness.castFromHand(player1, new Doublecast(), "{R}{R}");
        harness.passBothPriorities();
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, shock.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void opponentsSpellDoesNotConsumeDelayedCopy() {
        harness.castFromHand(player1, new Doublecast(), "{R}{R}");
        harness.passBothPriorities();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertLife(player1, 18);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 16);
    }

    @Test
    void unusedCopyExpiresAtEndOfTurn() {
        harness.castFromHand(player1, new Doublecast(), "{R}{R}");
        harness.passBothPriorities();
        harness.passUntil(TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.assertLife(player2, 18);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void consecutiveDoublecastsCopyFollowingSpellTwice() {
        harness.castFromHand(player1, new Doublecast(), "{R}{R}");
        harness.passBothPriorities();
        harness.castFromHand(player1, new Doublecast(), "{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 14);
    }

    @Test
    @CardUsed({Redirect.class})
    void copiesTargetsAsTheyExistWhenDelayedTriggerResolves() {
        harness.castFromHand(player1, new Doublecast(), "{R}{R}");
        harness.passBothPriorities();
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.setHand(player2, List.of(new Redirect()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, shock.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player1, 16);
        harness.assertLife(player2, 20);
    }
}
