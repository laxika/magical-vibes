package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BlessedBreath;
import com.github.laxika.magicalvibes.cards.d.DevotedRetainer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HikariTwilightGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an Arcane spell may exile Hikari until the next end step")
    void arcaneSpellExilesAndReturnsHikari() {
        harness.addToBattlefield(player1, new HikariTwilightGuardian());
        harness.setHand(player1, List.of(new BlessedBreath()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        String hikariName = "Hikari, Twilight Guardian";
        harness.castInstant(player1, 0, harness.getPermanentId(player1, hikariName));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getName().equals("Hikari, Twilight Guardian"));

        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
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
}
