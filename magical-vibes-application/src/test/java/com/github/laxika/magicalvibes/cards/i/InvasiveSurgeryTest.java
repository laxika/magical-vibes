package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InvasiveSurgery.class, AirElemental.class, Forest.class, GrizzlyBears.class,
        LavaAxe.class, Millstone.class, Shock.class})
class InvasiveSurgeryTest extends BaseCardTest {

    @Test
    @DisplayName("Without delirium, counters the sorcery without searching or exiling")
    void withoutDeliriumCountersSorcery() {
        LavaAxe target = new LavaAxe();
        harness.setHand(player1, List.of(target));
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.setHand(player2, List.of(new InvasiveSurgery()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Lava Axe");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Invasive Surgery");
    }

    @Test
    @DisplayName("With delirium, counters the sorcery and can choose every same-name copy")
    void withDeliriumCountersAndExilesSameNameCopies() {
        LavaAxe target = new LavaAxe();
        LavaAxe handCopy = new LavaAxe();
        harness.setHand(player1, List.of(target, handCopy));
        harness.setGraveyard(player1, List.of(new LavaAxe()));
        harness.setGraveyard(player2, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
        harness.addMana(player1, ManaColor.RED, 5);

        GameData gd = harness.getGameData();
        harness.setLibrary(player1, List.of(new LavaAxe(), new Forest()));

        harness.setHand(player2, List.of(new InvasiveSurgery()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiZoneExileChoice.class);
        List<UUID> matchingIds = Stream.of(
                        gd.playerGraveyards.get(player1.getId()),
                        gd.playerHands.get(player1.getId()),
                        gd.playerDecks.get(player1.getId()))
                .flatMap(List::stream)
                .filter(card -> card.getName().equals("Lava Axe"))
                .map(Card::getId)
                .toList();
        harness.handleMultipleCardsChosen(player2, matchingIds);
        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getName().equals("Lava Axe"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .filteredOn(card -> card.getName().equals("Lava Axe"))
                .hasSize(4);
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Lava Axe"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Lava Axe"));
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Lava Axe"));
    }

    @Test
    @DisplayName("Can target only sorcery spells")
    void cannotTargetCreatureSpell() {
        AirElemental target = new AirElemental();
        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.setHand(player2, List.of(new InvasiveSurgery()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a sorcery spell");
    }

    @Test
    @DisplayName("Delirium permits choosing zero matching cards to exile")
    void canChooseZeroCardsWithDelirium() {
        LavaAxe target = new LavaAxe();
        LavaAxe handCopy = new LavaAxe();
        LavaAxe libraryCopy = new LavaAxe();
        harness.setHand(player1, List.of(target, handCopy));
        harness.setLibrary(player1, List.of(libraryCopy, new Forest()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.setGraveyard(player2, List.of(
                new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
        harness.setHand(player2, List.of(new InvasiveSurgery()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiZoneExileChoice.class);
        harness.handleMultipleCardsChosen(player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target);
        assertThat(gd.playerHands.get(player1.getId())).contains(handCopy);
        assertThat(gd.playerDecks.get(player1.getId())).contains(libraryCopy);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }
}
