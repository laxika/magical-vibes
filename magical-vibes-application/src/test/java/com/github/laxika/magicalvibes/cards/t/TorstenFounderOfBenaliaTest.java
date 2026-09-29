package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TorstenFounderOfBenalia.class, Forest.class, GrizzlyBears.class, Plains.class,
        Shock.class, WrathOfGod.class})
class TorstenFounderOfBenaliaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB reveals seven and lets you put any number of creature and land cards into your hand")
    void entersAndPutsSelectedCreaturesAndLandsIntoHand() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card secondCreature = new GrizzlyBears();
        Card secondLand = new Plains();
        Card shock = new Shock();
        Card secondShock = new Shock();
        Card thirdShock = new Shock();
        harness.setLibrary(player1, List.of(creature, land, secondCreature, secondLand,
                shock, secondShock, thirdShock));

        castAndResolveTorsten();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                creature.getId(), land.getId(), secondCreature.getId(), secondLand.getId());
        assertThat(choice.maxCount()).isEqualTo(4);
        assertThat(choice.minCount()).isZero();
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), land.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(creature, land);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(secondCreature, secondLand, shock, secondShock, thirdShock);
    }

    @Test
    @DisplayName("When Torsten dies, seven Soldier tokens are created")
    void deathCreatesSevenSoldiers() {
        harness.addToBattlefield(player1, new TorstenFounderOfBenalia());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> soldiers = findPermanents(player1, "Soldier");
        assertThat(soldiers).hasSize(7);
        assertThat(soldiers).allSatisfy(soldier -> {
            assertThat(soldier.getCard().isToken()).isTrue();
            assertThat(soldier.getEffectivePower()).isEqualTo(1);
            assertThat(soldier.getEffectiveToughness()).isEqualTo(1);
        });
    }

    private void castAndResolveTorsten() {
        harness.setHand(player1, List.of(new TorstenFounderOfBenalia()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
