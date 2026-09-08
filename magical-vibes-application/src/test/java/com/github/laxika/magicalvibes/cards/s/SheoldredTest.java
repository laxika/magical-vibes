package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LilianaVess;
import com.github.laxika.magicalvibes.cards.t.TheTrueScriptures;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Sheoldred.class, TheTrueScriptures.class, GrizzlyBears.class, LilianaVess.class, Shock.class})
class SheoldredTest extends BaseCardTest {

    @Test
    @DisplayName("Sheoldred makes each opponent sacrifice a nontoken creature or planeswalker")
    void etbSacrificesAnOpponentsNontokenCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Sheoldred()));
        addManaForSheoldred();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Sheoldred can transform only when an opponent has eight cards in their graveyard")
    void transformsWithTheGraveyardCondition() {
        Permanent sheoldred = addReadySheoldred();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.setGraveyard(player2, filler(8));
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(sheoldred), null, null);
        harness.passBothPriorities();

        Permanent transformed = findPermanent(player1, "The True Scriptures");
        assertThat(transformed.isTransformed()).isTrue();
        assertThat(transformed.getCard().getName()).isEqualTo("The True Scriptures");
    }

    @Test
    @DisplayName("The True Scriptures chapter I destroys one target creature or planeswalker per opponent")
    void chapterIDestroysChosenOpponentPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new LilianaVess());
        target.setCounterCount(CounterType.LOYALTY, target.getCard().getLoyalty());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent saga = addTransformedSaga();
        saga.setCounterCount(CounterType.LORE, 0);

        advanceToNextChapter();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Liliana Vess");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The True Scriptures chapter III returns creatures and resets to Sheoldred")
    void chapterIIIReturnsCreaturesAndTheFrontFace() {
        Permanent saga = addTransformedSaga();
        saga.setCounterCount(CounterType.LORE, 2);
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        advanceToNextChapter();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Sheoldred")
                        && !permanent.isTransformed());
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .hasSize(2);
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("The True Scriptures"));
    }

    private Permanent addReadySheoldred() {
        Permanent permanent = new Permanent(new Sheoldred());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private Permanent addTransformedSaga() {
        Sheoldred front = new Sheoldred();
        Permanent permanent = new Permanent(front);
        permanent.setCard(front.getBackFaceCard());
        permanent.setTransformed(true);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void addManaForSheoldred() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    private List<Card> filler(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Shock());
        }
        return cards;
    }
}
