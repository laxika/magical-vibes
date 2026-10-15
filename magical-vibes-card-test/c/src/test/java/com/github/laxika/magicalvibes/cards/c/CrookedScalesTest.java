package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EdgarKingOfFigaro;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrookedScales.class, FreshVolunteers.class, EdgarKingOfFigaro.class})
class CrookedScalesTest extends BaseCardTest {

    @Test
    @DisplayName("Ability requires one creature controlled by each side")
    void requiresOneCreatureControlledByEachSide() {
        Permanent scales = addCreatureReady(player1, new CrookedScales());
        Permanent firstOwnCreature = addCreatureReady(player1, new FreshVolunteers());
        Permanent secondOwnCreature = addCreatureReady(player1, new FreshVolunteers());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(scales),
                0,
                List.of(firstOwnCreature.getId(), secondOwnCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability destroys one of the chosen creatures after its flip process")
    void destroysChosenCreatureAfterFlipProcess() {
        Permanent scales = addCreatureReady(player1, new CrookedScales());
        Permanent ownCreature = addCreatureReady(player1, new FreshVolunteers());
        Permanent opposingCreature = addCreatureReady(player2, new FreshVolunteers());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbilityWithMultiTargets(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(scales),
                0,
                List.of(ownCreature.getId(), opposingCreature.getId()));
        harness.passBothPriorities();

        if (!gd.pendingMayAbilities.isEmpty()) {
            harness.handleMayAbilityChosen(player1, true);
        }
        if (!gd.pendingMayAbilities.isEmpty()) {
            harness.handleMayAbilityChosen(player1, false);
        }

        List<String> flipLogs = gd.gameLog.stream()
                .map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Crooked Scales"))
                .toList();
        assertThat(flipLogs).isNotEmpty();

        boolean lastFlipWon = flipLogs.getLast().contains("wins the coin flip");
        assertThat(gd.playerBattlefields.get(player1.getId()).contains(ownCreature))
                .isEqualTo(lastFlipWon);
        assertThat(gd.playerBattlefields.get(player2.getId()).contains(opposingCreature))
                .isEqualTo(!lastFlipWon);
    }

    @Test
    @DisplayName("A winning flip destroys only the opponent's target creature")
    void winningFlipDestroysOnlyOpponentTarget() {
        Permanent scales = addCreatureReady(player1, new CrookedScales());
        Permanent ownCreature = addCreatureReady(player1, new FreshVolunteers());
        Permanent opposingCreature = addCreatureReady(player2, new FreshVolunteers());
        harness.addToBattlefield(player1, new EdgarKingOfFigaro());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithMultiTargets(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(scales),
                0,
                List.of(ownCreature.getId(), opposingCreature.getId()));
        harness.passBothPriorities();

        assertThat(gameLogContains("wins the coin flip for Crooked Scales")).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingCreature);
    }

    @Test
    @DisplayName("A winning flip still destroys the opponent's target when your target has left")
    void winningFlipResolvesWithOnlyOpponentTargetLegal() {
        Permanent scales = addCreatureReady(player1, new CrookedScales());
        Permanent ownCreature = addCreatureReady(player1, new FreshVolunteers());
        Permanent opposingCreature = addCreatureReady(player2, new FreshVolunteers());
        harness.addToBattlefield(player1, new EdgarKingOfFigaro());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithMultiTargets(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(scales), 0,
                List.of(ownCreature.getId(), opposingCreature.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(ownCreature);
        gd.playerGraveyards.get(player1.getId()).add(ownCreature.getCard());
        harness.passBothPriorities();

        assertThat(gameLogContains("wins the coin flip for Crooked Scales")).isTrue();
        harness.assertNotOnBattlefield(player2, "Fresh Volunteers");
        harness.assertInGraveyard(player2, "Fresh Volunteers");
    }

    @Test
    @DisplayName("A winning flip cannot destroy an opponent's target that is now controlled by you")
    void winningFlipDoesNotAffectTargetWhoseControllerChanged() {
        Permanent scales = addCreatureReady(player1, new CrookedScales());
        Permanent ownCreature = addCreatureReady(player1, new FreshVolunteers());
        Permanent opposingCreature = addCreatureReady(player2, new FreshVolunteers());
        harness.addToBattlefield(player1, new EdgarKingOfFigaro());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithMultiTargets(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(scales), 0,
                List.of(ownCreature.getId(), opposingCreature.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(opposingCreature);
        gd.playerBattlefields.get(player1.getId()).add(opposingCreature);
        harness.passBothPriorities();

        assertThat(gameLogContains("wins the coin flip for Crooked Scales")).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature, opposingCreature);
        harness.assertNotInGraveyard(player1, "Fresh Volunteers");
        harness.assertNotInGraveyard(player2, "Fresh Volunteers");
    }

    @Test
    @DisplayName("The ability does not flip a coin when both targets have left")
    void doesNotResolveWithBothTargetsIllegal() {
        Permanent scales = addCreatureReady(player1, new CrookedScales());
        Permanent ownCreature = addCreatureReady(player1, new FreshVolunteers());
        Permanent opposingCreature = addCreatureReady(player2, new FreshVolunteers());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbilityWithMultiTargets(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(scales), 0,
                List.of(ownCreature.getId(), opposingCreature.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(ownCreature);
        gd.playerGraveyards.get(player1.getId()).add(ownCreature.getCard());
        gd.playerBattlefields.get(player2.getId()).remove(opposingCreature);
        gd.playerGraveyards.get(player2.getId()).add(opposingCreature.getCard());
        harness.passBothPriorities();

        assertThat(gameLogContains("coin flip for Crooked Scales")).isFalse();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The activated ability resolves after Crooked Scales leaves the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent scales = addCreatureReady(player1, new CrookedScales());
        Permanent ownCreature = addCreatureReady(player1, new FreshVolunteers());
        Permanent opposingCreature = addCreatureReady(player2, new FreshVolunteers());
        harness.addToBattlefield(player1, new EdgarKingOfFigaro());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithMultiTargets(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(scales), 0,
                List.of(ownCreature.getId(), opposingCreature.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(scales);
        gd.playerGraveyards.get(player1.getId()).add(scales.getCard());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Fresh Volunteers");
        harness.assertNotOnBattlefield(player2, "Fresh Volunteers");
        harness.assertInGraveyard(player2, "Fresh Volunteers");
    }

    @Test
    @DisplayName("Declining to repeat destroys your target on a loss without another flip")
    void decliningRepeatFinishesAfterOneFlip() {
        Permanent scales = addCreatureReady(player1, new CrookedScales());
        Permanent ownCreature = addCreatureReady(player1, new FreshVolunteers());
        Permanent opposingCreature = addCreatureReady(player2, new FreshVolunteers());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbilityWithMultiTargets(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(scales), 0,
                List.of(ownCreature.getId(), opposingCreature.getId()));
        assertThat(scales.isTapped()).isTrue();
        harness.passBothPriorities();
        if (!gd.pendingMayAbilities.isEmpty()) {
            harness.handleMayAbilityChosen(player1, false);
        }

        List<String> flipLogs = gd.gameLog.stream()
                .map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Crooked Scales"))
                .toList();
        assertThat(flipLogs).hasSize(1);
        boolean won = flipLogs.getFirst().contains("wins the coin flip");
        assertThat(gd.playerBattlefields.get(player1.getId()).contains(ownCreature)).isEqualTo(won);
        assertThat(gd.playerBattlefields.get(player2.getId()).contains(opposingCreature)).isEqualTo(!won);
        assertThat(gd.playerGraveyards.get((won ? player2 : player1).getId()))
                .contains((won ? opposingCreature : ownCreature).getCard());
        assertThat(gd.pendingMayAbilities).isEmpty();
    }
}
