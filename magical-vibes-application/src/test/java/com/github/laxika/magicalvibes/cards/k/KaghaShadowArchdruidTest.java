package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({KaghaShadowArchdruid.class, Forest.class, GrizzlyBears.class})
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

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
