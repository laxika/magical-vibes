package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CursecatcherTest extends BaseCardTest {

    // ===== Counters an instant when its controller cannot pay {1} =====

    @Test
    @DisplayName("Counters target instant when controller has no mana to pay")
    void countersInstantWhenControllerCannotPay() {
        Cursecatcher cursecatcher = new Cursecatcher();
        harness.addToBattlefield(player1, cursecatcher);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1); // just enough to cast, nothing left to pay

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, shock.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Shock is countered — player1 takes no damage
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();

        // Cursecatcher is sacrificed
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Cursecatcher"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Cursecatcher"));
    }

    // ===== Spell survives when controller pays {1} =====

    @Test
    @DisplayName("Instant is not countered when controller pays {1}")
    void notCounteredWhenControllerPays() {
        Cursecatcher cursecatcher = new Cursecatcher();
        harness.addToBattlefield(player1, cursecatcher);

        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, bears);

        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 2); // 1 to cast, 1 to pay

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, shock.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        // Player2 pays {1}
        harness.handleMayAbilityChosen(player2, true);

        // Shock resolves and kills the Grizzly Bears
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Counters a sorcery too =====

    @Test
    @DisplayName("Counters target sorcery when controller cannot pay {1}")
    void countersSorcery() {
        Cursecatcher cursecatcher = new Cursecatcher();
        harness.addToBattlefield(player1, cursecatcher);

        GrizzlyBears victim = new GrizzlyBears();
        harness.addToBattlefield(player1, victim);

        CruelEdict edict = new CruelEdict();
        harness.setHand(player2, List.of(edict));
        harness.addMana(player2, ManaColor.BLACK, 2); // exactly enough to cast, nothing to pay

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, edict.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Cruel Edict is countered — Grizzly Bears survives
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Cruel Edict"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.stack).isEmpty();
    }

    // ===== Cannot target a non-instant/sorcery spell =====

    @Test
    @DisplayName("Cannot target a creature spell")
    void cannotTargetCreatureSpell() {
        Cursecatcher cursecatcher = new Cursecatcher();
        harness.addToBattlefield(player1, cursecatcher);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player2, List.of(bears));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
