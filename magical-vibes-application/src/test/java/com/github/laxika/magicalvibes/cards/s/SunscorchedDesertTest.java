package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SunscorchedDesertTest extends BaseCardTest {

    // ===== ETB: deal 1 damage to target player or planeswalker =====

    @Test
    @DisplayName("Entering deals 1 damage to the chosen opponent")
    void etbDamagesChosenOpponent() {
        harness.setHand(player1, List.of(new SunscorchedDesert()));
        harness.setLife(player2, 20);

        gs.playCard(gd, player1, 0, 0, null, null);

        // The land is played (not cast), so its mandatory ETB target is chosen as the ability
        // goes on the stack.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Any player is a legal target — the controller may be chosen")
    void canTargetController() {
        harness.setHand(player1, List.of(new SunscorchedDesert()));
        harness.setLife(player1, 20);

        gs.playCard(gd, player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(player1.getId(), player2.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("A planeswalker is a legal target, a creature is not; damage removes a loyalty counter")
    void etbDamagesPlaneswalkerNotCreature() {
        Permanent planeswalker = addPlaneswalker(player2, 4);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SunscorchedDesert()));

        gs.playCard(gd, player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(planeswalker.getId())
                .doesNotContain(creature.getId());
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
    }

    // ===== Mana ability =====

    @Test
    @DisplayName("{T}: Add {C} produces one colorless mana")
    void tapForColorlessMana() {
        harness.addToBattlefield(player1, new SunscorchedDesert());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setLoyalty(loyalty);
        Permanent permanent = new Permanent(card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
