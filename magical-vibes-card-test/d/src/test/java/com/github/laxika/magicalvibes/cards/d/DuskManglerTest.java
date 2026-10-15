package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.ChromeCat;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DuskMangler.class, GrizzlyBears.class, LlanowarElves.class, ChromeCat.class})
class DuskManglerTest extends BaseCardTest {

    @Test
    void sacrificesCreatureAsAdditionalCost() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DuskMangler()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        addMana();

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        resolveAllTriggers();
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    void discardsCardAsAdditionalCost() {
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new DuskMangler(), new GrizzlyBears()));
        addMana();

        harness.castInstantWithDiscard(player1, 0, null, 1);
        resolveAllTriggers();
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    void paysLifeAsAdditionalCost() {
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new DuskMangler()));
        addMana();

        harness.castCreature(player1, 0);
        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    void cannotCastWithoutAnyAdditionalCostPayment() {
        harness.setLife(player1, 3);
        harness.setHand(player1, List.of(new DuskMangler()));
        addMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getLife(player1.getId())).isEqualTo(3);
        harness.assertInHand(player1, "Dusk Mangler");
    }

    @Test
    void canSacrificeInsteadOfPayingLifeWhenLifeIsTooLow() {
        harness.setLife(player1, 3);
        harness.setHand(player2, List.of());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new ChromeCat());
        harness.setHand(player1, List.of(new DuskMangler()));
        addMana();

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());

        harness.assertInGraveyard(player1, "Chrome Cat");
        harness.assertNotOnBattlefield(player1, "Chrome Cat");
        harness.assertLife(player1, 3);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Dusk Mangler");
        harness.assertLife(player2, 16);
    }

    @Test
    void canDiscardInsteadOfPayingLifeWhenLifeIsTooLow() {
        harness.setLife(player1, 3);
        harness.setHand(player2, List.of());
        harness.setHand(player1, List.of(new DuskMangler(), new ChromeCat()));
        addMana();

        harness.castSorceryWithDiscard(player1, 0, 1);

        harness.assertInGraveyard(player1, "Chrome Cat");
        harness.assertLife(player1, 3);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Dusk Mangler");
        harness.assertLife(player2, 16);
    }

    @Test
    void enteringWithoutCastingDoesNotRequireAdditionalCost() {
        harness.setLife(player1, 3);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new ChromeCat()));

        harness.enterBattlefieldAndReturn(player1, new DuskMangler());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player1, "Dusk Mangler");
        harness.assertLife(player1, 3);
        harness.assertInGraveyard(player2, "Chrome Cat");
        harness.assertLife(player2, 16);
    }

    @Test
    void opponentChoosesCreatureThenCardBeforeLosingLife() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new ChromeCat());
        harness.addToBattlefield(player2, new DuskMangler());
        harness.addToBattlefield(player1, new ChromeCat());
        harness.setHand(player1, List.of(new ChromeCat()));
        harness.setHand(player2, List.of(new ChromeCat(), new DuskMangler()));

        harness.enterBattlefieldAndReturn(player1, new DuskMangler());
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
        harness.handleMultiplePermanentsChosen(player2, List.of(chosen.getId()));
        harness.assertNotOnBattlefield(player2, "Chrome Cat");
        harness.assertOnBattlefield(player2, "Dusk Mangler");
        harness.assertLife(player2, 20);
        harness.handleCardChosen(player2, 1);

        harness.assertInGraveyard(player2, "Chrome Cat");
        harness.assertInGraveyard(player2, "Dusk Mangler");
        harness.assertInHand(player2, "Chrome Cat");
        harness.assertNotInHand(player2, "Dusk Mangler");
        harness.assertLife(player2, 16);
        harness.assertLife(player1, 20);
        harness.assertInHand(player1, "Chrome Cat");
        harness.assertOnBattlefield(player1, "Chrome Cat");
    }

    @Test
    void opponentWithEmptyHandStillSacrificesAndLosesLife() {
        harness.addToBattlefield(player2, new ChromeCat());
        harness.setHand(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new DuskMangler());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Chrome Cat");
        harness.assertInGraveyard(player2, "Chrome Cat");
        harness.assertLife(player2, 16);
        harness.assertLife(player1, 20);
    }

    @Test
    void cannotDiscardTheSpellItselfToPayItsAdditionalCost() {
        harness.setHand(player1, List.of(new DuskMangler()));
        addMana();

        assertThatThrownBy(() -> harness.castSorceryWithDiscard(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Dusk Mangler");
        harness.assertNotInGraveyard(player1, "Dusk Mangler");
        harness.assertLife(player1, 20);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
