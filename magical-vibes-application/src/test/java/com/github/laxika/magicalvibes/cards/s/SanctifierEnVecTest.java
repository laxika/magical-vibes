package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Chatterstorm;
import com.github.laxika.magicalvibes.cards.d.Damn;
import com.github.laxika.magicalvibes.cards.d.DressDown;
import com.github.laxika.magicalvibes.cards.n.NestedShambler;
import com.github.laxika.magicalvibes.cards.u.UnholyHeat;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SanctifierEnVec.class, NestedShambler.class, Chatterstorm.class, UnholyHeat.class, DressDown.class, Damn.class})
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
        Card redCard = new UnholyHeat();
        Card blackCard = new NestedShambler();
        Card ownGreenCard = new Chatterstorm();
        Card opponentGreenCard = new Chatterstorm();
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
        Permanent shambler = harness.addToBattlefieldAndReturn(player2, new NestedShambler());
        harness.setHand(player1, List.of(new UnholyHeat()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, shambler.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(shambler.getCard());
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(shambler.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Unholy Heat"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Unholy Heat"));
    }

    @Test
    void abilityLossDisablesGraveyardReplacement() {
        harness.addToBattlefield(player1, new SanctifierEnVec());
        harness.addToBattlefield(player2, new DressDown());
        Permanent shambler = harness.addToBattlefieldAndReturn(player2, new NestedShambler());
        Card heat = new UnholyHeat();
        harness.setHand(player1, List.of(heat));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, shambler.getId());

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(shambler.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(heat);
        assertThat(gd.findExiledCard(shambler.getCard().getId())).isNull();
        assertThat(gd.findExiledCard(heat.getId())).isNull();
    }

    @Test
    void greenSpellStillGoesToGraveyard() {
        harness.addToBattlefield(player1, new SanctifierEnVec());
        Card chatterstorm = new Chatterstorm();
        harness.setHand(player1, List.of(chatterstorm));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(chatterstorm);
        assertThat(gd.findExiledCard(chatterstorm.getId())).isNull();
        harness.assertOnBattlefield(player1, "Squirrel");
    }

    @Test
    void entersWithNoMatchingGraveyardCards() {
        Card greenCard = new Chatterstorm();
        harness.setGraveyard(player2, List.of(greenCard));

        castSanctifier();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(greenCard);
        assertThat(gd.findExiledCard(greenCard.getId())).isNull();
    }

    @Test
    void exilesBlackCreatureDyingSimultaneouslyWithSanctifier() {
        Permanent sanctifier = harness.addToBattlefieldAndReturn(player1, new SanctifierEnVec());
        Permanent shambler = harness.addToBattlefieldAndReturn(player1, new NestedShambler());
        Card damn = new Damn();
        harness.setHand(player1, List.of(damn));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sanctifier.getCard(), damn);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(shambler.getCard());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(shambler.getCard());
        harness.assertNotOnBattlefield(player1, "Squirrel");
    }

    private void castSanctifier() {
        harness.setHand(player1, List.of(new SanctifierEnVec()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
