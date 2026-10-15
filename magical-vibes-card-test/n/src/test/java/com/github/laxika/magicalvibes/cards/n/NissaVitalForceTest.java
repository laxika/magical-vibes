package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BlossomingDefense;
import com.github.laxika.magicalvibes.cards.c.ConfiscationCoup;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RiparianTiger;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NissaVitalForce.class, Forest.class, BlossomingDefense.class, RiparianTiger.class,
        ConfiscationCoup.class})
class NissaVitalForceTest extends BaseCardTest {

    @Test
    @DisplayName("+1 untaps and animates a land into a 5/5 Elemental with haste without changing its color")
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
        assertThat(gqs.getEffectiveColors(gd, forest)).isEmpty();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, forest)).contains(CardSubtype.ELEMENTAL);
        assertThat(gqs.isLand(gd, forest)).isTrue();

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
        Card permanent = new RiparianTiger();
        Card instant = new BlossomingDefense();
        harness.setGraveyard(player1, List.of(permanent, instant));

        harness.activateAbility(player1, 0, 1, null, permanent.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        harness.assertInHand(player1, "Riparian Tiger");
        harness.assertNotInGraveyard(player1, "Riparian Tiger");
        harness.assertInGraveyard(player1, "Blossoming Defense");
    }

    @Test
    @DisplayName("-3 cannot target a nonpermanent card")
    void minusThreeCannotTargetNonpermanent() {
        addReadyNissa(player1, 4);
        Card instant = new BlossomingDefense();
        harness.setGraveyard(player1, List.of(instant));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 1, null, instant.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-6 creates a landfall emblem that may draw a card")
    void minusSixCreatesLandfallDrawEmblem() {
        Permanent nissa = addReadyNissa(player1, 7);
        harness.setLibrary(player1, List.of(new RiparianTiger()));

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(nissa.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.emblems).hasSize(1);

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInHand(player1, "Riparian Tiger");
    }

    @Test
    void plusOneCanAnimateAnUntappedLandAndExpiresAtYourNextTurn() {
        addReadyNissa(player1, 3);
        Permanent forest = addLand(player1);

        harness.activateAbility(player1, 0, 0, forest.getId(), null);
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, forest)).isTrue();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isFalse();
    }

    @Test
    void animationExpiresAtNissasControllersNextTurnAfterLandIsStolen() {
        addReadyNissa(player1, 3);
        Permanent forest = addLand(player1);
        harness.activateAbility(player1, 0, 0, forest.getId(), null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new ConfiscationCoup()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.castSorcery(player2, 0, forest.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(forest);
        assertThat(gqs.isCreature(gd, forest)).isTrue();

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, forest)).isFalse();
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isFalse();
    }

    @Test
    void minusThreeCannotTargetOpponentsGraveyard() {
        addReadyNissa(player1, 4);
        Card target = new RiparianTiger();
        harness.setGraveyard(player2, List.of(target));

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 1, null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusThreeCanReturnALand() {
        addReadyNissa(player1, 4);
        Card target = new Forest();
        harness.setGraveyard(player1, List.of(target));

        harness.activateAbility(player1, 0, 1, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    void emblemPersistsAfterNissaDiesAndDrawCanBeDeclined() {
        addReadyNissa(player1, 6);
        harness.setLibrary(player1, List.of(new RiparianTiger()));
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Nissa, Vital Force");

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotInHand(player1, "Riparian Tiger");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void emblemDoesNotTriggerForOpponentsLand() {
        addReadyNissa(player1, 7);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyNissa(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new NissaVitalForce());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private Permanent addLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Forest());
    }
}
