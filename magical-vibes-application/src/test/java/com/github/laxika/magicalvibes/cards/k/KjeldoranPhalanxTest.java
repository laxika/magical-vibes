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

class KjeldoranPhalanxTest extends BaseCardTest {

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
    @DisplayName("First strike kills a 5/2 blocker before it can deal damage")
    void firstStrikeKillsBlockerBeforeReciprocalDamage() {
        Permanent phalanx = new Permanent(new KjeldoranPhalanx());
        phalanx.setSummoningSick(false);
        phalanx.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(phalanx);

        Permanent blocker = new Permanent(creature("Ogre Bruiser", 5, 2));
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kjeldoran Phalanx");
        harness.assertNotOnBattlefield(player2, "Ogre Bruiser");
        harness.assertInGraveyard(player2, "Ogre Bruiser");
    }

    @Test
    @DisplayName("Banding attacker: active player divides the blocker's combat damage")
    void bandingAttackerLetsActivePlayerDivideBlockerDamage() {
        Permanent phalanx = new Permanent(new KjeldoranPhalanx());
        Permanent bears = new Permanent(new GrizzlyBears());
        Permanent wall = new Permanent(creature("Great Wall", 3, 6));

        gd.playerBattlefields.get(player1.getId()).add(phalanx);
        gd.playerBattlefields.get(player1.getId()).add(bears);
        gd.playerBattlefields.get(player2.getId()).add(wall);

        UUID band = UUID.randomUUID();
        phalanx.setSummoningSick(false);
        phalanx.setAttacking(true);
        phalanx.setBandId(band);
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

        // Funnel all 3 damage onto the bears; first strike from Phalanx already resolved.
        harness.handleCombatDamageAssigned(player1, 0, Map.of(bears.getId(), 3));

        harness.assertOnBattlefield(player1, "Kjeldoran Phalanx");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Great Wall");
    }
}
