package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormOfMemories.class, Shock.class, GrizzlyBears.class, LavaAxe.class, CounselOfTheSoratami.class})
class StormOfMemoriesTest extends BaseCardTest {

    @Test
    @DisplayName("Storm copies Storm of Memories for each spell cast before it")
    void stormCopiesForEachPreviousSpell() {
        gd.recordSpellCast(player1.getId(), new Shock());
        castStormOfMemories();

        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);
        assertThat(gd.stack.stream().filter(StackEntry::isCopy).findFirst().orElseThrow().getCard().getName())
                .isEqualTo("Storm of Memories");

        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Exiles an eligible instant or sorcery for free casting")
    void exilesEligibleSpellForFreeCasting() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        castStormOfMemories();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.findExiledCard(shock.getId())).isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.findExiledCard(shock.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(shock);
    }

    @Test
    @DisplayName("Does not exile an instant or sorcery with mana value greater than three")
    void ignoresIneligibleManaValue() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setGraveyard(player1, List.of(lavaAxe));
        castStormOfMemories();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(lavaAxe);
        assertThat(gd.findExiledCard(lavaAxe.getId())).isNull();
    }

    @Test
    @DisplayName("Declining the free cast leaves the selected card in exile")
    void decliningFreeCastLeavesCardExiled() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        castStormOfMemories();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(shock.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(shock);
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Creature cards and the opponent's graveyard are not eligible")
    void ignoresCreaturesAndOpponentsGraveyard() {
        GrizzlyBears bears = new GrizzlyBears();
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(bears));
        harness.setGraveyard(player2, List.of(shock));
        castStormOfMemories();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(shock);
        assertThat(gd.findExiledCard(bears.getId())).isNull();
        assertThat(gd.findExiledCard(shock.getId())).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Storm counts both players' spells and selects separately for each copy")
    void stormCopiesIndependentlyExileRemainingCards() {
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());
        gd.recordSpellCast(player2.getId(), new Shock());
        Shock first = new Shock();
        Shock second = new Shock();
        Shock third = new Shock();
        List<Shock> cards = List.of(first, second, third);
        harness.setGraveyard(player1, List.of(first, second, third));
        castStormOfMemories();
        harness.passBothPriorities();

        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);
        for (int remaining = 2; remaining >= 0; remaining--) {
            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, false);
            assertThat(gd.playerGraveyards.get(player1.getId()).stream()
                    .filter(card -> cards.contains(card))).hasSize(remaining);
        }

        assertThat(cards).allSatisfy(card -> assertThat(gd.findExiledCard(card.getId())).isNotNull());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getTotalSpellsCastThisTurnCount()).isEqualTo(3);
    }
    @Test
    @DisplayName("Casts a sorcery with mana value exactly three without paying its cost")
    void castsSorceryAtManaValueLimit() {
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        Shock drawnShock = new Shock();
        GrizzlyBears drawnBears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnShock, drawnBears));
        harness.setGraveyard(player1, List.of(counsel));
        castStormOfMemories();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnShock, drawnBears);
        assertThat(gd.findExiledCard(counsel.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(counsel);
    }
    private void castStormOfMemories() {
        harness.setHand(player1, List.of(new StormOfMemories()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0);
    }
}
