package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.ArachnePsionicWeaver;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SpectacularSpiderMan;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RadioactiveSpider.class, ArachnePsionicWeaver.class, SpectacularSpiderMan.class,
        GiantSpider.class, GrizzlyBears.class})
class RadioactiveSpiderTest extends BaseCardTest {

    @Test
    void sacrificesAndOffersOnlySpiderHeroes() {
        activateAbility(List.of(
                new ArachnePsionicWeaver(),
                new GiantSpider(),
                new SpectacularSpiderMan(),
                new GrizzlyBears()));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Radioactive Spider");
        harness.assertInGraveyard(player1, "Radioactive Spider");

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(card -> card.getName())
                .containsExactly("Arachne, Psionic Weaver", "Spectacular Spider-Man");
        assertThat(search.params().reveals()).isTrue();
    }

    @Test
    void chosenSpiderHeroGoesToHand() {
        activateAbility(List.of(new ArachnePsionicWeaver(), new GiantSpider()));

        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Arachne, Psionic Weaver");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void abilityRequiresSorcerySpeed() {
        harness.addToBattlefield(player1, new RadioactiveSpider());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private void activateAbility(List<com.github.laxika.magicalvibes.model.Card> library) {
        harness.addToBattlefield(player1, new RadioactiveSpider());
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(library);
        harness.activateAbility(player1, 0, null, null);
    }
}
