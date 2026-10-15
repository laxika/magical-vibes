package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.ElegantParlor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HedronCrab;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.s.SinisterStarfish;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({MirkoObsessiveTheorist.class, ElegantParlor.class, HedronCrab.class,
        Memnite.class, GrizzlyBears.class, SinisterStarfish.class})
class MirkoObsessiveTheoristTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever you surveil, Mirko gets a +1/+1 counter")
    void surveilingPutsCounterOnMirko() {
        Permanent mirko = harness.addToBattlefieldAndReturn(player1, new MirkoObsessiveTheorist());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new ElegantParlor()));
        harness.playLand(player1, 0);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(mirko.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("End step returns only a creature card with power less than Mirko and gives it a finality counter")
    void endStepReturnsStrictlyLowerPowerCreatureWithFinalityCounter() {
        Permanent mirko = harness.addToBattlefieldAndReturn(player1, new MirkoObsessiveTheorist());
        Card lowerPower = new HedronCrab();
        Card equalPower = new Memnite();
        Card greaterPower = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(lowerPower, equalPower, greaterPower));

        advanceToEndStep(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(lowerPower.getId());

        harness.handleMultipleCardsChosen(player1, List.of(lowerPower.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(lowerPower.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(equalPower, greaterPower);
        assertThat(mirko.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("The optional end-step return can be declined")
    void endStepReturnCanBeDeclined() {
        harness.addToBattlefield(player1, new MirkoObsessiveTheorist());
        Card lowerPower = new HedronCrab();
        harness.setGraveyard(player1, List.of(lowerPower));

        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(lowerPower.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(lowerPower);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(lowerPower.getId()));
    }

    @Test
    @DisplayName("Surveilling an empty library still puts a counter on Mirko")
    void surveillingEmptyLibraryStillTriggers() {
        Permanent mirko = harness.addToBattlefieldAndReturn(player1, new MirkoObsessiveTheorist());
        harness.addToBattlefieldAndReturn(player1, new SinisterStarfish()).setSummoningSick(false);
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(mirko.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Keeping the surveilled card on top still puts a counter on Mirko")
    void keepingSurveilledCardStillTriggers() {
        Permanent mirko = harness.addToBattlefieldAndReturn(player1, new MirkoObsessiveTheorist());
        harness.addToBattlefieldAndReturn(player1, new SinisterStarfish()).setSummoningSick(false);
        Card topCard = new SinisterStarfish();
        harness.setLibrary(player1, List.of(topCard));

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(mirko.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("An opponent surveilling does not put a counter on Mirko")
    void opponentsSurveilDoesNotTrigger() {
        Permanent mirko = harness.addToBattlefieldAndReturn(player1, new MirkoObsessiveTheorist());
        harness.addToBattlefieldAndReturn(player2, new SinisterStarfish()).setSummoningSick(false);
        harness.setLibrary(player2, List.of());
        harness.ensurePriority(player2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(mirko.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Mirko does not return creatures at an opponent's end step")
    void opponentsEndStepDoesNotTrigger() {
        harness.addToBattlefield(player1, new MirkoObsessiveTheorist());
        Card creature = new SinisterStarfish();
        harness.setGraveyard(player1, List.of(creature));

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
    }

    @Test
    @DisplayName("The target's power must still be less than Mirko's on resolution")
    void powerRestrictionIsRecheckedOnResolution() {
        Permanent mirko = harness.addToBattlefieldAndReturn(player1, new MirkoObsessiveTheorist());
        mirko.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card creature = new Memnite();
        harness.setGraveyard(player1, List.of(creature));
        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        mirko.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(mirko);
    }

    @Test
    @DisplayName("Mirko's last known power is used after he leaves the battlefield")
    void usesLastKnownPowerWhenMirkoDiesInResponse() {
        Permanent mirko = harness.addToBattlefieldAndReturn(player1, new MirkoObsessiveTheorist());
        mirko.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Card creature = new Memnite();
        harness.setGraveyard(player1, List.of(creature));
        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        mirko.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId())
                        && permanent.getCounterCount(CounterType.FINALITY) == 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(mirko.getCard()).doesNotContain(creature);
    }

    @Test
    @DisplayName("A returned creature is exiled rather than dying because of its finality counter")
    void returnedCreatureIsExiledInsteadOfDying() {
        harness.addToBattlefield(player1, new MirkoObsessiveTheorist());
        Card creature = new SinisterStarfish();
        harness.setGraveyard(player1, List.of(creature));
        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst().orElseThrow();
        returned.setMarkedDamage(3);
        harness.runStateBasedActions();

        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(returned);
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player, TurnStep.END_STEP);
    }
}
