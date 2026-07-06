package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.ImprovisationCapstoneEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ImprovisationCapstoneTest extends BaseCardTest {

    

    @Test
    @DisplayName("Exiles from library until total mana value 4 or greater")
    void exilesUntilTotalManaValueFour() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Shock()));
        harness.setHand(player1, List.of(new ImprovisationCapstone()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(e -> e.card().getName()).toList())
                .contains("Forest", "Forest", "Shock");
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ImprovisationCapstoneCastChoice.class);
    }

    @Test
    @DisplayName("Choosing Shock casts it without paying mana cost")
    void castsShockWithoutPaying() {
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(shock));
        harness.addToBattlefield(player2, new com.github.laxika.magicalvibes.cards.g.GrizzlyBears());
        harness.setHand(player1, List.of(new ImprovisationCapstone()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        UUID bearId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearId);

        assertThat(gd.stack.stream().anyMatch(e -> e.getCard().getName().equals("Shock"))).isTrue();
    }

    @Test
    @DisplayName("Remaining chosen spells are still cast after a targeted spell resolves its target")
    void castsAllChosenSpellsWhenNonLastSpellNeedsTarget() {
        Shock shock = new Shock();
        com.github.laxika.magicalvibes.cards.g.GrizzlyBears bears1 =
                new com.github.laxika.magicalvibes.cards.g.GrizzlyBears();
        com.github.laxika.magicalvibes.cards.g.GrizzlyBears bears2 =
                new com.github.laxika.magicalvibes.cards.g.GrizzlyBears();
        // Shock (targeted) is exiled first, so it is cast before the two untargeted creature spells.
        harness.setLibrary(player1, List.of(shock, bears1, bears2));
        harness.setHand(player1, List.of(new ImprovisationCapstone()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId(), bears1.getId(), bears2.getId()));
        // Shock pauses for a target; resolving it must resume the queue for the remaining creatures.
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.stack.stream().anyMatch(e -> e.getCard().getName().equals("Shock"))).isTrue();
        assertThat(gd.stack.stream().filter(e -> e.getCard().getName().equals("Grizzly Bears")).count())
                .isEqualTo(2);
    }
}
