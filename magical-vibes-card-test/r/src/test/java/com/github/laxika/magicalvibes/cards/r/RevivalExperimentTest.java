package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AjaniSteadfast;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrafdiggersCage;
import com.github.laxika.magicalvibes.cards.i.InvasionOfSegovia;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.PhyrexianArena;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RevivalExperiment.class, Ornithopter.class, PhyrexianArena.class, Forest.class,
        AjaniSteadfast.class, GrafdiggersCage.class, InvasionOfSegovia.class})
class RevivalExperimentTest extends BaseCardTest {

    @Test
    @DisplayName("Returns one card of each permanent type, loses life for each, and exiles itself")
    void returnsCardsOfEachPermanentType() {
        RevivalExperiment spell = new RevivalExperiment();
        Card artifactCreature = new Ornithopter();
        Card enchantment = new PhyrexianArena();
        Card land = new Forest();
        Card planeswalker = new AjaniSteadfast();
        harness.setGraveyard(player1, List.of(artifactCreature, enchantment, land, planeswalker));
        castRevivalExperiment(spell);

        choose(artifactCreature);
        choose(artifactCreature);
        choose(enchantment);
        choose(land);
        choose(planeswalker);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(artifactCreature.getId(), enchantment.getId(), land.getId(),
                        planeswalker.getId());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(8);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("Does not lose life when no cards are returned")
    void noCardsReturnedMeansNoLifeLoss() {
        RevivalExperiment spell = new RevivalExperiment();
        castRevivalExperiment(spell);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("Chooses graveyard cards during resolution, without targeting")
    void choosesDuringResolution() {
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        RevivalExperiment spell = new RevivalExperiment();

        harness.castFromHand(player1, spell, "{4}{B}{G}");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        choose(land);
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("May decline available cards without losing life")
    void canDeclineAvailableCards() {
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        RevivalExperiment spell = new RevivalExperiment();
        castRevivalExperiment(spell);

        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertLife(player1, 20);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("Returns a battle and loses three life for it")
    void returnsBattleCards() {
        Card battle = new InvasionOfSegovia();
        harness.setGraveyard(player1, List.of(battle));
        RevivalExperiment spell = new RevivalExperiment();
        castRevivalExperiment(spell);

        choose(battle);

        harness.assertOnBattlefield(player1, "Invasion of Segovia");
        harness.assertNotInGraveyard(player1, "Invasion of Segovia");
        harness.assertLife(player1, 17);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("Does not lose life for a creature that cannot enter the battlefield")
    void countsOnlyCardsActuallyReturned() {
        harness.addToBattlefield(player2, new GrafdiggersCage());
        Card creature = new Ornithopter();
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(creature, land));
        RevivalExperiment spell = new RevivalExperiment();
        castRevivalExperiment(spell);

        choose(creature);
        harness.handleMultipleCardsChosen(player1, List.of());
        choose(land);

        harness.assertInGraveyard(player1, "Ornithopter");
        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertOnBattlefield(player1, "Forest");
        harness.assertLife(player1, 17);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    private void castRevivalExperiment(RevivalExperiment spell) {
        harness.castFromHand(player1, spell, "{4}{B}{G}");
        harness.passBothPriorities();
    }

    private void choose(Card card) {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
    }
}
