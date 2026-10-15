package com.github.laxika.magicalvibes.cards.g;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GorexTheTombshell.class, GrizzlyBears.class, Shock.class})
class GorexTheTombshellTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles any number of creature cards and tracks them with Gorex")
    void exilesCreatureCardsAndTracksThem() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(first, second, shock));
        harness.setHand(player1, List.of(new GorexTheTombshell()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(shock);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();

        harness.passBothPriorities();

        Permanent gorex = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.getCardsExiledByPermanent(gorex.getId()))
                .containsExactlyInAnyOrder(first, second);
    }

    @Test
    @DisplayName("Rejects a noncreature card selected for Gorex's additional cost")
    void rejectsNoncreatureAdditionalCostCard() {
        GrizzlyBears creature = new GrizzlyBears();
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(creature, shock));
        harness.setHand(player1, List.of(new GorexTheTombshell()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("additional cost");

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature, shock);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(6);
    }

    @Test
    @DisplayName("Returns a random card exiled with Gorex when it attacks")
    void returnsExiledCardWhenItAttacks() {
        Card exiledCard = new GrizzlyBears();
        Permanent gorex = castGorex(exiledCard);
        gorex.setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(exiledCard);
    }

    @Test
    @DisplayName("Returns a random card exiled with Gorex when it dies")
    void returnsExiledCardWhenItDies() {
        Card exiledCard = new GrizzlyBears();
        Permanent gorex = castGorex(exiledCard);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, gorex));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(exiledCard);
    }

    @Test
    @DisplayName("May cast Gorex without exiling any cards")
    void mayDeclineAdditionalCost() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new GorexTheTombshell()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent gorex = findPermanent(player1, "Gorex, the Tombshell");
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        gorex.setSummoningSick(false);
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("May exile more cards than needed to reduce the generic cost to zero")
    void mayExileMoreThanThreeCreatures() {
        List<Card> creatures = List.of(new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, creatures);
        harness.setHand(player1, List.of(new GorexTheTombshell()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2, 3));
        harness.passBothPriorities();

        Permanent gorex = findPermanent(player1, "Gorex, the Tombshell");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(gorex.getId()))
                .containsExactlyInAnyOrderElementsOf(creatures);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Exiling cards cannot pay Gorex's black mana requirement")
    void reductionDoesNotRemoveColoredManaRequirement() {
        List<Card> creatures = List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
        harness.setGraveyard(player1, creatures);
        harness.setHand(player1, List.of(new GorexTheTombshell()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithMultipleGraveyardExile(
                player1, 0, List.of(0, 1, 2)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyElementsOf(creatures);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
    }

    @Test
    @DisplayName("Attack and death each return one card even when Gorex dies before its attack trigger resolves")
    void attackAndDeathTriggersReturnDistinctCards() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        Card unrelated = new GrizzlyBears();
        harness.setExile(player1, List.of(unrelated));
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new GorexTheTombshell()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1));
        harness.passBothPriorities();
        Permanent gorex = findPermanent(player1, "Gorex, the Tombshell");
        gorex.setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, gorex));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1)
                .allMatch(card -> card == first || card == second);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.getCardsExiledByPermanent(gorex.getId())).isEmpty();
        assertThat(gd.findExiledCard(unrelated.getId())).isNotNull();
    }

    private Permanent castGorex(Card exiledCard) {
        harness.setGraveyard(player1, List.of(exiledCard));
        harness.setHand(player1, List.of(new GorexTheTombshell()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0));
        harness.passBothPriorities();
        return findPermanent(player1, "Gorex, the Tombshell");
    }
}
