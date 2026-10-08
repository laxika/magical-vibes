package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.j.JaceTheLivingGuildpact;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulOfShandalar.class, RuneclawBear.class, JaceTheLivingGuildpact.class})
class SoulOfShandalarTest extends BaseCardTest {

    @Test
    @DisplayName("Battlefield ability burns the target player and a creature they control")
    void battlefieldAbilityHitsPlayerAndTheirCreature() {
        addReadySoul();
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId(), bear.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
    }

    @Test
    @DisplayName("The creature target is optional")
    void creatureTargetIsOptional() {
        addReadySoul();
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(bear.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A targeted planeswalker takes the damage and its controller's creature is hit too")
    void hitsPlaneswalkerAndItsControllersCreature() {
        addReadySoul();
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceTheLivingGuildpact());
        jace.setCounterCount(CounterType.LOYALTY, 5);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(jace.getId(), bear.getId()));
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
    }

    @Test
    @DisplayName("A creature the targeted player does not control is an illegal second target")
    void rejectsCreatureOfAnotherController() {
        addReadySoul();
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(player1, 0, 0,
                List.of(player2.getId(), ownBear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Graveyard ability exiles the card and deals the same damage")
    void graveyardAbilityExilesAndBurns() {
        Card soul = new SoulOfShandalar();
        harness.setGraveyard(player1, List.of(soul));
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateGraveyardAbilityWithTargets(player1, 0, 0, List.of(player2.getId(), bear.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(soul.getId()));
    }

    @Test
    @DisplayName("Graveyard ability rejects an illegal creature target before paying any cost")
    void graveyardAbilityRejectsIllegalCreatureTarget() {
        Card soul = new SoulOfShandalar();
        harness.setGraveyard(player1, List.of(soul));
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithTargets(player1, 0, 0,
                List.of(player2.getId(), ownBear.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(soul);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("Planeswalker still takes damage when its creature changes controller")
    void planeswalkerRemainsLegalWhenCreatureChangesController(boolean fromGraveyard) {
        prepareAbility(fromGraveyard);
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceTheLivingGuildpact());
        jace.setCounterCount(CounterType.LOYALTY, 5);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.RED, 5);

        activateDamage(fromGraveyard, List.of(jace.getId(), bear.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(bear);
        gd.playerBattlefields.get(player1.getId()).add(bear);
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(bear.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("Planeswalker still takes damage after changing controller away from its creature")
    void planeswalkerRemainsLegalWhenItChangesController(boolean fromGraveyard) {
        prepareAbility(fromGraveyard);
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceTheLivingGuildpact());
        jace.setCounterCount(CounterType.LOYALTY, 5);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.RED, 5);

        activateDamage(fromGraveyard, List.of(jace.getId(), bear.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(jace);
        gd.playerBattlefields.get(player1.getId()).add(jace);
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(bear.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("Cannot determine the creature's required controller when the planeswalker leaves")
    void creatureIsNotDamagedWhenPlaneswalkerLeaves(boolean fromGraveyard) {
        prepareAbility(fromGraveyard);
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceTheLivingGuildpact());
        jace.setCounterCount(CounterType.LOYALTY, 5);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.RED, 5);

        activateDamage(fromGraveyard, List.of(jace.getId(), bear.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(jace);
        harness.setGraveyard(player2, List.of(jace.getCard()));
        harness.passBothPriorities();

        assertThat(bear.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Graveyard exile is paid immediately and the creature target may be omitted")
    void graveyardAbilityPaysExileBeforeResolutionAndCanTargetOnlyPlaneswalker() {
        Card soul = new SoulOfShandalar();
        harness.setGraveyard(player1, List.of(soul));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        Permanent jace = harness.addToBattlefieldAndReturn(player2, new JaceTheLivingGuildpact());
        jace.setCounterCount(CounterType.LOYALTY, 5);
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateGraveyardAbilityWithTargets(player1, 0, 0, List.of(jace.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(soul);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.passBothPriorities();
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Battlefield activation works while summoning sick and tapped, even if source leaves")
    void battlefieldAbilityDoesNotRequireTapOrSurvivingSource() {
        Permanent soul = harness.addToBattlefieldAndReturn(player1, new SoulOfShandalar());
        soul.setSummoningSick(true);
        soul.setTapped(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(soul);
        harness.setGraveyard(player1, List.of(soul.getCard()));
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }


    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("Neither ability can target a creature as its required first target")
    void rejectsCreatureAsFirstTarget(boolean fromGraveyard) {
        prepareAbility(fromGraveyard);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> activateDamage(fromGraveyard, List.of(bear.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bear.getMarkedDamage()).isZero();
        if (fromGraveyard) {
            harness.assertInGraveyard(player1, "Soul of Shandalar");
            assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("Both abilities require two red mana")
    void rejectsInsufficientRedMana(boolean fromGraveyard) {
        prepareAbility(fromGraveyard);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> activateDamage(fromGraveyard, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);
        if (fromGraveyard) {
            harness.assertInGraveyard(player1, "Soul of Shandalar");
            assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        }
    }

    @Test
    @DisplayName("Soul may damage its controller and a creature that controller controls")
    void mayTargetItsOwnControllerAndCreature() {
        addReadySoul();
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new RuneclawBear());
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player1.getId(), bear.getId()));
        harness.passBothPriorities();

        harness.assertLife(player1, 17);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
    }

    private void prepareAbility(boolean fromGraveyard) {
        if (fromGraveyard) {
            harness.setGraveyard(player1, List.of(new SoulOfShandalar()));
            harness.forceActivePlayer(player1);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        } else {
            addReadySoul();
        }
    }

    private void activateDamage(boolean fromGraveyard, List<UUID> targets) {
        if (fromGraveyard) {
            harness.activateGraveyardAbilityWithTargets(player1, 0, 0, targets);
        } else {
            harness.activateAbilityWithMultiTargets(player1, 0, 0, targets);
        }
    }

    private void addReadySoul() {
        harness.addToBattlefield(player1, new SoulOfShandalar());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
