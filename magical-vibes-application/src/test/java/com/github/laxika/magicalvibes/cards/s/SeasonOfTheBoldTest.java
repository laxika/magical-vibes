package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeasonOfTheBold.class, GrizzlyBears.class, Plains.class, Shock.class})
class SeasonOfTheBoldTest extends BaseCardTest {

    @Test
    @DisplayName("Creates tapped Treasures and allows repeating a mode")
    void createsTappedTreasuresWithRepeatedMode() {
        cast(mode(0, 0));

        List<Permanent> treasures = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(treasures).hasSize(2).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Exiles the top two cards with play permission through the next turn")
    void exilesTopTwoCards() {
        Card first = new Plains();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));

        cast(mode(1));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
        assertThat(gd.exilePlayPermissions).containsEntry(first.getId(), player1.getId())
                .containsEntry(second.getId(), player1.getId());
    }

    @Test
    @DisplayName("The spell-cast mode deals damage to an optionally chosen creature")
    void spellCastModeDamagesChosenCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast(mode(2));

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The spell-cast mode can resolve without a creature target")
    void spellCastModeCanDeclineTarget() {
        cast(mode(2));

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private void cast(int selection) {
        harness.setHand(player1, List.of(new SeasonOfTheBold()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castSorcery(player1, 0, selection);
        harness.passBothPriorities();
    }

    private static int mode(int... modeIndices) {
        return ChooseOneEffect.encodeBudgetedModeSelection(5, List.of(1, 2, 3), modeIndices);
    }
}
