package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({StormOfMemories.class, Shock.class, GrizzlyBears.class, LavaAxe.class})
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

    private void castStormOfMemories() {
        harness.setHand(player1, List.of(new StormOfMemories()));
        harness.addMana(player1, ManaColor.RED, 5);
        harness.castSorcery(player1, 0);
    }
}
