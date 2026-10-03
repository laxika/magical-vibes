package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ApexDevastator.class, Forest.class, GrizzlyBears.class})
class ApexDevastatorTest extends BaseCardTest {

    @Test
    @DisplayName("Apex Devastator has four independent cascade triggers")
    void cascadesFourTimes() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setLibrary(player1, List.of(
                new Forest(),
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears(),
                new GrizzlyBears()));
        harness.setHand(player1, List.of(new ApexDevastator()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                    .params().cards())
                    .extracting("name")
                    .containsExactly("Grizzly Bears");

            harness.handleCardChosen(player1, -1);
        }

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Each cascade instance goes on the stack as a separate triggered ability")
    void putsFourSeparateTriggersAboveTheSpell() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new ApexDevastator()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(5);
    }

    @Test
    @DisplayName("Cascade can cast its hit without spending any mana")
    void castsHitWithoutPayingMana() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        GrizzlyBears hit = new GrizzlyBears();
        harness.setLibrary(player1, List.of(new Forest(), hit));
        harness.setHand(player1, List.of(new ApexDevastator()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack.stream().anyMatch(entry -> entry.getCard().getId().equals(hit.getId()))
                || gd.playerBattlefields.get(player1.getId()).stream()
                .anyMatch(permanent -> permanent.getCard().getId().equals(hit.getId()))).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).noneMatch(card -> card.getId().equals(hit.getId()));
    }

    @Test
    @DisplayName("An empty library does not prevent Apex Devastator from resolving")
    void resolvesWithEmptyLibrary() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new ApexDevastator()));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        for (int i = 0; i < 5 && !gd.stack.isEmpty(); i++) {
            harness.passBothPriorities();
        }

        harness.assertOnBattlefield(player1, "Apex Devastator");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
