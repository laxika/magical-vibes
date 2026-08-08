package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MistbladeShinobiTest extends BaseCardTest {

    private Permanent addReadyCreature(Player player, Card card) {
        GameData gd = harness.getGameData();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    @Test
    @DisplayName("Combat damage to a player lets the controller bounce a creature that player controls")
    void bouncesCreature() {
        Permanent shinobi = addReadyCreature(player1, new MistbladeShinobi());
        shinobi.setAttacking(true);
        Permanent bears = addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The bounce is optional — returning nothing is allowed")
    void bounceIsOptional() {
        Permanent shinobi = addReadyCreature(player1, new MistbladeShinobi());
        shinobi.setAttacking(true);
        addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Only one creature may be returned even when the Shinobi is pumped")
    void returnsOnlyOneCreatureRegardlessOfDamage() {
        Permanent shinobi = addReadyCreature(player1, new MistbladeShinobi());
        shinobi.setAttacking(true);
        shinobi.setPowerModifier(3);
        addReadyCreature(player2, new GrizzlyBears());
        addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).maxCount())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("No choice when the damaged player controls no creatures")
    void noChoiceWithoutCreatures() {
        Permanent shinobi = addReadyCreature(player1, new MistbladeShinobi());
        shinobi.setAttacking(true);

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Blocked Shinobi deals no damage to the player and does not trigger")
    void noTriggerWhenBlocked() {
        Permanent shinobi = addReadyCreature(player1, new MistbladeShinobi());
        shinobi.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
    }
}
