package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.IntrepidTenderfoot;
import com.github.laxika.magicalvibes.cards.h.HaliyaGuidedByLight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheSeriema.class, IntrepidTenderfoot.class, HaliyaGuidedByLight.class})
class TheSeriemaTest extends BaseCardTest {

    @Test
    void entersAndSearchesForLegendaryCreature() {
        Card legendaryCreature = new HaliyaGuidedByLight();
        Card nonlegendaryCreature = new IntrepidTenderfoot();
        Card legendaryArtifact = new TheSeriema();
        harness.setLibrary(player1, List.of(nonlegendaryCreature, legendaryArtifact, legendaryCreature));
        harness.setHand(player1, List.of(new TheSeriema()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        PendingInteraction.LibrarySearch search =
                gameData.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(legendaryCreature);
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();

        harness.handleCardChosen(player1, 0);

        assertThat(gameData.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(legendaryCreature.getId()));
        assertThat(gameData.playerDecks.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(legendaryCreature.getId()));
    }

    @Test
    void stationUsesTappedCreaturePowerAndUnlocksTheSeriemaAtSevenCounters() {
        Permanent seriema = harness.addToBattlefieldAndReturn(player1, new TheSeriema());
        Permanent bears = addCreatureReady(player1, new IntrepidTenderfoot());

        harness.activateAbility(player1, battlefieldIndex(seriema), null, null);
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isTrue();
        assertThat(seriema.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(gqs.isCreature(gd, seriema)).isFalse();

        seriema.setCounterCount(CounterType.CHARGE, 7);

        assertThat(gqs.isCreature(gd, seriema)).isTrue();
        assertThat(gqs.getEffectivePower(gd, seriema)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, seriema)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, seriema, Keyword.FLYING)).isTrue();
    }

    @Test
    void grantsIndestructibleOnlyToOtherTappedLegendaryCreaturesYouControl() {
        harness.addToBattlefieldAndReturn(player1, new TheSeriema());
        Permanent tappedLegendary = addCreatureReady(player1, new HaliyaGuidedByLight());
        tappedLegendary.tap();
        Permanent untappedLegendary = addCreatureReady(player1, new HaliyaGuidedByLight());
        Permanent tappedNonlegendary = addCreatureReady(player1, new IntrepidTenderfoot());
        tappedNonlegendary.tap();
        Permanent opponentLegendary = addCreatureReady(player2, new HaliyaGuidedByLight());
        opponentLegendary.tap();

        assertThat(gqs.hasKeyword(gd, tappedLegendary, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, untappedLegendary, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, tappedNonlegendary, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentLegendary, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void stationRequiresAnotherUntappedCreature() {
        Permanent seriema = harness.addToBattlefieldAndReturn(player1, new TheSeriema());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(seriema), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void stationCanTapASummoningSickCreatureWhileTheSeriemaIsTapped() {
        Permanent seriema = harness.addToBattlefieldAndReturn(player1, new TheSeriema());
        seriema.tap();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new IntrepidTenderfoot());

        harness.activateAbility(player1, battlefieldIndex(seriema), null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(seriema.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    void losesCreatureStatusAndFlyingBelowSevenAndDoesNotProtectItself() {
        Permanent seriema = harness.addToBattlefieldAndReturn(player1, new TheSeriema());
        seriema.setCounterCount(CounterType.CHARGE, 7);
        seriema.tap();

        assertThat(gqs.isCreature(gd, seriema)).isTrue();
        assertThat(gqs.hasKeyword(gd, seriema, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, seriema, Keyword.INDESTRUCTIBLE)).isFalse();

        seriema.setCounterCount(CounterType.CHARGE, 6);

        assertThat(gqs.isCreature(gd, seriema)).isFalse();
        assertThat(gqs.hasKeyword(gd, seriema, Keyword.FLYING)).isFalse();
    }

    @Test
    void stationCannotTapItselfOnceAnimated() {
        Permanent seriema = harness.addToBattlefieldAndReturn(player1, new TheSeriema());
        seriema.setCounterCount(CounterType.CHARGE, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(seriema), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(seriema.isTapped()).isFalse();
    }

    @Test
    void stationCannotBeActivatedOutsideMainPhase() {
        Permanent seriema = harness.addToBattlefieldAndReturn(player1, new TheSeriema());
        Permanent creature = addCreatureReady(player1, new IntrepidTenderfoot());
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(seriema), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(creature.isTapped()).isFalse();
    }
    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
