package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BlackCat;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SanctifierEnVec.class, BlackCat.class, DoomBlade.class, GrizzlyBears.class, Shock.class})
class SanctifierEnVecTest extends BaseCardTest {

    @Test
    void hasProtectionFromBlackAndRed() {
        Permanent sanctifier = harness.addToBattlefieldAndReturn(player1, new SanctifierEnVec());

        assertThat(gqs.hasProtectionFrom(gd, sanctifier, CardColor.BLACK)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, sanctifier, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, sanctifier, CardColor.GREEN)).isFalse();
    }

    @Test
    void entersAndExilesBlackAndRedCardsFromAllGraveyards() {
        Card redCard = new Shock();
        Card blackCard = new DoomBlade();
        Card ownGreenCard = new GrizzlyBears();
        Card opponentGreenCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(redCard, ownGreenCard));
        harness.setGraveyard(player2, List.of(blackCard, opponentGreenCard));

        castSanctifier();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownGreenCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentGreenCard);
        assertThat(gd.findExiledCard(redCard.getId())).isNotNull();
        assertThat(gd.findExiledCard(blackCard.getId())).isNotNull();
    }

    @Test
    void exilesBlackPermanentAndRedSpellInsteadOfGraveyards() {
        harness.addToBattlefield(player1, new SanctifierEnVec());
        Permanent blackCat = harness.addToBattlefieldAndReturn(player2, new BlackCat());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, blackCat.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(blackCat.getCard());
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(blackCat.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Shock"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Shock"));
    }

    private void castSanctifier() {
        harness.setHand(player1, List.of(new SanctifierEnVec()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
