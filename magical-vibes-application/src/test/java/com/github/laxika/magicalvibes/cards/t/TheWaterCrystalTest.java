package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.p.PetraSphinx;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheWaterCrystal.class, Divination.class, GrizzlyBears.class, Millstone.class, PetraSphinx.class})
class TheWaterCrystalTest extends BaseCardTest {

    @Test
    void blueSpellsCostOneLessGenericMana() {
        harness.addToBattlefield(player1, new TheWaterCrystal());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void nonBlueSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new TheWaterCrystal());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void opponentsSpellsAreNotReduced() {
        harness.addToBattlefield(player1, new TheWaterCrystal());
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void addsFourCardsToEachOpponentMill() {
        harness.addToBattlefield(player1, new TheWaterCrystal());
        harness.addToBattlefield(player1, new Millstone());
        harness.setLibrary(player2, cards(6));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(6);
    }

    @Test
    void doesNotAddCardsToControllerMill() {
        harness.addToBattlefield(player1, new TheWaterCrystal());
        harness.addToBattlefield(player1, new Millstone());
        harness.setLibrary(player1, cards(2));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void doesNotAddCardsWhenAnEffectPutsCardsIntoAGraveyard() {
        harness.addToBattlefield(player1, new TheWaterCrystal());
        Permanent sphinx = harness.addToBattlefieldAndReturn(player1, new PetraSphinx());
        sphinx.setSummoningSick(false);
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Divination");

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    void activatedAbilityUsesControllerHandSizeBeforeMillBonus() {
        harness.addToBattlefield(player1, new TheWaterCrystal());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, cards(7));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(7);
    }

    private List<Card> cards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(ignored -> (Card) new GrizzlyBears())
                .toList();
    }
}
