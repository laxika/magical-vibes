package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EquilibriumAdept.class, DarkRitual.class, Island.class})
class EquilibriumAdeptTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles the top card with permission to play it until the end of your next turn")
    void etbExilesTopCardWithNextTurnPlayPermission() {
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new EquilibriumAdept()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireAtTurnEnd.get(topCard.getId()))
                .isEqualTo(gd.turnNumber + 2);
    }

    @Test
    @DisplayName("Flurry grants double strike on the second spell each turn")
    void flurryGrantsDoubleStrikeOnSecondSpell() {
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new EquilibriumAdept());
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0);
        assertThat(gqs.hasKeyword(gd, adept, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.castAndResolveInstant(player1, 0);
        assertThat(gqs.hasKeyword(gd, adept, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("An empty library does not prevent the Adept from entering")
    void emptyLibraryDoesNotPreventEntering() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new EquilibriumAdept()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Equilibrium Adept");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The exiled land can be played through the normal land permission")
    void canPlayExiledLand() {
        Card land = new Island();
        harness.setLibrary(player1, List.of(land));
        harness.setHand(player1, List.of(new EquilibriumAdept()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(land);
    }

    @Test
    @DisplayName("Flurry counts the Adept itself as the first spell but does not trigger on its own cast")
    void countsSpellCastBeforeEntering() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new EquilibriumAdept(), new EquilibriumAdept()));
        harness.addMana(player1, ManaColor.RED, 8);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        Permanent first = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.hasKeyword(gd, first, Keyword.DOUBLE_STRIKE)).isFalse();

        harness.castCreature(player1, 0);
        assertThat(gqs.hasKeyword(gd, first, Keyword.DOUBLE_STRIKE)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, first, Keyword.DOUBLE_STRIKE)).isTrue();
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent second = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(gqs.hasKeyword(gd, second, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Flurry triggers during an opponent's turn for the controller's second spell")
    void flurryTriggersDuringOpponentsTurn() {
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new EquilibriumAdept());
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0);
        assertThat(gqs.hasKeyword(gd, adept, Keyword.DOUBLE_STRIKE)).isFalse();
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, adept, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("An opponent's second spell does not trigger Flurry")
    void opponentsSpellsDoNotTriggerFlurry() {
        Permanent adept = harness.addToBattlefieldAndReturn(player1, new EquilibriumAdept());
        harness.setHand(player2, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player2, 0);
        harness.castAndResolveInstant(player2, 0);

        assertThat(gqs.hasKeyword(gd, adept, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
