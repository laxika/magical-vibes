package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BloodKnight;
import com.github.laxika.magicalvibes.cards.g.GiantDustwasp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HammerheimDeadeye.class, GiantDustwasp.class, BloodKnight.class})
class HammerheimDeadeyeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield destroys a target creature with flying")
    void etbDestroysTargetCreatureWithFlying() {
        harness.addToBattlefield(player2, new GiantDustwasp());
        castAndResolveDeadeye(harness.getPermanentId(player2, "Giant Dustwasp"));

        harness.assertNotOnBattlefield(player2, "Giant Dustwasp");
        harness.assertInGraveyard(player2, "Giant Dustwasp");
        harness.assertOnBattlefield(player1, "Hammerheim Deadeye");
    }

    @Test
    @DisplayName("ETB cannot target a creature without flying")
    void etbCannotTargetCreatureWithoutFlying() {
        harness.addToBattlefield(player2, new BloodKnight());
        prepareDeadeye();
        UUID targetId = harness.getPermanentId(player2, "Blood Knight");

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature with flying");
    }

    @Test
    @DisplayName("Declining echo sacrifices Hammerheim Deadeye at its next upkeep")
    void decliningEchoSacrificesDeadeye() {
        harness.addToBattlefield(player2, new GiantDustwasp());
        castAndResolveDeadeye(harness.getPermanentId(player2, "Giant Dustwasp"));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Hammerheim Deadeye");
        harness.assertInGraveyard(player1, "Hammerheim Deadeye");
    }

    @Test
    @DisplayName("Paying echo keeps Hammerheim Deadeye and echo does not trigger again")
    void payingEchoKeepsDeadeyeAndIsOneShot() {
        harness.addToBattlefield(player2, new GiantDustwasp());
        castAndResolveDeadeye(harness.getPermanentId(player2, "Giant Dustwasp"));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Hammerheim Deadeye");

        advanceToUpkeep(player1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Hammerheim Deadeye");
    }

    @Test
    @DisplayName("ETB can target a flying creature its controller owns")
    void etbCanTargetOwnFlyingCreature() {
        harness.addToBattlefield(player1, new GiantDustwasp());
        castAndResolveDeadeye(harness.getPermanentId(player1, "Giant Dustwasp"));

        harness.assertNotOnBattlefield(player1, "Giant Dustwasp");
        harness.assertInGraveyard(player1, "Giant Dustwasp");
        harness.assertOnBattlefield(player1, "Hammerheim Deadeye");
    }

    @Test
    @DisplayName("Echo does not trigger during an opponent's upkeep")
    void echoDoesNotTriggerDuringOpponentUpkeep() {
        harness.addToBattlefield(player2, new GiantDustwasp());
        castAndResolveDeadeye(harness.getPermanentId(player2, "Giant Dustwasp"));

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Hammerheim Deadeye");
    }

    private void prepareDeadeye() {
        harness.setHand(player1, List.of(new HammerheimDeadeye()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);
    }

    private void castAndResolveDeadeye(UUID targetId) {
        prepareDeadeye();
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
