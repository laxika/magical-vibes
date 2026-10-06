package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CrucibleOfWorlds;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RampantGrowth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KaghaShadowArchdruid.class, Forest.class, GrizzlyBears.class, CrucibleOfWorlds.class,
        RampantGrowth.class})
class KaghaShadowArchdruidTest extends BaseCardTest {

    @Test
    void attackGainsDeathtouchAndMillsTwoCards() {
        Permanent kagha = addCreatureReady(player1, new KaghaShadowArchdruid());
        Card first = new GrizzlyBears();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, kagha, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(first.getId(), second.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, kagha, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void castsOnePermanentPutIntoGraveyardFromLibraryPerTurn() {
        harness.addToBattlefield(player1, new KaghaShadowArchdruid());
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        gd.cardsPutIntoGraveyardFromLibraryThisTurn
                .computeIfAbsent(player1.getId(), ignored -> new HashSet<>())
                .add(first.getId());
        gd.cardsPutIntoGraveyardFromLibraryThisTurn
                .get(player1.getId())
                .add(second.getId());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 4);
        prepareMainPhase();

        harness.castFromGraveyard(player1, 0);
        resolveAllTriggers();
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsPermanentNotPutIntoGraveyardFromLibraryThisTurn() {
        harness.addToBattlefield(player1, new KaghaShadowArchdruid());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 2);
        prepareMainPhase();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void playsAQualifyingLandFromGraveyard() {
        Forest forest = new Forest();
        harness.addToBattlefield(player1, new KaghaShadowArchdruid());
        harness.setGraveyard(player1, List.of(forest));
        gd.cardsPutIntoGraveyardFromLibraryThisTurn
                .computeIfAbsent(player1.getId(), ignored -> new HashSet<>())
                .add(forest.getId());
        prepareMainPhase();

        harness.playGraveyardLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(forest.getId()));
    }

    @Test
    void actualAttackMillAllowsCastingInPostcombatMainPhase() {
        GrizzlyBears bears = new GrizzlyBears();
        millWithKagha(List.of(bears, new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castFromGraveyard(player1, bears.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId).doesNotContain(bears.getId());
    }

    @Test
    void playingMilledLandUsesTheSamePermissionAsCastingPermanent() {
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        millWithKagha(List.of(forest, bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.playGraveyardLand(player1, forest.getId());

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void castingMilledPermanentUsesTheSamePermissionAsPlayingLand() {
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        millWithKagha(List.of(forest, bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castFromGraveyard(player1, bears.getId());
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void playingLandWithCrucibleDoesNotSpendKaghasPermission() {
        Forest forest = new Forest();
        GrizzlyBears bears = new GrizzlyBears();
        millWithKagha(List.of(forest, bears));
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.playGraveyardLand(player1, forest.getId());
        harness.castFromGraveyard(player1, bears.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void millingShortLibraryStillGrantsDeathtouch() {
        Permanent kagha = addCreatureReady(player1, new KaghaShadowArchdruid());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, kagha, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId).containsExactly(forest.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void graveyardPermissionDoesNotWaiveManaCost() {
        GrizzlyBears bears = new GrizzlyBears();
        millWithKagha(List.of(bears, new Forest()));

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId).contains(bears.getId());
    }

    @Test
    void cannotCastMilledSorcery() {
        RampantGrowth growth = new RampantGrowth();
        millWithKagha(List.of(growth, new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, growth.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotCastMilledCreatureDuringCombat() {
        GrizzlyBears bears = new GrizzlyBears();
        millWithKagha(List.of(bears, new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotPlayMilledLandAfterPlayingLandFromHand() {
        Forest forest = new Forest();
        millWithKagha(List.of(forest, new GrizzlyBears()));
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThatThrownBy(() -> harness.playGraveyardLand(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void permissionDisappearsWhenKaghaLeavesBattlefield() {
        GrizzlyBears bears = new GrizzlyBears();
        millWithKagha(List.of(bears, new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        gd.playerBattlefields.get(player1.getId()).clear();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void millWithKagha(List<Card> cards) {
        addCreatureReady(player1, new KaghaShadowArchdruid());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, cards);
        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
