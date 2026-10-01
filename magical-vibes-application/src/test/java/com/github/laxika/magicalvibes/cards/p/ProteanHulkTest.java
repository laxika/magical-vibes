package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AssaultZeppelid;
import com.github.laxika.magicalvibes.cards.d.DreadSlag;
import com.github.laxika.magicalvibes.cards.k.KillSuitCultist;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.cards.w.WreckingBall;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProteanHulk.class, KillSuitCultist.class, MistralCharger.class,
        AssaultZeppelid.class, DreadSlag.class, WreckingBall.class})
class ProteanHulkTest extends BaseCardTest {

    @Test
    @DisplayName("Death trigger puts chosen creatures with total mana value at most six onto the battlefield")
    void searchesForCreaturesWithinTotalManaValue() {
        Card oneManaCreature = new KillSuitCultist();
        Card twoManaCreature = new MistralCharger();
        Card fourManaCreature = new AssaultZeppelid();
        Card fiveManaCreature = new DreadSlag();
        Card tooExpensiveCreature = new ProteanHulk();
        harness.setLibrary(player1, List.of(oneManaCreature, twoManaCreature, fourManaCreature,
                fiveManaCreature, tooExpensiveCreature));

        killProteanHulk();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(
                oneManaCreature, twoManaCreature, fourManaCreature, fiveManaCreature);

        harness.handleCardChosen(player1, 1);

        search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(oneManaCreature, fourManaCreature);
        assertThat(search.params().cards()).doesNotContain(fiveManaCreature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .doesNotContain(twoManaCreature);

        harness.handleCardChosen(player1, 1);

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
        Card creature = new MistralCharger();
        harness.setLibrary(player1, List.of(creature));

        killProteanHulk();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .doesNotContain(creature);
    }

    @Test
    @DisplayName("Ignores noncreatures and creatures over the mana value limit")
    void ignoresIneligibleLibraryCards() {
        Card nonCreature = new WreckingBall();
        Card tooExpensiveCreature = new ProteanHulk();
        harness.setLibrary(player1, List.of(nonCreature, tooExpensiveCreature));

        killProteanHulk();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .doesNotContain(nonCreature, tooExpensiveCreature);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(nonCreature, tooExpensiveCreature);
    }

    private void killProteanHulk() {
        Permanent hulk = harness.addToBattlefieldAndReturn(player1, new ProteanHulk());
        harness.setHand(player1, List.of(new WreckingBall()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, hulk.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
