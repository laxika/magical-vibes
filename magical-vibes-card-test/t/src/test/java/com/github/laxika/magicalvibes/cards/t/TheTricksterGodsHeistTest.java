package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AxgardCavalry;
import com.github.laxika.magicalvibes.cards.d.DepartTheRealm;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoldveinPick;
import com.github.laxika.magicalvibes.cards.r.RavenWings;
import com.github.laxika.magicalvibes.cards.s.SnakeskinVeil;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheTricksterGodsHeist.class, RavenWings.class, GoldveinPick.class,
        Forest.class, AxgardCavalry.class, SnakeskinVeil.class, DepartTheRealm.class})
class TheTricksterGodsHeistTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I exchanges control of two target creatures")
    void chapterIExchangesCreatureControl() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new AxgardCavalry());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new AxgardCavalry());
        addSaga(0);

        triggerChapter();
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opposingCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ownCreature);
    }

    @Test
    @DisplayName("Chapter II exchanges control of matching nonbasic noncreature permanents")
    void chapterIIExchangesMatchingPermanentControl() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new GoldveinPick());
        Permanent opposingArtifact = harness.addToBattlefieldAndReturn(player2, new RavenWings());
        Permanent basicLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        addSaga(1);

        triggerChapter();
        PendingInteraction.PermanentChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(firstChoice.validPermanentIds()).contains(ownArtifact.getId(), opposingArtifact.getId())
                .doesNotContain(basicLand.getId());
        harness.handlePermanentChosen(player1, ownArtifact.getId());
        harness.handlePermanentChosen(player1, opposingArtifact.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opposingArtifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ownArtifact);
    }

    @Test
    @DisplayName("Chapter III makes a target player lose life and gains you life")
    void chapterIIILosesAndGainsLife() {
        addSaga(2);

        triggerChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void chapterICanBeDeclined() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AxgardCavalry());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AxgardCavalry());
        addSaga(0);

        triggerChapter();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first).doesNotContain(second);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(second).doesNotContain(first);
    }

    @Test
    void chapterIDoesNothingWhenBothCreaturesHaveTheSameController() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new AxgardCavalry());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AxgardCavalry());
        addSaga(0);

        triggerChapter();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first, second);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(first, second);
    }

    @Test
    void chapterIDoesNotExchangeWhenOneTargetLeaves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AxgardCavalry());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AxgardCavalry());
        addSaga(0);

        triggerChapter();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.setHand(player1, List.of(new DepartTheRealm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, second.getId());
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first).doesNotContain(second);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(first, second);
        harness.assertInHand(player2, "Axgard Cavalry");
    }

    @Test
    void chapterIExcludesOpposingHexproofCreaturesFromTargetSelection() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new AxgardCavalry());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new AxgardCavalry());
        harness.setHand(player2, List.of(new SnakeskinVeil()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, opposing.getId());
        addSaga(0);

        triggerChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(own.getId()).doesNotContain(opposing.getId());
    }

    @Test
    void chapterIIRequiresTheSecondTargetToShareACardType() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new GoldveinPick());
        Permanent otherArtifact = harness.addToBattlefieldAndReturn(player2, new RavenWings());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new AxgardCavalry());
        Permanent saga = addSaga(1);

        triggerChapter();
        harness.handlePermanentChosen(player1, artifact.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(otherArtifact.getId())
                .doesNotContain(artifact.getId(), saga.getId(), creature.getId());
    }

    @Test
    void chapterIICanBeDeclined() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GoldveinPick());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new RavenWings());
        addSaga(1);

        triggerChapter();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first).doesNotContain(second);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(second).doesNotContain(first);
    }

    @Test
    void chapterIIICanTargetItsControllerAndSacrificesTheSaga() {
        addSaga(2);

        triggerChapter();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player1, "The Trickster-God's Heist");
        harness.assertInGraveyard(player1, "The Trickster-God's Heist");
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheTricksterGodsHeist());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
