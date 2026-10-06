package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScavengersTalent.class, GrizzlyBears.class, Shock.class})
class ScavengersTalentTest extends BaseCardTest {

    @Test
    @DisplayName("Creates only one Food token for multiple creature deaths in a turn")
    void createsFoodOncePerTurnForCreatureDeaths() {
        harness.addToBattlefield(player1, new ScavengersTalent());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        killWithShock(first);
        assertThat(foodCount()).isEqualTo(1);

        killWithShock(second);
        assertThat(foodCount()).isEqualTo(1);
    }

    @Test
    void foodTriggerResetsOnTheNextPlayersTurn() {
        harness.addToBattlefield(player1, new ScavengersTalent());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        killWithShock(first);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, second.getId());
        harness.passBothPriorities();

        assertThat(foodCount()).isEqualTo(2);
    }

    @Test
    void opposingCreatureDeathsDoNotCreateFood() {
        harness.addToBattlefield(player1, new ScavengersTalent());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        killWithShock(creature);

        assertThat(foodCount()).isZero();
    }

    @Test
    void cannotGainLevelThreeBeforeLevelTwo() {
        harness.addToBattlefield(player1, new ScavengersTalent());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotGainAClassLevelAtInstantSpeed() {
        harness.addToBattlefield(player1, new ScavengersTalent());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("At level 3, sacrifices three other nonland permanents and returns a creature with finality")
    void levelThreeSacrificesThenReturnsCreature() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new ScavengersTalent());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent fourth = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GrizzlyBears graveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCreature));

        levelUp(talent, 0, 1);
        levelUp(talent, 1, 2);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(3);
        assertThat(choice.validIds()).containsExactly(first.getId(), second.getId(), third.getId(), fourth.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.GraveyardChoice.class);
        int graveyardIndex = gd.playerGraveyards.get(player1.getId()).indexOf(graveyardCreature);
        harness.handleGraveyardCardChosen(player1, graveyardIndex);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(graveyardCreature.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fourth).doesNotContain(first, second, third);
    }

    @Test
    void gainingClassLevelsDoesNotPlaceLevelCounters() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new ScavengersTalent());

        levelUp(talent, 0, 1);
        levelUp(talent, 1, 2);

        assertThat(talent.getCounterCount(CounterType.LEVEL)).isZero();
        assertThat(talent.getClassLevel()).isEqualTo(3);
    }

    @Test
    void foodCanBeSacrificedForLifeAtLevelOneWithoutMilling() {
        harness.addToBattlefield(player1, new ScavengersTalent());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        killWithShock(creature);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        Permanent food = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(food), null, null);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 3);
        assertThat(foodCount()).isZero();
    }

    @Test
    void sacrificingFoodAtLevelTwoMillsTheChosenOpponent() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new ScavengersTalent());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        killWithShock(creature);
        levelUp(talent, 0, 1);
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        GrizzlyBears third = new GrizzlyBears();
        harness.setLibrary(player2, List.of(first, second, third));
        Permanent food = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst().orElseThrow();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(food), null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(third);
    }

    @Test
    void decliningLevelThreeLeavesPermanentsAndGraveyardUnchanged() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new ScavengersTalent());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        levelUp(talent, 0, 1);
        levelUp(talent, 1, 2);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(talent, first, second, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
    }

    @Test
    void levelThreeCannotSacrificeOnlyTwoOtherPermanents() {
        Permanent talent = harness.addToBattlefieldAndReturn(player1, new ScavengersTalent());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        levelUp(talent, 0, 1);
        levelUp(talent, 1, 2);

        advanceToEndStep(player1);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(talent, first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void killWithShock(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private int foodCount() {
        return (int) gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .filter(permanent -> permanent.getCard().getName().equals("Food"))
                .count();
    }

    private void levelUp(Permanent talent, int abilityIndex, int genericMana) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, genericMana);
        int talentIndex = gd.playerBattlefields.get(player1.getId()).indexOf(talent);
        harness.activateAbility(player1, talentIndex, abilityIndex, null, null);
        harness.passBothPriorities();
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
    }
}
