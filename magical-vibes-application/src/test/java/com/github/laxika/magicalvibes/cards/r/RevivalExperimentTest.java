package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AjaniSteadfast;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.PhyrexianArena;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RevivalExperiment.class, Ornithopter.class, PhyrexianArena.class, Forest.class,
        AjaniSteadfast.class})
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
        harness.passBothPriorities();

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
        harness.setHand(player1, List.of(spell));
        addMana();
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    private void castRevivalExperiment(RevivalExperiment spell) {
        harness.setHand(player1, List.of(spell));
        addMana();
        harness.castSorcery(player1, 0);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private void choose(Card card) {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
    }
}
