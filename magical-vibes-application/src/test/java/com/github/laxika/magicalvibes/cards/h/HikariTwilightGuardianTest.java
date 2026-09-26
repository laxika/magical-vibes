package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BlessedBreath;
import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HikariTwilightGuardian.class, BlessedBreath.class, DevotedRetainer.class, HarshDeceiver.class})
class HikariTwilightGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an Arcane spell may exile Hikari until the next end step")
    void arcaneSpellExilesAndReturnsHikari() {
        harness.addToBattlefield(player1, new HikariTwilightGuardian());
        harness.setHand(player1, List.of(new BlessedBreath()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Hikari, Twilight Guardian"));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Hikari, Twilight Guardian");

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hikari, Twilight Guardian");
    }

    @Test
    @DisplayName("Casting a Spirit spell may exile Hikari")
    void spiritSpellTriggersHikari() {
        harness.addToBattlefield(player1, new HikariTwilightGuardian());
        harness.setHand(player1, List.of(new HarshDeceiver()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Casting a non-Spirit non-Arcane spell does not trigger Hikari")
    void unrelatedSpellDoesNotTriggerHikari() {
        harness.addToBattlefield(player1, new HikariTwilightGuardian());
        harness.setHand(player1, List.of(new DevotedRetainer()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNull();
        harness.assertOnBattlefield(player1, "Hikari, Twilight Guardian");
    }

    @Test
    @DisplayName("Declining the trigger leaves Hikari on the battlefield")
    void decliningTriggerDoesNotExileHikari() {
        harness.addToBattlefield(player1, new HikariTwilightGuardian());
        harness.setHand(player1, List.of(new HarshDeceiver()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, false);
        harness.assertOnBattlefield(player1, "Hikari, Twilight Guardian");
    }

    @Test
    @DisplayName("A Spirit spell cast by an opponent does not trigger Hikari")
    void opponentSpiritSpellDoesNotTriggerHikari() {
        harness.addToBattlefield(player1, new HikariTwilightGuardian());
        harness.setHand(player2, List.of(new HarshDeceiver()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreature(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNull();
        harness.assertOnBattlefield(player1, "Hikari, Twilight Guardian");
    }

    @Test
    @DisplayName("Hikari returns under its owner's control when it is controlled by another player")
    void returnsUnderOwnersControlWhenStolen() {
        HikariTwilightGuardian hikari = new HikariTwilightGuardian();
        hikari.setOwnerId(player1.getId());
        harness.addToBattlefield(player2, hikari);
        harness.setHand(player2, List.of(new HarshDeceiver()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreature(player2, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Hikari, Twilight Guardian");

        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hikari, Twilight Guardian");
        harness.assertNotOnBattlefield(player2, "Hikari, Twilight Guardian");
    }
}
