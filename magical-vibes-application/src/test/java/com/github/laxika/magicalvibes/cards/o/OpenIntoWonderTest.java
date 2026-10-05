package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OpenIntoWonder.class, GrizzlyBears.class, Forest.class})
class OpenIntoWonderTest extends BaseCardTest {

    @Test
    @DisplayName("X=2 makes both target creatures unblockable")
    void makesBothTargetsUnblockable() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new OpenIntoWonder()));
        harness.addMana(player1, ManaColor.BLUE, 4); // X=2: {2}{U}{U}

        harness.castSorcery(player1, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isCantBeBlocked()).isTrue();
        assertThat(second.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("A granted creature dealing combat damage to a player draws a card")
    void grantedCreatureDrawsOnCombatDamage() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);

        harness.setHand(player1, List.of(new OpenIntoWonder()));
        harness.addMana(player1, ManaColor.BLUE, 3); // X=1: {1}{U}{U}

        harness.castSorcery(player1, 0, 1, List.of(bears.getId()));
        harness.passBothPriorities();

        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        bears.setAttacking(true);
        harness.setLife(player2, 20);
        resolveCombat();

        // Grizzly Bears deals 2 combat damage to player2.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        // Resolve the granted "draw a card" trigger.
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Unblockable wears off at end of turn")
    void unblockableWearsOffAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new OpenIntoWonder()));
        harness.addMana(player1, ManaColor.BLUE, 3); // X=1

        harness.castSorcery(player1, 0, 1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(bears.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a non-creature")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new OpenIntoWonder()));
        harness.addMana(player1, ManaColor.BLUE, 3); // X=1

        UUID forestId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of(forestId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("X=0 resolves without targets")
    void zeroXResolvesWithoutTargets() {
        harness.setHand(player1, List.of(new OpenIntoWonder()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Open into Wonder");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("X=2 requires two targets, not one")
    void cannotChooseFewerTargetsThanX() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new OpenIntoWonder()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, List.of(bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Positive X cannot be cast without targets")
    void positiveXRequiresTargets() {
        harness.setHand(player1, List.of(new OpenIntoWonder()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's targeted creature draws for its controller")
    void opponentsCreatureDrawsForOpponent() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new OpenIntoWonder()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        harness.castSorcery(player1, 0, 1, List.of(bears.getId()));
        harness.passBothPriorities();
        assertThat(bears.isCantBeBlocked()).isTrue();
        int casterHandSize = gd.playerHands.get(player1.getId()).size();
        bears.setAttacking(true);
        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(casterHandSize);
    }

    @Test
    @DisplayName("Each targeted creature draws once when both deal combat damage")
    void bothCreaturesDrawCards() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new OpenIntoWonder()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 2, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        first.setAttacking(true);
        second.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The remaining target still gains both effects when another target leaves")
    void resolvesForRemainingTarget() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new OpenIntoWonder()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 2, List.of(first.getId(), second.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(second);
        gd.playerGraveyards.get(player1.getId()).add(second.getCard());
        harness.passBothPriorities();

        assertThat(first.isCantBeBlocked()).isTrue();
        first.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The granted draw ability expires at end of turn")
    void drawAbilityExpiresAtEndOfTurn() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new OpenIntoWonder()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, 1, List.of(bears.getId()));
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        int handSizeBeforeCombat = gd.playerHands.get(player1.getId()).size();

        bears.setAttacking(true);
        resolveCombat(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeCombat);
    }
}
