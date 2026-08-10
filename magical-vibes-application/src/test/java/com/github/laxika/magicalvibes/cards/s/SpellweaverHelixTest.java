package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CranialExtraction;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SpellweaverHelixTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles exactly two target sorceries from one graveyard")
    void etbExilesTwoSorceriesFromOneGraveyard() {
        CranialExtraction first = new CranialExtraction();
        Divination second = new Divination();
        LightningBolt instant = new LightningBolt();
        harness.setGraveyard(player2, List.of(first, second, instant));
        harness.setHand(player1, List.of(new SpellweaverHelix()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.minCount()).isEqualTo(2);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactly(first.getId(), second.getId());

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        Permanent helix = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Spellweaver Helix"))
                .findFirst()
                .orElseThrow();
        assertThat(gd.getCardsExiledByPermanent(helix.getId()))
                .extracting(Card::getId)
                .containsExactly(first.getId(), second.getId());
        harness.assertInGraveyard(player2, "Lightning Bolt");
    }

    @Test
    @DisplayName("Triggers for another player's matching cast and copies the other exiled card")
    void triggersForAnyPlayersMatchingCast() {
        SpellweaverHelix helixCard = new SpellweaverHelix();
        harness.addToBattlefield(player1, helixCard);
        Permanent helix = gd.playerBattlefields.get(player1.getId()).getFirst();

        CranialExtraction imprinted = new CranialExtraction();
        Divination other = new Divination();
        gd.addToExile(player1.getId(), imprinted, helix.getId());
        gd.addToExile(player1.getId(), other, helix.getId());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new CranialExtraction()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.castSorcery(player2, 0, player1.getId());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).anyMatch(entry ->
                entry.getCard().getName().equals("Divination") && entry.isCopy());
        assertThat(gd.getCardsExiledByPermanent(helix.getId()))
                .extracting(Card::getId)
                .containsExactly(imprinted.getId(), other.getId());
    }
}
