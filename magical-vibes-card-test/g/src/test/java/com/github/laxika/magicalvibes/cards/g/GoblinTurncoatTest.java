package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BoggartShenanigans;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinTurncoat.class, GoblinGoon.class, FugitiveWizard.class, BoggartShenanigans.class})
class GoblinTurncoatTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another Goblin regenerates Goblin Turncoat")
    void sacrificesAnotherGoblinAndRegeneratesItself() {
        Permanent turncoat = harness.addToBattlefieldAndReturn(player1, new GoblinTurncoat());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinGoon());

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, goblin.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(turncoat).doesNotContain(goblin);
        assertThat(turncoat.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Goblin Turncoat may sacrifice itself")
    void canSacrificeItself() {
        Permanent turncoat = harness.addToBattlefieldAndReturn(player1, new GoblinTurncoat());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(turncoat);
        harness.assertInGraveyard(player1, "Goblin Turncoat");
    }

    @Test
    @DisplayName("Sacrifice cost only accepts Goblins")
    void sacrificeCostOnlyAcceptsGoblins() {
        harness.addToBattlefield(player1, new GoblinTurncoat());
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new GoblinGoon());
        Permanent nonGoblin = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonGoblin.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
        harness.handlePermanentChosen(player1, goblin.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(nonGoblin).doesNotContain(goblin);
    }

    @Test
    @DisplayName("Regeneration shield saves Goblin Turncoat from lethal combat damage")
    void regenerationShieldSavesFromLethalCombatDamage() {
        Permanent turncoat = harness.addToBattlefieldAndReturn(player1, new GoblinTurncoat());
        Permanent sacrificedGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinGoon());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificedGoblin.getId());
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player2, new GoblinGoon());
        addCreatureReady(player2, new FugitiveWizard());
        attacker.setAttacking(true);
        turncoat.setBlocking(true);
        turncoat.addBlockingTarget(0);

        harness.forceActivePlayer(player2);
        harness.resolveCombatDamage();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(turncoat);
        assertThat(turncoat.isTapped()).isTrue();
        assertThat(turncoat.getRegenerationShield()).isZero();
        harness.assertNotInGraveyard(player1, "Goblin Turncoat");
    }

    @Test
    @DisplayName("A noncreature Goblin can pay the sacrifice cost")
    void canSacrificeNoncreatureGoblin() {
        Permanent turncoat = harness.addToBattlefieldAndReturn(player1, new GoblinTurncoat());
        Permanent shenanigans = harness.addToBattlefieldAndReturn(player1, new BoggartShenanigans());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, shenanigans.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(turncoat).doesNotContain(shenanigans);
        harness.assertInGraveyard(player1, "Boggart Shenanigans");
        assertThat(turncoat.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's Goblin cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsGoblin() {
        Permanent turncoat = harness.addToBattlefieldAndReturn(player1, new GoblinTurncoat());
        Permanent ownGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinGoon());
        Permanent opposingGoblin = harness.addToBattlefieldAndReturn(player2, new GoblinGoon());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingGoblin.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, ownGoblin.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingGoblin);
        assertThat(turncoat.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped Turncoat can activate repeatedly and accumulate shields")
    void tappedTurncoatCanAccumulateRegenerationShields() {
        Permanent turncoat = harness.addToBattlefieldAndReturn(player1, new GoblinTurncoat());
        Permanent firstGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinGoon());
        Permanent secondGoblin = harness.addToBattlefieldAndReturn(player1, new GoblinGoon());
        turncoat.tap();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, firstGoblin.getId());
        assertThat(turncoat.getRegenerationShield()).isZero();
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, secondGoblin.getId());
        harness.passBothPriorities();

        assertThat(turncoat.getRegenerationShield()).isEqualTo(2);
        assertThat(turncoat.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(turncoat).doesNotContain(firstGoblin, secondGoblin);
    }
}
