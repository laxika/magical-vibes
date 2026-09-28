package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FaithfulSquire;
import com.github.laxika.magicalvibes.cards.f.Frostling;
import com.github.laxika.magicalvibes.cards.g.GoblinCohort;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmptyShrineKannushi.class, FaithfulSquire.class, Frostling.class, GoblinCohort.class})
class EmptyShrineKannushiTest extends BaseCardTest {

    private void attackWithKannushi() {
        addCreatureReady(player1, new EmptyShrineKannushi());
        declareAttackersAndPrepareBlockers(List.of(0));
    }

    @Test
    @DisplayName("A white creature cannot block Empty-Shrine Kannushi, which is itself white")
    void whiteCreatureCannotBlock() {
        attackWithKannushi();

        Permanent blocker = addCreatureReady(player2, new FaithfulSquire());

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("A red creature can block while its controller controls no red permanent")
    void redCreatureCanBlockWithoutRedPermanent() {
        attackWithKannushi();

        Permanent blocker = addCreatureReady(player2, new GoblinCohort());

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Gaining a red permanent extends the protection to red creatures")
    void redCreatureCannotBlockOnceControllerHasRedPermanent() {
        attackWithKannushi();

        addCreatureReady(player1, new Frostling());

        Permanent blocker = addCreatureReady(player2, new GoblinCohort());

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from red prevents a red creature ability from targeting it")
    void redAbilityCannotTargetWhenControllerHasRedPermanent() {
        Permanent kannushi = addCreatureReady(player1, new EmptyShrineKannushi());
        addCreatureReady(player1, new Frostling());
        addCreatureReady(player2, new Frostling());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, kannushi.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }

    @Test
    @DisplayName("An opponent's red permanent does not grant protection from red")
    void opponentRedPermanentDoesNotGrantProtection() {
        attackWithKannushi();

        Permanent blocker = addCreatureReady(player2, new GoblinCohort());
        addCreatureReady(player2, new GoblinCohort());

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can die simultaneously with another creature without reevaluating protection off the battlefield")
    void canDieSimultaneouslyWithAnotherCreature() {
        Permanent kannushi = harness.addToBattlefieldAndReturn(player1, new EmptyShrineKannushi());
        Permanent frostling = harness.addToBattlefieldAndReturn(player1, new Frostling());
        kannushi.setMarkedDamage(1);
        frostling.setMarkedDamage(1);

        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Empty-Shrine Kannushi", "Frostling");
    }

    @Test
    @DisplayName("Protection from red prevents combat damage from a red creature")
    void preventsCombatDamageFromRedCreature() {
        Permanent kannushi = addCreatureReady(player1, new EmptyShrineKannushi());
        addCreatureReady(player1, new Frostling());
        addCreatureReady(player2, new Frostling());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(kannushi.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Empty-Shrine Kannushi", "Frostling");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Frostling");
    }

    @Test
    @DisplayName("Can die in combat while damage triggers are collected")
    void canDieInCombatWhileDamageTriggersAreCollected() {
        Permanent kannushi = addCreatureReady(player1, new EmptyShrineKannushi());
        addCreatureReady(player2, new GoblinCohort());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Empty-Shrine Kannushi");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.getMarkedDamage()).isEqualTo(1));
    }
}
