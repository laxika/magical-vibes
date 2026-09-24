package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.z.ZagothTriome;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TyphoidMaryFractured.class, ZagothTriome.class, GrizzlyBears.class, Shock.class})
class TyphoidMaryFracturedTest extends BaseCardTest {

    private static final String MARY_MODE = "Mary — Create a Treasure token";
    private static final String TYPHOID_MARY_MODE = "Typhoid Mary — Draw a card";
    private static final String BLOODY_MARY_MODE =
            "Bloody Mary — Each opponent loses 2 life and you gain 2 life";

    @Test
    @DisplayName("Lets you choose the Treasure mode after discarding this turn")
    void choosesTreasureModeAfterDiscarding() {
        discardThisTurn();
        attackMary();

        harness.handleListChoice(player1, MARY_MODE);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("Lets you choose the draw mode after discarding this turn")
    void choosesDrawModeAfterDiscarding() {
        discardThisTurn();
        attackMary();

        harness.handleListChoice(player1, TYPHOID_MARY_MODE);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .contains("Shock");
    }

    @Test
    @DisplayName("Lets you choose the life-drain mode after discarding this turn")
    void choosesLifeDrainModeAfterDiscarding() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        discardThisTurn();
        attackMary();

        harness.handleListChoice(player1, BLOODY_MARY_MODE);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(12);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Chooses one mode at random without a discard")
    void choosesModeAtRandomWithoutDiscarding() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Shock()));
        attackMary();
        resolveAllTriggers();

        boolean treasure = findPermanents(player1, "Treasure").size() == 1;
        boolean draw = gd.playerHands.get(player1.getId()).stream()
                .anyMatch(card -> card.getName().equals("Grizzly Bears")
                        || card.getName().equals("Shock"));
        boolean drain = gd.playerLifeTotals.get(player1.getId()) == 22
                && gd.playerLifeTotals.get(player2.getId()) == 18;
        assertThat(treasure || draw || drain).isTrue();
    }

    private void discardThisTurn() {
        harness.setHand(player1, List.of(new ZagothTriome()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
    }

    private void attackMary() {
        Permanent mary = addCreatureReady(player1, new TyphoidMaryFractured());
        mary.setAttacking(true);
        resolveCombat();
    }
}
