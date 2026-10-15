package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({LilianaOfTheDarkRealms.class, Forest.class, WalkingCorpse.class, Swamp.class})
class LilianaOfTheDarkRealmsTest extends BaseCardTest {

    private static final String PLUS_MODE = "Target creature gets +X/+X until end of turn.";
    private static final String MINUS_MODE = "Target creature gets -X/-X until end of turn.";

    @Test
    @DisplayName("+1 tutors a Swamp from the library to hand")
    void plusOneTutorsSwamp() {
        Permanent liliana = addReadyLiliana(player1);
        harness.setLibrary(player1, List.of(new Forest(), new Swamp()));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        var offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).allMatch(card -> card.getName().equals("Swamp"));
        harness.handleCardChosen(player1, 0);

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        harness.assertInHand(player1, "Swamp");
    }

    @Test
    @DisplayName("-3 with the +X/+X mode pumps the target by the number of Swamps controlled")
    void minusThreePumpsBySwampCount() {
        Permanent liliana = addReadyLiliana(player1);
        liliana.setCounterCount(CounterType.LOYALTY, 4);
        addSwamp(player1);
        addSwamp(player1);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        harness.activateAbility(player1, 0, 1, null, bear.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, PLUS_MODE);

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("-3 with the -X/-X mode shrinks the target and kills it when toughness hits zero")
    void minusThreeShrinksAndKills() {
        addReadyLiliana(player1);
        addSwamp(player1);
        addSwamp(player1);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

        harness.activateAbility(player1, 0, 1, null, bear.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, MINUS_MODE);

        harness.assertInGraveyard(player2, "Walking Corpse");
    }

    @Test
    @DisplayName("-3 counts only Swamps you control")
    void minusThreeCountsOnlyOwnSwamps() {
        addReadyLiliana(player1);
        addSwamp(player1);
        addSwamp(player2);
        addSwamp(player2);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        harness.activateAbility(player1, 0, 1, null, bear.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, PLUS_MODE);

        assertThat(bear.getEffectivePower()).isEqualTo(3);
    }

    @Test
    @DisplayName("-3 pump wears off at end of turn")
    void minusThreePumpWearsOff() {
        addReadyLiliana(player1);
        addSwamp(player1);
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());

        harness.activateAbility(player1, 0, 1, null, bear.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, PLUS_MODE);
        assertThat(bear.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("-3 cannot target a noncreature permanent")
    void minusThreeCannotTargetLand() {
        addReadyLiliana(player1);
        Permanent swamp = addSwamp(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, swamp.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-6 creates an emblem granting Swamps you control a four-black mana ability")
    void minusSixCreatesEmblem() {
        Permanent liliana = addReadyLiliana(player1);
        liliana.setCounterCount(CounterType.LOYALTY, 6);
        Permanent swamp = addSwamp(player1);
        harness.addToBattlefield(player1, new Forest());
        addSwamp(player2);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Liliana of the Dark Realms");
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(swamp.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.forceActivePlayer(player2);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.forceActivePlayer(player1);
        Permanent laterSwamp = addSwamp(player1);
        harness.activateAbility(player1, 2, 0, null, null);
        assertThat(laterSwamp.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(8);
    }

    @Test
    @DisplayName("Cannot activate -6 with only 3 loyalty")
    void cannotUltimateWithoutLoyalty() {
        addReadyLiliana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    @DisplayName("+1 may fail to find even when a Swamp is available")
    void plusOneMayFailToFind() {
        addReadyLiliana(player1);
        harness.setLibrary(player1, List.of(new Swamp()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("+1 resolves with no matching Swamp")
    void plusOneWithNoSwamp() {
        addReadyLiliana(player1);
        harness.setLibrary(player1, List.of(new Forest()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("-3 determines X on resolution and fixes that value afterward")
    void minusThreeCountsSwampsOnResolution() {
        addReadyLiliana(player1);
        addSwamp(player1);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

        harness.activateAbility(player1, 0, 1, null, bear.getId());
        addSwamp(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, PLUS_MODE);

        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(4);
        addSwamp(player1);
        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("-3 with no Swamps leaves the target unchanged")
    void minusThreeWithZeroSwamps() {
        addReadyLiliana(player1);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new WalkingCorpse());

        harness.activateAbility(player1, 0, 1, null, bear.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, MINUS_MODE);

        harness.assertOnBattlefield(player2, "Walking Corpse");
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    private Permanent addReadyLiliana(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new LilianaOfTheDarkRealms());
        perm.setCounterCount(CounterType.LOYALTY, 3);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    private Permanent addSwamp(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Swamp());
    }
}
