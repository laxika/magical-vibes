package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ClingToDust;
import com.github.laxika.magicalvibes.cards.m.MireTriton;
import com.github.laxika.magicalvibes.cards.o.OmenOfTheDead;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TymaretChosenFromDeath.class, OmenOfTheDead.class, MireTriton.class, ClingToDust.class})
class TymaretChosenFromDeathTest extends BaseCardTest {

    @Test
    @DisplayName("Tymaret's toughness equals black devotion")
    void toughnessEqualsBlackDevotion() {
        Permanent tymaret = harness.addToBattlefieldAndReturn(player1, new TymaretChosenFromDeath());

        assertThat(gqs.getEffectivePower(gd, tymaret)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tymaret)).isEqualTo(2);

        Permanent omen = harness.addToBattlefieldAndReturn(player1, new OmenOfTheDead());

        assertThat(gqs.getEffectiveToughness(gd, tymaret)).isEqualTo(3);

        harness.addToBattlefield(player2, new OmenOfTheDead());
        harness.setGraveyard(player1, List.of(new MireTriton()));
        assertThat(gqs.getEffectiveToughness(gd, tymaret)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(omen);
        assertThat(gqs.getEffectiveToughness(gd, tymaret)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exiles up to two cards from different graveyards and gains life for creatures")
    void exilesCardsFromAnyGraveyardsAndGainsLifeForCreatures() {
        harness.addToBattlefieldAndReturn(player1, new TymaretChosenFromDeath());
        Card creature = new MireTriton();
        Card noncreature = new ClingToDust();
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

    @Test
    void gainsTwoLifeForTwoCreatureCardsFromOneGraveyard() {
        harness.addToBattlefield(player1, new TymaretChosenFromDeath());
        Card first = new MireTriton();
        Card second = new MireTriton();
        harness.setGraveyard(player2, List.of(first, second));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second);
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    void exilesSingleNoncreatureWithoutGainingLife() {
        harness.addToBattlefield(player1, new TymaretChosenFromDeath());
        Card card = new ClingToDust();
        harness.setGraveyard(player2, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(card.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(card);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void resolvesRemainingTargetWhenAnotherActivationExilesOneTarget() {
        harness.addToBattlefield(player1, new TymaretChosenFromDeath());
        Card first = new MireTriton();
        Card second = new MireTriton();
        harness.setGraveyard(player2, List.of(first, second));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(first.getId(), second.getId()));
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(first.getId()));
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second);
        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    void rejectsDuplicateTargetsAndMoreThanTwoTargets() {
        harness.addToBattlefield(player1, new TymaretChosenFromDeath());
        Card first = new MireTriton();
        Card second = new MireTriton();
        Card third = new ClingToDust();
        harness.setGraveyard(player2, List.of(first, second, third));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(first.getId(), first.getId()))).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(first.getId(), second.getId(), third.getId()))).isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(first, second, third);
    }

    @Test
    void gainsNoAdditionalLifeWhenAllTargetsHaveLeftTheGraveyard() {
        harness.addToBattlefield(player1, new TymaretChosenFromDeath());
        Card creature = new MireTriton();
        harness.setGraveyard(player2, List.of(creature));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId()));
        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void toughnessAbilityFunctionsInGraveyardUsingOwnersDevotion() {
        Card tymaret = new TymaretChosenFromDeath();
        harness.setGraveyard(player1, List.of(tymaret));
        harness.addToBattlefield(player2, new OmenOfTheDead());

        assertThat(gqs.getEffectiveCardToughness(gd, tymaret)).isZero();

        harness.addToBattlefield(player1, new OmenOfTheDead());

        assertThat(gqs.getEffectiveCardToughness(gd, tymaret)).isEqualTo(1);
    }
}
