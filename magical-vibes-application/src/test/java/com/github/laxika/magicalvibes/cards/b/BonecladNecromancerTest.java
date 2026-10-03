package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.v.Vorstclaw;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BonecladNecromancer.class, Vorstclaw.class, Shock.class})
class BonecladNecromancerTest extends BaseCardTest {

    private void castNecromancer() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BonecladNecromancer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting the ETB ability exiles a creature card and creates a Zombie")
    void exilesCreatureAndCreatesZombie() {
        Card creature = new Vorstclaw();
        harness.setGraveyard(player2, List.of(creature));

        castNecromancer();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInGraveyard(player2, "Vorstclaw");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
        harness.assertOnBattlefield(player1, "Zombie");
        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        Card token = findPermanents(player1, "Zombie").getFirst().getCard();
        assertThat(token.isToken()).isTrue();
        assertThat(token.hasType(CardType.CREATURE)).isTrue();
        assertThat(token.getPower()).isEqualTo(2);
        assertThat(token.getToughness()).isEqualTo(2);
        assertThat(token.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(findPermanents(player2, "Zombie")).isEmpty();
    }

    @Test
    @DisplayName("Declining the ETB ability leaves the graveyard unchanged")
    void decliningLeavesGraveyardUnchanged() {
        Card creature = new Vorstclaw();
        harness.setGraveyard(player2, List.of(creature));

        castNecromancer();

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player2, "Vorstclaw");
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    @Test
    @DisplayName("The ETB ability is not put on the stack without a creature card target")
    void noncreatureCardCannotBeChosen() {
        harness.setGraveyard(player2, List.of(new Shock()));

        castNecromancer();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player2, "Shock");
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }

    @Test
    @DisplayName("A creature card in the controller's graveyard can be exiled")
    void canExileFromOwnGraveyard() {
        Card creature = new Vorstclaw();
        harness.setGraveyard(player1, List.of(creature));

        castNecromancer();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotInGraveyard(player1, "Vorstclaw");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getId()));
        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
    }

    @Test
    @DisplayName("No Zombie is created when the target leaves the graveyard before resolution")
    void missingTargetCreatesNoZombie() {
        Card creature = new Vorstclaw();
        harness.setGraveyard(player2, List.of(creature));

        castNecromancer();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player2, List.of(creature));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player2, "Vorstclaw");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(findPermanents(player1, "Zombie")).isEmpty();
    }
}
