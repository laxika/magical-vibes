package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.ErebosGodOfTheDead;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TymaretChosenFromDeath.class, ErebosGodOfTheDead.class, GrizzlyBears.class, Shock.class})
class TymaretChosenFromDeathTest extends BaseCardTest {

    @Test
    @DisplayName("Tymaret's toughness equals black devotion")
    void toughnessEqualsBlackDevotion() {
        Permanent tymaret = harness.addToBattlefieldAndReturn(player1, new TymaretChosenFromDeath());

        assertThat(gqs.getEffectivePower(gd, tymaret)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tymaret)).isEqualTo(2);

        harness.addToBattlefield(player1, new ErebosGodOfTheDead());

        assertThat(gqs.getEffectiveToughness(gd, tymaret)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exiles up to two cards from different graveyards and gains life for creatures")
    void exilesCardsFromAnyGraveyardsAndGainsLifeForCreatures() {
        harness.addToBattlefieldAndReturn(player1, new TymaretChosenFromDeath());
        Card creature = new GrizzlyBears();
        Card noncreature = new Shock();
        harness.setGraveyard(player1, List.of(creature));
        harness.setGraveyard(player2, List.of(noncreature));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(creature.getId(), noncreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(noncreature);
        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }

    @Test
    @DisplayName("Can choose no graveyard cards")
    void canChooseNoCards() {
        harness.addToBattlefieldAndReturn(player1, new TymaretChosenFromDeath());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }
}
