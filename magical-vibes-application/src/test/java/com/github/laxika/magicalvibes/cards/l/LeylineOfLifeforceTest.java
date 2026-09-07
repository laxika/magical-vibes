package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeylineOfLifeforce.class, GrizzlyBears.class, Cancel.class, Shock.class})
class LeylineOfLifeforceTest extends BaseCardTest {

    @Test
    @DisplayName("Leyline in the opening hand may begin the game on the battlefield")
    void leylineInOpeningHandMayStartOnBattlefield() {
        GameTestHarness openingHarness = new GameTestHarness();
        LeylineOfLifeforce leyline = new LeylineOfLifeforce();
        openingHarness.setHand(openingHarness.getPlayer1(), List.of(leyline));
        openingHarness.skipMulligan();

        assertThat(openingHarness.getGameData().interaction.isAwaitingInput()).isTrue();

        openingHarness.handleMayAbilityChosen(openingHarness.getPlayer1(), true);

        assertThat(openingHarness.getGameData().playerBattlefields
                .get(openingHarness.getPlayer1().getId()))
                .anyMatch(permanent -> permanent.getCard() == leyline);
    }

    @Test
    @DisplayName("Declining the opening-hand ability keeps Leyline in hand")
    void decliningOpeningHandAbilityKeepsLeylineInHand() {
        GameTestHarness openingHarness = new GameTestHarness();
        LeylineOfLifeforce leyline = new LeylineOfLifeforce();
        openingHarness.setHand(openingHarness.getPlayer1(), List.of(leyline));
        openingHarness.skipMulligan();

        openingHarness.handleMayAbilityChosen(openingHarness.getPlayer1(), false);

        assertThat(openingHarness.getGameData().playerBattlefields
                .get(openingHarness.getPlayer1().getId()))
                .noneMatch(permanent -> permanent.getCard() == leyline);
        assertThat(openingHarness.getGameData().playerHands
                .get(openingHarness.getPlayer1().getId()))
                .containsExactly(leyline);
    }

    @Test
    @DisplayName("Leyline prevents creature spells from being countered")
    void creatureSpellsCannotBeCountered() {
        harness.addToBattlefield(player1, new LeylineOfLifeforce());

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        Cancel cancel = new Cancel();
        harness.setHand(player2, List.of(cancel));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == bears);
    }

    @Test
    @DisplayName("Leyline does not prevent noncreature spells from being countered")
    void noncreatureSpellsCanBeCountered() {
        harness.addToBattlefield(player1, new LeylineOfLifeforce());

        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);

        Cancel cancel = new Cancel();
        harness.setHand(player2, List.of(cancel));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, shock.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(shock);
    }
}
