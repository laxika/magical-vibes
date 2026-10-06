package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RollRollRollRoll.class, GrizzlyBears.class, Forest.class})
class RollRollRollRollTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I can exile a creature you control and return it at the next end step")
    void chapterIExilesAndReturnsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castSaga();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(creature.getId())
                .doesNotContain(findPermanent(player2, "Grizzly Bears").getId());

        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));

        advanceToEndStep();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Chapter II can exile a land you control")
    void chapterIIExilesLand() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new RollRollRollRoll());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        saga.setCounterCount(CounterType.LORE, 1);

        triggerChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(land.getId())
                .doesNotContain(saga.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Forest"));
    }

    @Test
    @DisplayName("A chapter can be declined when no target is chosen")
    void chapterCanBeDeclined() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castSaga();

        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3})
    @DisplayName("Later chapters flicker a land, and the final chapter sacrifices the Saga")
    void laterChaptersReturnLand(int initialLore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new RollRollRollRoll());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.tap();
        saga.setCounterCount(CounterType.LORE, initialLore);

        triggerChapter();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        if (initialLore == 3) {
            harness.assertNotOnBattlefield(player1, "Roll-Roll-Roll-Roll");
            harness.assertInGraveyard(player1, "Roll-Roll-Roll-Roll");
        } else {
            harness.assertOnBattlefield(player1, "Roll-Roll-Roll-Roll");
        }

        advanceToEndStep();

        Permanent returned = findPermanent(player1, "Forest");
        assertThat(returned.getId()).isNotEqualTo(land.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A creature owned by the opponent returns under its owner's control")
    void stolenCreatureReturnsToOwner() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        gd.stolenCreatures.put(creature.getId(), player2.getId());
        castSaga();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));

        advanceToEndStep();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("A chosen creature that changes controllers is not exiled")
    void targetMustStillBeControlledAtResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castSaga();
        harness.handlePermanentChosen(player1, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);
        gd.stolenCreatures.put(creature.getId(), player1.getId());

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("With no legal targets, the final chapter resolves and the Saga is sacrificed")
    void finalChapterWithoutLegalTargets() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new RollRollRollRoll());
        harness.addToBattlefield(player2, new GrizzlyBears());
        saga.setCounterCount(CounterType.LORE, 3);

        triggerChapter();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Roll-Roll-Roll-Roll");
        harness.assertNotOnBattlefield(player1, "Roll-Roll-Roll-Roll");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private void castSaga() {
        harness.setHand(player1, List.of(new RollRollRollRoll()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

}
