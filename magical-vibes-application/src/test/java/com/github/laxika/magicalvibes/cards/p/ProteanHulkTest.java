package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProteanHulk.class, AirElemental.class, DoomBlade.class, GrizzlyBears.class,
        HillGiant.class, LlanowarElves.class})
class ProteanHulkTest extends BaseCardTest {

    @Test
    @DisplayName("Death trigger puts chosen creatures with total mana value at most six onto the battlefield")
    void searchesForCreaturesWithinTotalManaValue() {
        Card oneManaCreature = new LlanowarElves();
        Card twoManaCreature = new GrizzlyBears();
        Card fourManaCreature = new HillGiant();
        Card fiveManaCreature = new AirElemental();
        Card tooExpensiveCreature = new ProteanHulk();
        harness.setLibrary(player1, List.of(oneManaCreature, twoManaCreature, fourManaCreature,
                fiveManaCreature, tooExpensiveCreature));

        killProteanHulk();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(
                oneManaCreature, twoManaCreature, fourManaCreature, fiveManaCreature);

        chooseCard(1);

        search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(oneManaCreature, fourManaCreature);
        assertThat(search.params().cards()).doesNotContain(fiveManaCreature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .doesNotContain(twoManaCreature);

        chooseCard(1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .contains(twoManaCreature, fourManaCreature)
                .doesNotContain(oneManaCreature, fiveManaCreature, tooExpensiveCreature);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(oneManaCreature, fiveManaCreature, tooExpensiveCreature);
    }

    @Test
    @DisplayName("The controller may choose zero creatures")
    void mayChooseNoCreatures() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));

        killProteanHulk();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        chooseCard(-1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .doesNotContain(creature);
    }

    private void killProteanHulk() {
        Permanent hulk = harness.addToBattlefieldAndReturn(player1, new ProteanHulk());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, hulk.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void chooseCard(int index) {
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(index));
    }
}
