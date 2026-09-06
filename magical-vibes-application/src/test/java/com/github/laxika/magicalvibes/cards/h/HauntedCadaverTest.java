package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(HauntedCadaver.class)
class HauntedCadaverTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage may sacrifice Haunted Cadaver and make the player discard three cards")
    void combatDamageMaySacrificeAndDiscardThree() {
        harness.setHand(player2, List.of(new HauntedCadaver(), new HauntedCadaver(), new HauntedCadaver()));
        addAttacker();

        resolveCombat();
        chooseDamagedPlayer();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Haunted Cadaver");
        harness.assertInGraveyard(player1, "Haunted Cadaver");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount())
                .isEqualTo(3);

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Declining the may ability keeps Haunted Cadaver on the battlefield")
    void decliningMayKeepsCadaver() {
        harness.setHand(player2, List.of(new HauntedCadaver()));
        addAttacker();

        resolveCombat();
        chooseDamagedPlayer();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Haunted Cadaver");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A blocked Haunted Cadaver does not trigger")
    void blockedCadaverDoesNotTrigger() {
        addAttacker();
        Permanent blocker = addCreatureReady(player2, new HauntedCadaver());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Haunted Cadaver");
    }

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new HauntedCadaver());
        attacker.setAttacking(true);
        return attacker;
    }

    private void chooseDamagedPlayer() {
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
    }
}
