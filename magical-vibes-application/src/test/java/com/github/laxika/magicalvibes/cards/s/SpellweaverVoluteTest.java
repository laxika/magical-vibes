package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpellweaverVolute.class, LightningBolt.class, Shock.class, Divination.class})
class SpellweaverVoluteTest extends BaseCardTest {

    @Test
    @DisplayName("Enters attached to an instant card in a graveyard")
    void entersAttachedToInstantCardInGraveyard() {
        LightningBolt enchantedCard = new LightningBolt();
        harness.setGraveyard(player2, List.of(enchantedCard));
        harness.setHand(player1, List.of(new SpellweaverVolute()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, enchantedCard.getId());
        harness.passBothPriorities();

        Permanent volute = findPermanent(player1, "Spellweaver Volute");
        assertThat(volute.getAttachedTo()).isEqualTo(enchantedCard.getId());
    }

    @Test
    @DisplayName("Copies the enchanted instant and reattaches to another instant when cast")
    void copiesAndReattachesAfterCastingCopy() {
        LightningBolt enchantedCard = new LightningBolt();
        Shock anotherInstant = new Shock();
        harness.setGraveyard(player2, List.of(enchantedCard, anotherInstant));
        harness.setHand(player1, List.of(new SpellweaverVolute()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, enchantedCard.getId());
        harness.passBothPriorities();

        Permanent volute = findPermanent(player1, "Spellweaver Volute");
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castSorcery(player2, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(anotherInstant.getId()));

        assertThat(volute.getAttachedTo()).isEqualTo(anotherInstant.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .contains(enchantedCard.getId());
        assertThat(gd.stack).anyMatch(entry ->
                entry.getCard().getName().equals("Lightning Bolt") && entry.isCopy());
    }
}
