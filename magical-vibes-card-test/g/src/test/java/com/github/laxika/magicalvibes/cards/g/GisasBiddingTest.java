package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RavensCrime;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GisasBidding.class, RavensCrime.class})
class GisasBiddingTest extends BaseCardTest {

    /** Force player1 to discard Gisa's Bidding via Raven's Crime from player2. */
    private GisasBidding discardViaRavensCrime() {
        GisasBidding bidding = new GisasBidding();
        harness.setHand(player1, List.of(bidding));
        harness.setHand(player2, List.of(new RavensCrime()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castAndResolveSorcery(player2, 0, player1.getId());
        harness.handleCardChosen(player1, 0);
        return bidding;
    }

    private List<Permanent> zombiesOnBattlefield() {
        return findPermanents(player1, "Zombie");
    }

    @Test
    @DisplayName("Casting creates two 2/2 black Zombie tokens")
    void createsTwoZombies() {
        harness.setHand(player1, List.of(new GisasBidding()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        List<Permanent> zombies = zombiesOnBattlefield();
        assertThat(zombies).hasSize(2);
        for (Permanent zombie : zombies) {
            assertThat(zombie.getCard().getPower()).isEqualTo(2);
            assertThat(zombie.getCard().getToughness()).isEqualTo(2);
            assertThat(zombie.getCard().isToken()).isTrue();
        }
    }

    @Test
    @DisplayName("Discarding Gisa's Bidding exiles it and offers madness cast")
    void discardTriggersMadness() {
        GisasBidding bidding = discardViaRavensCrime();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(bidding.getId()));
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.stack.getLast().getDescription()).contains("madness");

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Declining madness cast puts the card into the graveyard")
    void decliningMadnessGoesToGraveyard() {
        GisasBidding bidding = discardViaRavensCrime();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(bidding.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(bidding.getId()));
    }

    @Test
    @DisplayName("Accepting madness cast pays {2}{B} and creates two Zombie tokens")
    void acceptingMadnessCreatesZombies() {
        discardViaRavensCrime();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        List<Permanent> zombies = zombiesOnBattlefield();
        assertThat(zombies).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInGraveyard(player1, "Gisa's Bidding");
    }

    @Test
    @DisplayName("Created tokens are black Zombie creatures under the caster's control")
    void tokensHaveCorrectCharacteristicsAndController() {
        harness.setHand(player1, List.of(new GisasBidding()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(zombiesOnBattlefield()).hasSize(2).allSatisfy(zombie -> {
            assertThat(zombie.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(zombie.getCard().getSubtypes()).containsExactly(CardSubtype.ZOMBIE);
        });
        assertThat(findPermanents(player2, "Zombie")).isEmpty();
        harness.assertInGraveyard(player1, "Gisa's Bidding");
    }

    @Test
    @DisplayName("An unaffordable madness cast moves the card to the graveyard without creating tokens")
    void unpayableMadnessGoesToGraveyard() {
        GisasBidding bidding = discardViaRavensCrime();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.findExiledCard(bidding.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bidding);
        assertThat(zombiesOnBattlefield()).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }
}
