package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MODOKEvilIntellect.class, GrizzlyBears.class})
class MODOKEvilIntellectTest extends BaseCardTest {

    @Test
    @DisplayName("The second card drawn each turn makes a target opponent sacrifice a nontoken creature")
    void secondDrawSacrificesChosenNontokenCreature() {
        harness.addToBattlefield(player1, new MODOKEvilIntellect());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, tokenCreature("Zombie Token"));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        draw(player1);
        assertThat(gd.stack).isEmpty();

        draw(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(first.getId()));
        assertThat(findPermanent(player2, "Zombie Token")).isNotNull();
    }

    @Test
    @DisplayName("The draw trigger does not trigger for the first or third card drawn")
    void triggersOnlyOnSecondDraw() {
        harness.addToBattlefield(player1, new MODOKEvilIntellect());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        draw(player1);
        assertThat(gd.stack).isEmpty();

        draw(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Grizzly Bears");

        draw(player1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The trigger can target only an opponent")
    void cannotTargetController() {
        harness.addToBattlefield(player1, new MODOKEvilIntellect());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        draw(player1);
        draw(player1);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("An opponent with only tokens sacrifices nothing")
    void tokenOnlyBattlefieldIsUnaffected() {
        harness.addToBattlefield(player1, new MODOKEvilIntellect());
        harness.addToBattlefield(player2, tokenCreature("Zombie Token"));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        draw(player1);
        draw(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Zombie Token");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Cards drawn before M.O.D.O.K. enters still count toward the second draw")
    void countsDrawBeforeEntering() {
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        draw(player1);
        harness.addToBattlefield(player1, new MODOKEvilIntellect());
        harness.addToBattlefield(player2, new GrizzlyBears());

        draw(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An opponent's second draw does not trigger M.O.D.O.K.")
    void opponentDrawDoesNotTrigger() {
        harness.addToBattlefield(player1, new MODOKEvilIntellect());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        draw(player2);
        draw(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
    private void draw(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }

    private Card tokenCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setColor(CardColor.BLACK);
        card.setPower(2);
        card.setToughness(2);
        card.setToken(true);
        return card;
    }
}
