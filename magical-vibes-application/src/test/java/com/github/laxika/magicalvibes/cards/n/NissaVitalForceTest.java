package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NissaVitalForceTest extends BaseCardTest {

    @Test
    @DisplayName("+1 untaps and animates a land into a green 5/5 Elemental with haste until your next turn")
    void plusOneUntapsAndAnimatesLand() {
        Permanent nissa = addReadyNissa(player1, 3);
        Permanent forest = addLand(player1);
        forest.tap();

        harness.activateAbility(player1, 0, 0, forest.getId(), null);
        harness.passBothPriorities();

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(forest.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(5);
        assertThat(gqs.getEffectiveColors(gd, forest)).containsExactly(CardColor.GREEN);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, forest)).contains(CardSubtype.ELEMENTAL);
        assertThat(forest.getCard().hasType(CardType.LAND)).isTrue();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(5);
    }

    @Test
    @DisplayName("+1 cannot target an opponent's land")
    void plusOneCannotTargetOpponentLand() {
        addReadyNissa(player1, 3);
        Permanent opponentForest = addLand(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, opponentForest.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-3 returns a target permanent card from the graveyard to its owner's hand")
    void minusThreeReturnsPermanentToHand() {
        Permanent nissa = addReadyNissa(player1, 4);
        Card permanent = new GrizzlyBears();
        Card instant = new GiantGrowth();
        harness.setGraveyard(player1, List.of(permanent, instant));

        harness.activateAbility(player1, 0, 1, null, permanent.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Giant Growth");
    }

    @Test
    @DisplayName("-3 cannot target a nonpermanent card")
    void minusThreeCannotTargetNonpermanent() {
        addReadyNissa(player1, 4);
        Card instant = new GiantGrowth();
        harness.setGraveyard(player1, List.of(instant));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 1, null, instant.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-6 creates a landfall emblem that may draw a card")
    void minusSixCreatesLandfallDrawEmblem() {
        Permanent nissa = addReadyNissa(player1, 7);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.emblems).hasSize(1);

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private Permanent addReadyNissa(Player player, int loyalty) {
        Permanent permanent = new Permanent(new NissaVitalForce());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private Permanent addLand(Player player) {
        Permanent permanent = new Permanent(new Forest());
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
