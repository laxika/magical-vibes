package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BarkhideMauler;
import com.github.laxika.magicalvibes.cards.c.Conspiracy;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WirewoodSavage.class, BarkhideMauler.class, WirewoodElf.class, Shock.class,
        WoodlandChangeling.class, Conspiracy.class})
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

    @Test
    @DisplayName("Recognizes an opponent's creature made into a Beast by Conspiracy")
    void drawsForOpponentsCreatureWithChangedType() {
        harness.addToBattlefield(player1, new WirewoodSavage());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player2, CardSubtype.BEAST.name());
        harness.castFromHand(player2, new WirewoodElf(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        PendingInteraction.MayAbilityChoice mayChoice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(mayChoice).isNotNull();
        assertThat(mayChoice.playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInHand(player1, "Shock");
        harness.assertNotInHand(player2, "Shock");
    }

    @Test
    @DisplayName("Your Conspiracy does not change an opponent's entering Beast")
    void ownSubtypeChangeDoesNotSuppressOpponentsBeastTrigger() {
        harness.addToBattlefield(player1, new WirewoodSavage());
        harness.setLibrary(player1, List.of(new Shock()));
        harness.castFromHand(player1, new Conspiracy(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.ELF.name());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        castBeast(player2);

        PendingInteraction.MayAbilityChoice mayChoice =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(mayChoice).isNotNull();
        assertThat(mayChoice.playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.assertInHand(player1, "Shock");
    }

    @Test
    @DisplayName("The draw trigger resolves after Wirewood Savage leaves the battlefield")
    void drawTriggerSurvivesSourceRemoval() {
        harness.addToBattlefield(player1, new WirewoodSavage());
        harness.setLibrary(player1, List.of(new WirewoodElf(), new BarkhideMauler()));
        harness.castFromHand(player1, new BarkhideMauler(), "{4}{G}");
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Wirewood Savage"));
        harness.assertInGraveyard(player1, "Wirewood Savage");

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Wirewood Elf");
        harness.assertNotInHand(player1, "Barkhide Mauler");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private void castBeast(com.github.laxika.magicalvibes.model.Player player) {
        harness.castFromHand(player, new BarkhideMauler(), "{4}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
