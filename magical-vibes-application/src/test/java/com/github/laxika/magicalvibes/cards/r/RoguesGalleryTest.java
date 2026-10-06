package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HaloScarab;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WortTheRaidmother;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        RoguesGallery.class,
        AirElemental.class,
        BlackKnight.class,
        GrizzlyBears.class,
        HaloScarab.class,
        SerraAngel.class,
        ShivanDragon.class,
        Shock.class,
        WortTheRaidmother.class
})
class RoguesGalleryTest extends BaseCardTest {

    @Test
    void returnsUpToOneCreatureOfEachColor() {
        List<Card> creatures = List.of(
                new SerraAngel(),
                new AirElemental(),
                new BlackKnight(),
                new ShivanDragon(),
                new GrizzlyBears());
        RoguesGallery spell = new RoguesGallery();
        harness.setGraveyard(player1, creatures);
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();

        List<UUID> creatureIds = creatures.stream().map(Card::getId).toList();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrderElementsOf(creatureIds);
        harness.handleMultipleCardsChosen(player1, creatureIds);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrderElementsOf(creatures);
    }

    @Test
    void multicoloredCreaturesCanFillDifferentColorGroups() {
        Card first = new WortTheRaidmother();
        Card second = new WortTheRaidmother();
        RoguesGallery spell = new RoguesGallery();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).stream().map(Card::getId))
                .containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    @Test
    void cannotTargetNoncreatureCards() {
        Card shock = new Shock();
        Card creature = new SerraAngel();
        RoguesGallery spell = new RoguesGallery();
        harness.setGraveyard(player1, List.of(shock, creature));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
    }

    @Test
    void mayChooseNoTargetsEvenWhenCreaturesAreAvailable() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new RoguesGallery()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        harness.assertInGraveyard(player1, "Rogues' Gallery");
    }

    @Test
    void rejectsTwoMonocoloredCreaturesOfTheSameColor() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new RoguesGallery()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(second).doesNotContain(first);
    }

    @Test
    void multicoloredTargetCanUseTheColorNotNeededByAnotherTarget() {
        Card multicolored = new WortTheRaidmother();
        Card green = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(multicolored, green));
        harness.setHand(player1, List.of(new RoguesGallery()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(multicolored.getId(), green.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(multicolored, green);
    }

    @Test
    void excludesColorlessCreaturesAndOpponentsGraveyard() {
        Card colorless = new HaloScarab();
        Card ownCreature = new GrizzlyBears();
        Card opposingCreature = new SerraAngel();
        harness.setGraveyard(player1, List.of(colorless, ownCreature));
        harness.setGraveyard(player2, List.of(opposingCreature));
        harness.setHand(player1, List.of(new RoguesGallery()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(ownCreature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(colorless);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCreature);
    }

    @Test
    @CardUsed({RoguesGallery.class})
    void canResolveWithAnEmptyGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.castFromHand(player1, new RoguesGallery(), "{2}{B}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Rogues' Gallery");
    }

    @Test
    void returnsRemainingLegalTargetWhenAnotherLeavesTheGraveyard() {
        Card first = new SerraAngel();
        Card second = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new RoguesGallery()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.setGraveyard(player1, List.of(second));
        harness.setExile(player1, List.of(first));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
    }
}
