package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BurrentonBombardier;
import com.github.laxika.magicalvibes.cards.d.Disperse;
import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.g.GameTrailChangeling;
import com.github.laxika.magicalvibes.cards.n.NamelessInversion;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WinnowerPatrol.class, ElvishWarrior.class, BurrentonBombardier.class,
        Disperse.class, GameTrailChangeling.class, NamelessInversion.class})
class WinnowerPatrolTest extends BaseCardTest {

    @Test
    @DisplayName("Kinship prompts to reveal when the top card shares a creature type")
    void kinshipPromptsWhenSharedType() {
        addCreatureReady(player1, new WinnowerPatrol());
        harness.setLibrary(player1, List.of(new ElvishWarrior()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Revealing the shared-type card puts a +1/+1 counter on this creature")
    void revealAddsCounter() {
        Permanent patrol = addCreatureReady(player1, new WinnowerPatrol());
        harness.setLibrary(player1, List.of(new ElvishWarrior()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(patrol.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining to reveal leaves the creature without a counter")
    void decliningDoesNothing() {
        Permanent patrol = addCreatureReady(player1, new WinnowerPatrol());
        harness.setLibrary(player1, List.of(new ElvishWarrior()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(patrol.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("No reveal prompt when the top card shares no creature type")
    void noSharedTypeNoPrompt() {
        addCreatureReady(player1, new WinnowerPatrol());
        harness.setLibrary(player1, List.of(new BurrentonBombardier()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Trigger does nothing with an empty library")
    void emptyLibraryDoesNothing() {
        addCreatureReady(player1, new WinnowerPatrol());
        harness.setLibrary(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Revealing a changeling leaves it on top of the library")
    void revealingChangelingLeavesCardOnTop() {
        Permanent patrol = addCreatureReady(player1, new WinnowerPatrol());
        GameTrailChangeling top = new GameTrailChangeling();
        harness.setLibrary(player1, List.of(top));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(patrol.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    @DisplayName("A kindred instant with changeling can satisfy kinship")
    void kindredInstantCanSatisfyKinship() {
        Permanent patrol = addCreatureReady(player1, new WinnowerPatrol());
        harness.setLibrary(player1, List.of(new NamelessInversion()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(patrol.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Kinship does not trigger during the opponent's upkeep")
    void opponentUpkeepDoesNotTrigger() {
        Permanent patrol = addCreatureReady(player1, new WinnowerPatrol());
        harness.setLibrary(player1, List.of(new ElvishWarrior()));

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(patrol.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Kinship can still reveal using last known types after the source leaves")
    void canRevealAfterSourceLeavesBattlefield() {
        Permanent patrol = addCreatureReady(player1, new WinnowerPatrol());
        ElvishWarrior top = new ElvishWarrior();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new Disperse()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, patrol.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(patrol);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(patrol.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Kinship uses creature types at resolution after Nameless Inversion")
    void losingCreatureTypesBeforeResolutionPreventsReveal() {
        Permanent patrol = addCreatureReady(player1, new WinnowerPatrol());
        patrol.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLibrary(player1, List.of(new ElvishWarrior()));
        harness.setHand(player1, List.of(new NamelessInversion()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, patrol.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(patrol);
        assertThat(gqs.effectiveCreatureSubtypes(gd, patrol)).isEmpty();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(patrol.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
