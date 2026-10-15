package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShambleBack.class, GrizzlyBears.class, Cancel.class})
class ShambleBackTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature card from a graveyard, creates a Zombie, and gains 2 life")
    void exilesCreatureCreatesZombieAndGainsLife() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new ShambleBack()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
        harness.assertOnBattlefield(player1, "Zombie");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Cannot target a noncreature card")
    void rejectsNonCreatureTarget() {
        Card cancel = new Cancel();
        harness.setGraveyard(player2, List.of(cancel));
        harness.setHand(player1, List.of(new ShambleBack()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(cancel.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzling because the target leaves the graveyard gains no life or token")
    void fizzlesIfTargetLeavesGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new ShambleBack()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, List.of(creature.getId()));
        harness.setGraveyard(player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    @Test
    @DisplayName("Can exile a creature from your own graveyard")
    void exilesOwnCreature() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new ShambleBack()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
        assertThat(findPermanents(player1, "Zombie")).singleElement().satisfies(zombie -> {
            assertThat(zombie.getCard().isToken()).isTrue();
            assertThat(zombie.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(zombie.getCard().getPower()).isEqualTo(2);
            assertThat(zombie.getCard().getToughness()).isEqualTo(2);
            assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(zombie.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
            assertThat(zombie.isTapped()).isFalse();
        });
        harness.assertNotOnBattlefield(player2, "Zombie");
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Shamble Back");
    }

    @Test
    @DisplayName("Cannot cast without a creature card target")
    void cannotCastWithoutTarget() {
        harness.setHand(player1, List.of(new ShambleBack()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Shamble Back");
        harness.assertLife(player1, 20);
        harness.assertNotOnBattlefield(player1, "Zombie");
    }
}
