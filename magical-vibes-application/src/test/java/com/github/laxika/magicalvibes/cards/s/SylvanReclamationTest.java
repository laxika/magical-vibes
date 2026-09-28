package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SylvanReclamation.class, AuraOfSilence.class, Forest.class, GrizzlyBears.class, Ornithopter.class})
class SylvanReclamationTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles up to two target artifacts and enchantments")
    void exilesTwoTargets() {
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new AuraOfSilence());
        UUID artifactId = harness.getPermanentId(player2, "Ornithopter");
        UUID enchantmentId = harness.getPermanentId(player2, "Aura of Silence");

        harness.setHand(player1, List.of(new SylvanReclamation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, List.of(artifactId, enchantmentId));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertNotOnBattlefield(player2, "Aura of Silence");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .contains("Ornithopter", "Aura of Silence");
    }

    @Test
    @DisplayName("Cannot target a nonartifact, nonenchantment permanent")
    void cannotTargetCreature() {
        UUID creatureId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new SylvanReclamation()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creatureId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Basic landcycling searches for a basic land")
    void basicLandcyclingSearchesForBasicLand() {
        SylvanReclamation reclamation = new SylvanReclamation();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(reclamation));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(forest);
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Sylvan Reclamation");
    }
}
