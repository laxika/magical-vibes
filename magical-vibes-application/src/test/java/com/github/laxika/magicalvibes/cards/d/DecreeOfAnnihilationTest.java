package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArkOfBlight;
import com.github.laxika.magicalvibes.cards.a.AvenFarseer;
import com.github.laxika.magicalvibes.cards.t.TempleOfTheFalseGod;
import com.github.laxika.magicalvibes.cards.u.Upwelling;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DecreeOfAnnihilation.class, ArkOfBlight.class, AvenFarseer.class, Dragonstorm.class,
        TempleOfTheFalseGod.class, Upwelling.class})
class DecreeOfAnnihilationTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles artifacts, creatures, lands, graveyards, and hands")
    void spellExilesTheSpecifiedZonesAndPermanentTypes() {
        Card decree = new DecreeOfAnnihilation();
        Card ownCreature = new AvenFarseer();
        Card ownArtifact = new ArkOfBlight();
        Card ownLandInHand = new TempleOfTheFalseGod();
        Card ownGraveyardCard = new Dragonstorm();
        Card opponentLand = new TempleOfTheFalseGod();
        Card opponentHandCard = new Dragonstorm();
        Card opponentGraveyardCard = new AvenFarseer();
        Card enchantment = new Upwelling();

        harness.addToBattlefield(player1, ownCreature);
        harness.addToBattlefield(player1, ownArtifact);
        harness.addToBattlefield(player1, enchantment);
        harness.addToBattlefield(player2, opponentLand);
        harness.setHand(player1, List.of(decree, ownLandInHand));
        harness.setHand(player2, List.of(opponentHandCard));
        harness.setGraveyard(player1, List.of(ownGraveyardCard));
        harness.setGraveyard(player2, List.of(opponentGraveyardCard));
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(enchantment.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(ownCreature.getId(), ownArtifact.getId(), ownLandInHand.getId(),
                        ownGraveyardCard.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(opponentLand.getId(), opponentHandCard.getId(),
                        opponentGraveyardCard.getId());
        harness.assertInGraveyard(player1, "Decree of Annihilation");
    }

    @Test
    @DisplayName("Cycling destroys all lands and still draws a card")
    void cyclingDestroysLandsAndDraws() {
        Card decree = new DecreeOfAnnihilation();
        Card draw = new Dragonstorm();
        harness.addToBattlefield(player1, new TempleOfTheFalseGod());
        harness.addToBattlefield(player2, new TempleOfTheFalseGod());
        harness.addToBattlefield(player1, new AvenFarseer());
        harness.setHand(player1, List.of(decree));
        harness.setLibrary(player1, List.of(draw));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateHandAbility(player1, 0, null);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Aven Farseer");
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Temple of the False God");
        harness.assertInGraveyard(player2, "Temple of the False God");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .doesNotContain("Temple of the False God");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(Card::getName)
                .doesNotContain("Temple of the False God");
        harness.assertInHand(player1, "Dragonstorm");
        harness.assertInGraveyard(player1, "Decree of Annihilation");
    }
}
