package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DeathcultRogue;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NinjaOfTheDeepHours;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KamiOfRestlessShadows.class, NinjaOfTheDeepHours.class, DeathcultRogue.class,
        GrizzlyBears.class, Forest.class})
class KamiOfRestlessShadowsTest extends BaseCardTest {

    @Test
    void returnsTargetNinjaFromGraveyardToHand() {
        Card ninja = new NinjaOfTheDeepHours();
        harness.setGraveyard(player1, List.of(ninja));
        harness.setHand(player1, List.of(new KamiOfRestlessShadows()));
        addMana();

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(ninja.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ninja.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ninja of the Deep Hours");
        harness.assertNotInGraveyard(player1, "Ninja of the Deep Hours");
    }

    @Test
    void returnsTargetRogueFromGraveyardToHand() {
        Card rogue = new DeathcultRogue();
        harness.setGraveyard(player1, List.of(rogue));
        harness.setHand(player1, List.of(new KamiOfRestlessShadows()));
        addMana();

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(rogue.getId());
        harness.handleMultipleCardsChosen(player1, List.of(rogue.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Deathcult Rogue");
        harness.assertNotInGraveyard(player1, "Deathcult Rogue");
    }

    @Test
    void firstModeCanBeDeclined() {
        harness.setHand(player1, List.of(new KamiOfRestlessShadows()));
        addMana();

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Kami of Restless Shadows");
    }

    @Test
    void firstModeCannotTargetOtherCreatureCards() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new KamiOfRestlessShadows()));
        addMana();

        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void putsTargetCreatureFromGraveyardOnTopOfLibrary() {
        Card creature = new GrizzlyBears();
        Card libraryCard = new Forest();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new KamiOfRestlessShadows()));
        addMana();

        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)
                .validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature, libraryCard);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void secondModeCannotTargetNonCreatureCards() {
        Card nonCreature = new Forest();
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.setHand(player1, List.of(new KamiOfRestlessShadows()));
        addMana();

        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Forest");
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
