package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FaerieGuidemother;
import com.github.laxika.magicalvibes.cards.g.GiftOfTheFae;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.StokeGenius;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HearthElemental.class, StokeGenius.class, Shock.class, FaerieGuidemother.class,
        GiftOfTheFae.class, GrizzlyBears.class, Forest.class, Mountain.class})
class HearthElementalTest extends BaseCardTest {

    @Test
    void canCastForFullCostWithNoQualifyingGraveyardCards() {
        HearthElemental card = new HearthElemental();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void costsOneLessForEachInstantSorceryAndAdventureCardInGraveyard() {
        Shock instant = new Shock();
        LavaAxe sorcery = new LavaAxe();
        FaerieGuidemother adventure = new FaerieGuidemother();
        harness.setGraveyard(player1, List.of(instant, sorcery, adventure));
        harness.setHand(player1, List.of(new HearthElemental()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void nonQualifyingGraveyardCardsDoNotReduceCost() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new HearthElemental()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void adventureDiscardsHandDrawsTwoAndExilesTheCard() {
        HearthElemental card = new HearthElemental();
        GrizzlyBears discardedCreature = new GrizzlyBears();
        Shock discardedInstant = new Shock();
        Forest forest = new Forest();
        Mountain mountain = new Mountain();
        harness.setHand(player1, new ArrayList<>(List.of(card, discardedCreature, discardedInstant)));
        harness.setLibrary(player1, new ArrayList<>(List.of(forest, mountain)));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(forest, mountain);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(discardedCreature, discardedInstant);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }
}
