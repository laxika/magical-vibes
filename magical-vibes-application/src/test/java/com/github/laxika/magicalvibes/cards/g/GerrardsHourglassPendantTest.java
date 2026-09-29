package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CaptureOfJingzhou;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.o.ObsidianBattleAxe;
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
        GrizzlyBears.class, Naturalize.class, ObsidianBattleAxe.class})
class GerrardsHourglassPendantTest extends BaseCardTest {

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
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Obsidian Battle-Axe"));
        harness.passBothPriorities();

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
}
