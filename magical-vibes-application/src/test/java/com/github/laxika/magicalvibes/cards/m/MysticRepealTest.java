package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MysticRepeal.class, AngelicChorus.class, GrizzlyBears.class})
class MysticRepealTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a target enchantment on the bottom of its owner's library")
    void putsEnchantmentOnBottomOfOwnersLibrary() {
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID targetId = harness.getPermanentId(player2, "Angelic Chorus");
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new MysticRepeal()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertNotInGraveyard(player2, "Angelic Chorus");
        assertThat(gd.playerDecks.get(player2.getId()))
                .hasSize(deckSizeBefore + 1)
                .last()
                .extracting(Card::getName)
                .isEqualTo("Angelic Chorus");
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new MysticRepeal()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("enchantment");
    }

    @Test
    @DisplayName("Returns an enchantment controlled by an opponent to its owner's library")
    void returnsStolenEnchantmentToOwnersLibrary() {
        AngelicChorus enchantment = new AngelicChorus();
        enchantment.setOwnerId(player1.getId());
        UUID targetId = harness.addToBattlefieldAndReturn(player2, enchantment).getId();
        GrizzlyBears existingCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(existingCard));
        int controllerDeckSize = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new MysticRepeal()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertNotInGraveyard(player1, "Angelic Chorus");
        harness.assertNotInGraveyard(player2, "Angelic Chorus");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(existingCard, enchantment);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(controllerDeckSize);
    }

    @Test
    @DisplayName("Does nothing when its target leaves the battlefield before resolution")
    void doesNothingWhenTargetLeavesBeforeResolution() {
        AngelicChorus enchantment = new AngelicChorus();
        UUID targetId = harness.addToBattlefieldAndReturn(player2, enchantment).getId();
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new MysticRepeal(), new MysticRepeal()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, targetId);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        assertThat(gd.playerDecks.get(player2.getId()))
                .hasSize(deckSizeBefore + 1)
                .last().isSameAs(enchantment);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof MysticRepeal).hasSize(2);
    }
}
