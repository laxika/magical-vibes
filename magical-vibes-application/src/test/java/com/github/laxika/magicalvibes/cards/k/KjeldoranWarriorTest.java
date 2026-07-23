package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class KjeldoranWarriorTest extends BaseCardTest {

    private static Card creature(String name, int power, int toughness) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    @Test
    @DisplayName("Banding attacker: active player divides the blocker's combat damage")
    void bandingAttackerLetsActivePlayerDivideBlockerDamage() {
        Permanent warrior = new Permanent(new KjeldoranWarrior());
        Permanent bears = new Permanent(new GrizzlyBears());
        Permanent wall = new Permanent(creature("Great Wall", 3, 6));

        gd.playerBattlefields.get(player1.getId()).add(warrior);
        gd.playerBattlefields.get(player1.getId()).add(bears);
        gd.playerBattlefields.get(player2.getId()).add(wall);

        UUID band = UUID.randomUUID();
        warrior.setSummoningSick(false);
        warrior.setAttacking(true);
        warrior.setBandId(band);
        bears.setSummoningSick(false);
        bears.setAttacking(true);
        bears.setBandId(band);

        wall.setBlocking(true);
        wall.addBlockingTarget(0);
        wall.addBlockingTarget(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player1.getId());
        assertThat(prompt.totalDamage()).isEqualTo(3);

        // Funnel all 3 damage onto the bears; warrior survives.
        harness.handleCombatDamageAssigned(player1, 0, Map.of(bears.getId(), 3));

        harness.assertOnBattlefield(player1, "Kjeldoran Warrior");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Great Wall");
    }
}
