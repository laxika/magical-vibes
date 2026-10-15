package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Censor.class, DuneBeetle.class})
class CensorTest extends BaseCardTest {

    @Test
    @DisplayName("Counters spell when opponent cannot pay {1}")
    void countersWhenOpponentCannotPay() {
        DuneBeetle beetle = new DuneBeetle();
        harness.setHand(player1, List.of(beetle));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.setHand(player2, List.of(new Censor()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, beetle.getId());

        harness.assertInGraveyard(player1, "Dune Beetle");
        harness.assertNotOnBattlefield(player1, "Dune Beetle");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Spell is not countered when opponent pays {1}")
    void spellNotCounteredWhenOpponentPays() {
        DuneBeetle beetle = new DuneBeetle();
        harness.setHand(player1, List.of(beetle));
        harness.addMana(player1, ManaColor.BLACK, 3); // 2 to cast, 1 to pay

        harness.setHand(player2, List.of(new Censor()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, beetle.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dune Beetle");
    }

    @Test
    @DisplayName("Spell is countered when opponent declines to pay")
    void spellCounteredWhenOpponentDeclines() {
        DuneBeetle beetle = new DuneBeetle();
        harness.setHand(player1, List.of(beetle));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.setHand(player2, List.of(new Censor()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, beetle.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Dune Beetle");
        harness.assertNotOnBattlefield(player1, "Dune Beetle");
    }

    @Test
    @DisplayName("Cycling discards the card and draws one")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new DuneBeetle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Censor");
        harness.assertInHand(player1, "Dune Beetle");
    }

    @Test
    @DisplayName("Cycling pays and discards immediately, but draws only on resolution")
    void cyclingDiscardsBeforeDrawing() {
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new DuneBeetle()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Censor");
        harness.assertNotInHand(player1, "Censor");
        harness.assertNotInHand(player1, "Dune Beetle");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();

        harness.passBothPriorities();

        harness.assertInHand(player1, "Dune Beetle");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling cannot be paid with nonblue mana")
    void cyclingRequiresBlueMana() {
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new DuneBeetle()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Censor");
        harness.assertNotInGraveyard(player1, "Censor");
        harness.assertNotInHand(player1, "Dune Beetle");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Censor can counter another instant spell")
    void countersAnInstantSpell() {
        DuneBeetle beetle = new DuneBeetle();
        Censor opposingCensor = new Censor();
        harness.setHand(player1, List.of(beetle, new Censor()));
        harness.setHand(player2, List.of(opposingCensor));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, beetle.getId());
        harness.castAndResolveInstant(player1, 0, opposingCensor.getId());

        harness.assertInGraveyard(player2, "Censor");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Dune Beetle");
        assertThat(gd.stack).isEmpty();
    }
}
