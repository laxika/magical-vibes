package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.n.NoviceOccultist;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({RottenReunion.class, NoviceOccultist.class})
class RottenReunionTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles up to one graveyard card and creates a decayed Zombie")
    void exilesCardAndCreatesDecayedZombie() {
        Card graveyardCard = new NoviceOccultist();
        harness.setGraveyard(player2, List.of(graveyardCard));
        castFromHand();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Novice Occultist");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(graveyardCard.getId()));
        assertDecayedZombie(findPermanent(player1, "Zombie"));
    }

    @Test
    @DisplayName("Can choose no graveyard card and still creates a decayed Zombie")
    void canChooseNoGraveyardCard() {
        Card graveyardCard = new NoviceOccultist();
        harness.setGraveyard(player2, List.of(graveyardCard));
        castFromHand();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Novice Occultist");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertDecayedZombie(findPermanent(player1, "Zombie"));
    }

    @Test
    @DisplayName("Flashback creates a decayed Zombie and exiles Rotten Reunion")
    void flashbackCreatesZombieAndExilesSpell() {
        Card rottenReunion = new RottenReunion();
        Card graveyardCard = new NoviceOccultist();
        harness.setGraveyard(player1, List.of(rottenReunion));
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFlashback(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Rotten Reunion"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(graveyardCard.getId()));
        assertDecayedZombie(findPermanent(player1, "Zombie"));
    }

    @Test
    void createsZombieWithEmptyGraveyards() {
        harness.setHand(player1, List.of(new RottenReunion()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0);

        assertDecayedZombie(findPermanent(player1, "Zombie"));
        harness.assertInGraveyard(player1, "Rotten Reunion");
        assertThat(countPermanents(player2, "Zombie")).isZero();
    }

    @Test
    void canExileNoncreatureCardFromOwnGraveyard() {
        Card target = new RottenReunion();
        harness.setGraveyard(player1, List.of(target));
        castFromHand();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
    }

    @Test
    void createsNoZombieWhenOnlyTargetLeavesGraveyard() {
        Card target = new NoviceOccultist();
        harness.setGraveyard(player2, List.of(target));
        castFromHand();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player2, List.of());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Zombie")).isZero();
        harness.assertInGraveyard(player1, "Rotten Reunion");
    }

    @Test
    void flashbackWithNoChosenTargetStillCreatesZombie() {
        Card spell = new RottenReunion();
        Card target = new NoviceOccultist();
        harness.setGraveyard(player1, List.of(spell));
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFlashback(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        harness.assertNotInGraveyard(player1, "Rotten Reunion");
        harness.assertInGraveyard(player2, "Novice Occultist");
        assertDecayedZombie(findPermanent(player1, "Zombie"));
    }

    @Test
    void flashbackIsExiledWithoutCreatingZombieWhenOnlyTargetLeavesGraveyard() {
        Card spell = new RottenReunion();
        Card target = new NoviceOccultist();
        harness.setGraveyard(player1, List.of(spell));
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFlashback(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player2, List.of());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Zombie")).isZero();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        harness.assertNotInGraveyard(player1, "Rotten Reunion");
    }

    @Test
    void decayedZombieIsSacrificedAtEndOfCombatAfterAttacking() {
        harness.setHand(player1, List.of(new RottenReunion()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0);
        Permanent zombie = findPermanent(player1, "Zombie");
        zombie.setSummoningSick(false);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            assertThat(gd.playerBattlefields.get(player1.getId())).contains(zombie);
        });
        harness.passUntil(player1, TurnStep.END_OF_COMBAT);

        harness.assertLife(player2, 18);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(zombie);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Zombie")).isZero();
    }

    private void castFromHand() {
        harness.setHand(player1, List.of(new RottenReunion()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castInstant(player1, 0);
    }

    private void assertDecayedZombie(Permanent zombie) {
        assertThat(harness.getBlockLegalityService().canBlock(gd, zombie)).isFalse();
        assertThat(zombie.getCard().isToken()).isTrue();
        assertThat(zombie.getCard().getPower()).isEqualTo(2);
        assertThat(zombie.getCard().getToughness()).isEqualTo(2);
        assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(zombie.getCard().getKeywords()).contains(Keyword.DECAYED);
    }
}
