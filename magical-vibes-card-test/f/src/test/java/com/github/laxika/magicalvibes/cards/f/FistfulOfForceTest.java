package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.cards.s.SylvanEchoes;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FistfulOfForce.class, WoodlandChangeling.class, Forest.class, SylvanEchoes.class})
class FistfulOfForceTest extends BaseCardTest {

    private void keepBothRevealedCardsOnTop() {
        placeRevealedCard(player1, false);
        placeRevealedCard(player2, false);
    }

    private void placeRevealedCard(Player player, boolean bottom) {
        PendingInteraction.Scry choice = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player.getId());
        gs.handleInteractionAnswer(gd, player, new InteractionAnswer.ScryOrder(
                bottom ? List.of() : List.of(0), bottom ? List.of(0) : List.of()));
    }

    @Test
    void tiedClashGrantsOnlyBaseBoost() {
        prepare();
        harness.addToBattlefield(player1, new WoodlandChangeling());
        harness.setLibrary(player1, List.of(new WoodlandChangeling(), new Forest()));
        harness.setLibrary(player2, List.of(new WoodlandChangeling(), new Forest()));

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Woodland Changeling"));
        keepBothRevealedCardsOnTop();

        Permanent target = findPermanent(player1, "Woodland Changeling");
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
    }

    @Test
    void winningClashBoostsOpponentsCreatureEvenWhenRevealedCardsGoToBottom() {
        prepare();
        harness.addToBattlefield(player2, new WoodlandChangeling());
        stackClashWinForCaster();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Woodland Changeling"));
        placeRevealedCard(player1, true);
        placeRevealedCard(player2, true);

        Permanent target = findPermanent(player2, "Woodland Changeling");
        assertThat(target.getPowerModifier()).isEqualTo(4);
        assertThat(target.getToughnessModifier()).isEqualTo(4);
        assertThat(target.getGrantedKeywords()).contains(Keyword.TRAMPLE);
        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isInstanceOf(WoodlandChangeling.class);
        harness.assertInGraveyard(player1, "Fistful of Force");
    }

    @Test
    void activeOpponentChoosesClashPlacementBeforeNonactiveCaster() {
        prepare();
        harness.forceActivePlayer(player2);
        harness.addToBattlefield(player1, new WoodlandChangeling());
        stackClashWinForCaster();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Woodland Changeling"));
        placeRevealedCard(player2, false);
        placeRevealedCard(player1, false);

        assertThat(findPermanent(player1, "Woodland Changeling").getPowerModifier()).isEqualTo(4);
    }

    private void prepare() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new FistfulOfForce()));
        harness.addMana(player1, ManaColor.GREEN, 2); // {1}{G}
    }

    @Test
    void opponentWinningClashTriggersTheirSylvanEchoes() {
        prepare();
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new WoodlandChangeling());
        harness.addToBattlefield(player2, new SylvanEchoes());
        stackClashLossForCaster();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Woodland Changeling"));
        keepBothRevealedCardsOnTop();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertInHand(player2, "Woodland Changeling");
        assertThat(findPermanent(player1, "Woodland Changeling").getPowerModifier()).isEqualTo(2);
    }

    @Test
    void emptyCasterLibraryCannotWinClash() {
        prepare();
        harness.addToBattlefield(player1, new WoodlandChangeling());
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Woodland Changeling"));
        placeRevealedCard(player2, false);

        Permanent target = findPermanent(player1, "Woodland Changeling");
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(2);
        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
    }

    @Test
    void removedTargetPreventsClashAndLibraryPlacement() {
        prepare();
        harness.addToBattlefield(player1, new WoodlandChangeling());
        stackClashWinForCaster();
        List<?> casterLibrary = List.copyOf(gd.playerDecks.get(player1.getId()));
        List<?> opponentLibrary = List.copyOf(gd.playerDecks.get(player2.getId()));

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Woodland Changeling"));
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEqualTo(casterLibrary);
        assertThat(gd.playerDecks.get(player2.getId())).isEqualTo(opponentLibrary);
        harness.assertInGraveyard(player1, "Fistful of Force");
    }

    // Caster wins the clash: their revealed Woodland Changeling (MV 2) beats the opponent's Forest (MV 0).
    private void stackClashWinForCaster() {
        harness.setLibrary(player1, List.of(new WoodlandChangeling(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
    }

    // Caster loses the clash: the opponent reveals the higher mana value.
    private void stackClashLossForCaster() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new WoodlandChangeling(), new Forest(), new Forest()));
    }

    @Test
    @DisplayName("Winning the clash grants +4/+4 and trample")
    void wonClashGrantsFullBoostAndTrample() {
        prepare();
        harness.addToBattlefield(player1, new WoodlandChangeling());
        stackClashWinForCaster();

        UUID bearId = harness.getPermanentId(player1, "Woodland Changeling");
        harness.castAndResolveInstant(player1, 0, bearId);
        keepBothRevealedCardsOnTop();

        Permanent bear = findPermanent(player1, "Woodland Changeling");
        assertThat(bear.getPowerModifier()).isEqualTo(4);
        assertThat(bear.getToughnessModifier()).isEqualTo(4);
        assertThat(bear.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Losing the clash grants only +2/+2 and no trample")
    void lostClashGrantsBaseBoostOnly() {
        prepare();
        harness.addToBattlefield(player1, new WoodlandChangeling());
        stackClashLossForCaster();

        UUID bearId = harness.getPermanentId(player1, "Woodland Changeling");
        harness.castAndResolveInstant(player1, 0, bearId);
        keepBothRevealedCardsOnTop();

        Permanent bear = findPermanent(player1, "Woodland Changeling");
        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isEqualTo(2);
        assertThat(bear.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Boost and trample wear off at cleanup step")
    void boostWearsOffAtCleanup() {
        prepare();
        harness.addToBattlefield(player1, new WoodlandChangeling());
        stackClashWinForCaster();

        UUID bearId = harness.getPermanentId(player1, "Woodland Changeling");
        harness.castAndResolveInstant(player1, 0, bearId);
        keepBothRevealedCardsOnTop();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Woodland Changeling");
        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(bear.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        prepare();
        harness.addToBattlefield(player1, new WoodlandChangeling()); // valid target so spell is castable
        harness.addToBattlefield(player1, new Forest());

        UUID landId = harness.getPermanentId(player1, "Forest");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, landId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
