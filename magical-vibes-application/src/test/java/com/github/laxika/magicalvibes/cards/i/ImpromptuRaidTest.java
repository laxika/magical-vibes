package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DevotedDruid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImpromptuRaid.class, DevotedDruid.class, Forest.class})
class ImpromptuRaidTest extends BaseCardTest {

    @Test
    @DisplayName("Revealed creature enters with haste and is scheduled for end-step sacrifice")
    void creatureEntersWithHasteAndEndStepSacrifice() {
        harness.addToBattlefield(player1, new ImpromptuRaid());
        Card creature = new DevotedDruid();
        harness.setLibrary(player1, List.of(creature));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Devoted Druid");
        assertThat(bears.getCard().getId()).isEqualTo(creature.getId());
        assertThat(bears.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creature.getId()));
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(bears.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));
    }

    @Test
    @DisplayName("Revealed non-creature card is put into the graveyard")
    void nonCreatureCardPutIntoGraveyard() {
        harness.addToBattlefield(player1, new ImpromptuRaid());
        Card land = new Forest();
        harness.setLibrary(player1, List.of(land));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(land.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(land.getId()));
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class)).isEmpty();
    }

    @Test
    void emptyLibraryDoesNothingAndDoesNotCauseDrawLoss() {
        harness.addToBattlefield(player1, new ImpromptuRaid());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class)).isEmpty();
        assertThat(gd.status).isEqualTo(com.github.laxika.magicalvibes.model.GameStatus.RUNNING);
    }

    @Test
    void creatureIsSacrificedWhenNextEndStepTriggerResolves() {
        harness.addToBattlefield(player1, new ImpromptuRaid());
        Card creature = new DevotedDruid();
        harness.setLibrary(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Devoted Druid");
        assertThat(gd.stack).isNotEmpty();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Devoted Druid");
        harness.assertInGraveyard(player1, "Devoted Druid");
    }

    @Test
    void activationDuringEndStepWaitsForFollowingTurnsEndStep() {
        harness.addToBattlefield(player1, new ImpromptuRaid());
        Card creature = new DevotedDruid();
        harness.setLibrary(player1, List.of(creature));
        harness.forceStep(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Devoted Druid");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Devoted Druid");
        harness.assertInGraveyard(player1, "Devoted Druid");
    }

    @Test
    void creatureControlledByOpponentCannotBeSacrificedByActivatingPlayer() {
        harness.addToBattlefield(player1, new ImpromptuRaid());
        Card creature = new DevotedDruid();
        harness.setLibrary(player1, List.of(creature));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Devoted Druid");
        gd.playerBattlefields.get(player1.getId()).remove(bears);
        gd.playerBattlefields.get(player2.getId()).add(bears);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).singleElement()
                .satisfies(trigger -> assertThat(trigger.getControllerId()).isEqualTo(player1.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Devoted Druid");
        harness.assertNotInGraveyard(player1, "Devoted Druid");
    }
}
