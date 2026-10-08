package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WasteManagement.class, GrizzlyBears.class, Shock.class})
class WasteManagementTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles up to two cards from one graveyard and creates a Rogue for each creature card")
    void exilesSelectedCardsAndCreatesRoguesForCreatures() {
        Card creature = new GrizzlyBears();
        Card noncreature = new Shock();
        Card untouchedCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature, noncreature, untouchedCreature));
        harness.setHand(player1, List.of(new WasteManagement()));
        addBaseMana();

        harness.castAndResolveInstant(player1, 0, List.of(creature.getId(), noncreature.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(creature.getId(), noncreature.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(untouchedCreature);
        assertRogues(1);
    }

    @Test
    @DisplayName("When kicked, exiles the target player's graveyard and counts only creature cards")
    void kickedExilesEntireGraveyardAndCountsCreatures() {
        Card firstCreature = new GrizzlyBears();
        Card noncreature = new Shock();
        Card secondCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(firstCreature, noncreature, secondCreature));
        harness.setHand(player1, List.of(new WasteManagement()));
        addKickedMana();

        harness.castKickedInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(
                        firstCreature.getId(), noncreature.getId(), secondCreature.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertRogues(2);
    }

    @Test
    void cannotTargetCardsFromDifferentGraveyards() {
        Card ownCreature = new GrizzlyBears();
        Card opposingCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opposingCreature));
        harness.setHand(player1, List.of(new WasteManagement()));
        addBaseMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                List.of(ownCreature.getId(), opposingCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canChooseZeroTargetsWithoutExilingAnything() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new WasteManagement()));
        addBaseMana();

        harness.castAndResolveInstant(player1, 0, List.of());

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        harness.assertInGraveyard(player1, "Waste Management");
        assertRogues(0);
    }

    @Test
    void canExileOneCreatureFromOwnGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new WasteManagement()));
        addBaseMana();

        harness.castAndResolveInstant(player1, 0, List.of(creature.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertRogues(1);
    }

    @Test
    void createsNoTokensForNoncreatureCards() {
        Card noncreature = new Shock();
        harness.setGraveyard(player2, List.of(noncreature));
        harness.setHand(player1, List.of(new WasteManagement()));
        addBaseMana();

        harness.castAndResolveInstant(player1, 0, List.of(noncreature.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(noncreature);
        assertRogues(0);
    }

    @Test
    void countsOnlyRemainingLegalTargetsOnResolution() {
        Card creature = new GrizzlyBears();
        Card noncreature = new Shock();
        harness.setGraveyard(player2, List.of(creature, noncreature));
        harness.setHand(player1, List.of(new WasteManagement(), new WasteManagement()));
        addBaseMana();
        addBaseMana();

        harness.castInstant(player1, 0, List.of(creature.getId(), noncreature.getId()));
        harness.castAndResolveInstant(player1, 0, List.of(creature.getId()));
        assertRogues(1);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(creature, noncreature);
        assertRogues(1);
    }

    @Test
    void doesNotCreateTokensWhenAllTargetsBecomeIllegal() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new WasteManagement(), new WasteManagement()));
        addBaseMana();
        addBaseMana();

        harness.castInstant(player1, 0, List.of(creature.getId()));
        harness.castAndResolveInstant(player1, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
        assertRogues(1);
    }

    @Test
    void kickedCanTargetOwnGraveyardAndDoesNotExileResolvingSpell() {
        Card creature = new GrizzlyBears();
        Card noncreature = new Shock();
        harness.setGraveyard(player1, List.of(creature, noncreature));
        harness.setHand(player1, List.of(new WasteManagement()));
        addKickedMana();

        harness.castKickedInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(creature, noncreature);
        harness.assertInGraveyard(player1, "Waste Management");
        assertRogues(1);
    }

    @Test
    void kickedCanTargetEmptyGraveyard() {
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new WasteManagement()));
        addKickedMana();

        harness.castKickedInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Waste Management");
        assertRogues(0);
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private void addKickedMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    private void assertRogues(int count) {
        List<Permanent> rogues = findPermanents(player1, "Rogue");
        assertThat(rogues).hasSize(count);
        assertThat(rogues).allSatisfy(rogue -> {
            assertThat(rogue.getCard().isToken()).isTrue();
            assertThat(rogue.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(rogue.getCard().getColor()).isEqualTo(CardColor.BLACK);
            assertThat(rogue.getCard().getSubtypes()).containsExactly(CardSubtype.ROGUE);
            assertThat(rogue.getCard().getPower()).isEqualTo(2);
            assertThat(rogue.getCard().getToughness()).isEqualTo(2);
        });
    }
}
