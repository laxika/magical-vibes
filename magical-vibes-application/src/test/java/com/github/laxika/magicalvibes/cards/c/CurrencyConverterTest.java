package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CurrencyConverter.class, Forest.class, GrizzlyBears.class})
class CurrencyConverterTest extends BaseCardTest {

    @Test
    @DisplayName("Draws, discards, and may exile the discarded card with Currency Converter")
    void discardsAndTracksCardWithConverter() {
        Permanent converter = addConverter();
        Card discarded = new GrizzlyBears();
        setUpDiscard(converter, discarded);

        assertThat(gd.findExiledCard(discarded.getId())).isNotNull();
        assertThat(gd.findExiledCard(discarded.getId()).sourcePermanentId()).isEqualTo(converter.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(discarded.getId()));
    }

    @Test
    @DisplayName("Returns an exiled land to its owner's graveyard and creates a Treasure")
    void landCreatesTreasure() {
        Permanent converter = addConverter();
        Card forest = new Forest();
        gd.addToExile(player1.getId(), forest, converter.getId());

        activateConversion(converter, forest.getId());

        assertThat(gd.findExiledCard(forest.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Rogue")).isEmpty();
    }

    @Test
    @DisplayName("Returns an exiled nonland to its owner's graveyard and creates a Rogue")
    void nonlandCreatesRogue() {
        Permanent converter = addConverter();
        Card bears = new GrizzlyBears();
        gd.addToExile(player1.getId(), bears, converter.getId());

        activateConversion(converter, bears.getId());

        assertThat(gd.findExiledCard(bears.getId())).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears);
        assertThat(findPermanents(player1, "Rogue")).hasSize(1);
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Cannot target an exiled card not exiled with Currency Converter")
    void conversionRequiresSourceTrackedTarget() {
        Permanent converter = addConverter();
        Card untracked = new GrizzlyBears();
        gd.addToExile(player1.getId(), untracked);

        assertThatThrownBy(() -> harness.activateAbility(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(converter),
                1,
                null,
                untracked.getId(),
                Zone.EXILE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exiled with this permanent");
    }

    private Permanent addConverter() {
        return harness.addToBattlefieldAndReturn(player1, new CurrencyConverter());
    }

    private void setUpDiscard(Permanent converter, Card discarded) {
        harness.setHand(player1, List.of(discarded));
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(converter), 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
    }

    private void activateConversion(Permanent converter, UUID cardId) {
        converter.untap();
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(converter), 1, null, cardId, Zone.EXILE);
        harness.passBothPriorities();
    }
}
