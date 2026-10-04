package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HermeticStudy;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TirelessTracker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvengingDruid.class, Shock.class, Forest.class, Island.class, GrizzlyBears.class,
        TirelessTracker.class, HermeticStudy.class})
class AvengingDruidTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the damage trigger reveals until a land and puts the rest into the graveyard")
    void acceptingDamageTriggerFindsLand() {
        Card shock = new Shock();
        Card forest = new Forest();
        Card island = new Island();
        attackAndResolveTrigger(List.of(shock, forest, island));

        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
    }

    @Test
    @DisplayName("Declining the damage trigger leaves the library unchanged")
    void decliningDamageTriggerDoesNothing() {
        Card shock = new Shock();
        Card forest = new Forest();
        attackAndResolveTrigger(List.of(shock, forest));

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof Forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock, forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(shock, forest);
    }

    @Test
    @DisplayName("Accepting with no land puts the entire library into the graveyard")
    void acceptingWithNoLandMillsTheLibrary() {
        Card shock = new Shock();
        Card bears = new GrizzlyBears();
        attackAndResolveTrigger(List.of(shock, bears));

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock, bears);
    }

    @Test
    @DisplayName("Putting the revealed land onto the battlefield triggers landfall")
    void revealedLandTriggersLandfall() {
        addCreatureReady(player1, new TirelessTracker());
        Card shock = new Shock();
        Card forest = new Forest();
        attackAndResolveTrigger(List.of(shock, forest));

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("A land on top enters untapped without revealing the next card")
    void landOnTopStopsRevealingImmediately() {
        Card forest = new Forest();
        Card shock = new Shock();
        attackAndResolveTrigger(List.of(forest, shock));

        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Forest").isTapped()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(forest, shock);
    }

    @Test
    @DisplayName("Accepting with an empty library does nothing")
    void emptyLibraryDoesNothing() {
        attackAndResolveTrigger(List.of());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Noncombat damage to an opponent triggers the reveal ability")
    void noncombatDamageToOpponentTriggers() {
        addDruidWithDamageAbility();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    @DisplayName("Noncombat damage to its controller does not trigger the reveal ability")
    void noncombatDamageToControllerDoesNotTrigger() {
        addDruidWithDamageAbility();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.activateAbility(player1, 0, null, player1.getId());
            resolveAllTriggers();
        });

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
    }

    private void addDruidWithDamageAbility() {
        Permanent druid = addCreatureReady(player1, new AvengingDruid());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HermeticStudy());
        aura.setAttachedTo(druid.getId());
    }

    private void attackAndResolveTrigger(List<Card> library) {
        Permanent druid = addCreatureReady(player1, new AvengingDruid());
        druid.setAttacking(true);
        harness.setLibrary(player1, library);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
