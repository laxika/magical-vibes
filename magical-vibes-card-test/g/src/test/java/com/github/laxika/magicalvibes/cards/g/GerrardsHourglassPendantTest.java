package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CaptureOfJingzhou;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.o.ObsidianBattleAxe;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.SoulWarden;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GerrardsHourglassPendant.class, CaptureOfJingzhou.class, DoomBlade.class,
        GrizzlyBears.class, Naturalize.class, ObsidianBattleAxe.class, Pacifism.class, SoulWarden.class})
class GerrardsHourglassPendantTest extends BaseCardTest {

    @Test
    void canBeCastDuringOpponentsTurnWithFlash() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new GerrardsHourglassPendant()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gerrard's Hourglass Pendant");
    }

    @Test
    @DisplayName("Skips the controller's extra turn")
    void skipsControllersExtraTurn() {
        harness.addToBattlefield(player1, new GerrardsHourglassPendant());
        harness.setHand(player1, List.of(new CaptureOfJingzhou()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.extraTurns).containsExactly(player1.getId());

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.extraTurns).isEmpty();
    }

    @Test
    @DisplayName("Returns battlefield permanents put into the graveyard this turn tapped")
    void returnsPermanentsThatLeftTheBattlefieldThisTurn() {
        Card bears = new GrizzlyBears();
        Card axe = new ObsidianBattleAxe();
        Permanent pendant = harness.addToBattlefieldAndReturn(player1, new GerrardsHourglassPendant());
        harness.addToBattlefield(player1, bears);
        harness.addToBattlefield(player1, axe);

        harness.setHand(player1, List.of(new DoomBlade(), new Naturalize()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Obsidian Battle-Axe"));

        int pendantIndex = gd.playerBattlefields.get(player1.getId()).indexOf(pendant);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, pendantIndex, null, null);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card.getId().equals(pendant.getCard().getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getId().equals(bears.getId())
                        || permanent.getCard().getId().equals(axe.getId()))
                .hasSize(2)
                .allMatch(Permanent::isTapped);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(bears.getId()) || card.getId().equals(axe.getId()))
                .anyMatch(card -> card.getName().equals("Doom Blade"));
    }

    @Test
    void skipsOpponentsExtraTurnButNotTheirNormalTurn() {
        harness.addToBattlefield(player1, new GerrardsHourglassPendant());
        harness.setHand(player2, List.of(new CaptureOfJingzhou()));
        harness.addMana(player2, ManaColor.BLUE, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveSorcery(player2, 0, 0);

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.extraTurns).isEmpty();
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
    }

    @Test
    void leavesUntrackedCardsAndOpponentsCardsInTheirGraveyards() {
        Card oldBears = new GrizzlyBears();
        Card opposingBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(oldBears));
        harness.addToBattlefield(player1, new GerrardsHourglassPendant());
        harness.addToBattlefield(player2, opposingBears);
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(oldBears);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposingBears);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void returnedCreaturesSeeEachOtherEnterSimultaneously() {
        harness.addToBattlefield(player1, new GerrardsHourglassPendant());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SoulWarden());
        harness.setHand(player1, List.of(new DoomBlade(), new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Soul Warden"));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Soul Warden");
        harness.assertLife(player1, 21);
    }

    @Test
    void returningAuraAllowsChoosingAnExistingCreatureToEnchant() {
        harness.addToBattlefield(player1, new GerrardsHourglassPendant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        var creatureId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new Pacifism(), new Naturalize()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, creatureId);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Pacifism"));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, creatureId);
        harness.assertOnBattlefield(player1, "Pacifism");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getName().equals("Pacifism"))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.getAttachedTo()).isEqualTo(creatureId);
                    assertThat(permanent.isTapped()).isTrue();
                });
    }
}
