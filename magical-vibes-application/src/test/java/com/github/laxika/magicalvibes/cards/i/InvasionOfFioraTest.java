package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MarchesaResoluteMonarch;
import com.github.laxika.magicalvibes.cards.z.ZurgoHelmsmasher;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({
        GrizzlyBears.class,
        InvasionOfFiora.class,
        MarchesaResoluteMonarch.class,
        ZurgoHelmsmasher.class
})
class InvasionOfFioraTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing both modes destroys legendary and nonlegendary creatures")
    void choosingBothModesDestroysAllCreatures() {
        Permanent legendary = harness.addToBattlefieldAndReturn(player2, new ZurgoHelmsmasher());
        Permanent nonlegendary = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castInvasion(ChooseOneEffect.encodeModeSelection(1, 2, new int[]{0, 1}));

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(legendary.getId())
                        || permanent.getId().equals(nonlegendary.getId()));
    }

    @Test
    @DisplayName("Choosing only the legendary mode leaves nonlegendary creatures alive")
    void choosingLegendaryModeOnly() {
        Permanent legendary = harness.addToBattlefieldAndReturn(player2, new ZurgoHelmsmasher());
        Permanent nonlegendary = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castInvasion(0);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(legendary.getId()))
                .anyMatch(permanent -> permanent.getId().equals(nonlegendary.getId()));
    }

    @Test
    @DisplayName("Marchesa removes all counters from up to one permanent when she attacks")
    void marchesaRemovesCountersWhenAttacking() {
        Permanent marchesa = addCreatureReady(player1, new MarchesaResoluteMonarch());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackers(player1, List.of(0));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(marchesa.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Marchesa draws and loses 1 life at upkeep when no combat damage was dealt")
    void marchesaUpkeepTriggersWithoutCombatDamage() {
        harness.addToBattlefield(player1, new MarchesaResoluteMonarch());
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawn));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Marchesa does not trigger at upkeep after combat damage was dealt")
    void marchesaUpkeepDoesNotTriggerAfterCombatDamage() {
        harness.addToBattlefield(player1, new MarchesaResoluteMonarch());
        addCreatureReady(player2, new GrizzlyBears());
        Card cardInLibrary = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(cardInLibrary));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(player2, List.of(0));
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.of());
        harness.passUntil(player1, TurnStep.UPKEEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    private void castInvasion(int modeSelection) {
        Card invasion = new InvasionOfFiora();
        harness.setHand(player1, List.of(invasion));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        gs.playCard(gd, player1, 0, modeSelection, player2.getId(), null);
    }
}
