package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BarkhideMauler;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WirewoodSavage.class, BarkhideMauler.class, WirewoodElf.class, Shock.class})
class WirewoodSavageTest extends BaseCardTest {

    @Test
    @DisplayName("May draw when a Beast enters under your control")
    void drawsForControlledBeast() {
        harness.addToBattlefield(player1, new WirewoodSavage());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Shock()));

        castBeast(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Shock");
    }

    @Test
    @DisplayName("May decline the draw")
    void mayDeclineDraw() {
        harness.addToBattlefield(player1, new WirewoodSavage());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Shock()));

        castBeast(player1);

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotInHand(player1, "Shock");
    }

    @Test
    @DisplayName("Triggers for a Beast entering under an opponent's control")
    void drawsForOpponentsBeast() {
        harness.addToBattlefield(player1, new WirewoodSavage());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Shock()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        castBeast(player2);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Shock");
    }

    @Test
    @CardUsed(WoodlandChangeling.class)
    @DisplayName("Treats a Changeling creature as a Beast")
    void drawsForChangelingCreature() {
        harness.addToBattlefield(player1, new WirewoodSavage());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Shock()));

        harness.castFromHand(player1, new WoodlandChangeling(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice mayChoice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(mayChoice).isNotNull();
        assertThat(mayChoice.playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Shock");
    }

    @Test
    @DisplayName("Does not trigger for a non-Beast creature")
    void ignoresNonBeastCreature() {
        harness.addToBattlefield(player1, new WirewoodSavage());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.castFromHand(player1, new WirewoodElf(), "{1}{G}");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertNotInHand(player1, "Shock");
    }

    private void castBeast(com.github.laxika.magicalvibes.model.Player player) {
        harness.castFromHand(player, new BarkhideMauler(), "{4}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
