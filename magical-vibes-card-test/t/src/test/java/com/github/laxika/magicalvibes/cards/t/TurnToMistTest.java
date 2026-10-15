package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DevotedDruid;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TurnToMist.class, DevotedDruid.class, Forest.class})
class TurnToMistTest extends BaseCardTest {

    private void addTurnToMistMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Exiles the target creature and schedules its return")
    void exilesTargetCreature() {
        harness.setHand(player1, java.util.List.of(new TurnToMist()));
        harness.addToBattlefield(player2, new DevotedDruid());
        addTurnToMistMana();

        UUID bearsId = harness.getPermanentId(player2, "Devoted Druid");
        harness.castAndResolveInstant(player1, 0, bearsId);

        harness.assertNotOnBattlefield(player2, "Devoted Druid");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Devoted Druid"));
        assertThat(gd.getDelayedActions(PendingExileReturn.class))
                .anyMatch(per -> per.card().getName().equals("Devoted Druid"));
    }

    @Test
    @DisplayName("Exiled creature returns at the next end step under its owner's control")
    void returnsAtEndStep() {
        harness.setHand(player1, java.util.List.of(new TurnToMist()));
        harness.addToBattlefield(player2, new DevotedDruid());
        addTurnToMistMana();

        UUID bearsId = harness.getPermanentId(player2, "Devoted Druid");
        harness.castAndResolveInstant(player1, 0, bearsId);

        harness.assertNotOnBattlefield(player2, "Devoted Druid");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Devoted Druid");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Devoted Druid"));
    }

    @Test
    void castDuringEndStepWaitsForTheFollowingTurnsEndStep() {
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, java.util.List.of(new TurnToMist()));
        harness.addToBattlefield(player2, new DevotedDruid());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Devoted Druid"));
        harness.assertNotOnBattlefield(player2, "Devoted Druid");
        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        harness.assertNotOnBattlefield(player2, "Devoted Druid");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Devoted Druid");
    }

    @Test
    void stolenCreatureReturnsToOwnerAsANewPermanent() {
        var original = harness.addToBattlefieldAndReturn(player1, new DevotedDruid());
        gd.stolenCreatures.put(original.getId(), player2.getId());
        original.tap();
        harness.setHand(player1, java.util.List.of(new TurnToMist()));
        addTurnToMistMana();

        harness.castAndResolveInstant(player1, 0, original.getId());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.assertNotOnBattlefield(player2, "Devoted Druid");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Devoted Druid");
        harness.assertOnBattlefield(player2, "Devoted Druid");
        var returned = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(returned.getId()).isNotEqualTo(original.getId());
        assertThat(returned.isTapped()).isFalse();
    }

    @Test
    void exiledTokenDoesNotReturn() {
        var token = new DevotedDruid();
        token.setToken(true);
        harness.addToBattlefield(player2, token);
        harness.setHand(player1, java.util.List.of(new TurnToMist()));
        addTurnToMistMana();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Devoted Druid"));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        if (!gd.stack.isEmpty()) harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Devoted Druid");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void delayedReturnDoesNotFollowACardThatLeavesAndReentersExile() {
        var druid = new DevotedDruid();
        harness.addToBattlefield(player2, druid);
        harness.setHand(player1, java.util.List.of(new TurnToMist()));
        addTurnToMistMana();
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Devoted Druid"));

        assertThat(gd.removeFromExile(druid.getId())).isTrue();
        harness.setExile(player2, java.util.List.of(druid));
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Devoted Druid");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(druid);
    }

    @Test
    void spellDoesNothingWhenItsTargetLeavesBeforeResolution() {
        var druid = new DevotedDruid();
        var target = harness.addToBattlefieldAndReturn(player2, druid);
        harness.setHand(player1, java.util.List.of(new TurnToMist()));
        addTurnToMistMana();
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(druid);

        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Devoted Druid");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.setHand(player1, java.util.List.of(new TurnToMist()));
        harness.addToBattlefield(player2, new Forest());
        addTurnToMistMana();

        UUID forestId = harness.getPermanentId(player2, "Forest");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, forestId))
                .isInstanceOf(IllegalStateException.class);
    }
}

