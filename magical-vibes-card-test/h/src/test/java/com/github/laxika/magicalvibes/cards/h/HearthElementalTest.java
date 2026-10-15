package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.r.RedcapThief;
import com.github.laxika.magicalvibes.cards.s.StokeGenius;
import com.github.laxika.magicalvibes.cards.t.TorchTheTower;
import com.github.laxika.magicalvibes.cards.w.WitchsMark;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HearthElemental.class, StokeGenius.class, TorchTheTower.class, WitchsMark.class,
        RedcapThief.class, Forest.class, Mountain.class})
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
        TorchTheTower instant = new TorchTheTower();
        WitchsMark sorcery = new WitchsMark();
        HearthElemental adventure = new HearthElemental();
        harness.setGraveyard(player1, List.of(instant, sorcery, adventure));
        harness.setHand(player1, List.of(new HearthElemental()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void nonQualifyingGraveyardCardsDoNotReduceCost() {
        harness.setGraveyard(player1, List.of(new RedcapThief()));
        harness.setHand(player1, List.of(new HearthElemental()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void adventureDiscardsHandDrawsTwoAndExilesTheCard() {
        HearthElemental card = new HearthElemental();
        RedcapThief discardedCreature = new RedcapThief();
        TorchTheTower discardedInstant = new TorchTheTower();
        Forest forest = new Forest();
        Mountain mountain = new Mountain();
        harness.setHand(player1, List.of(card, discardedCreature, discardedInstant));
        harness.setLibrary(player1, List.of(forest, mountain));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(forest, mountain);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(discardedCreature, discardedInstant);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
    }

    @Test
    void opponentsQualifyingGraveyardCardsDoNotReduceCost() {
        harness.setGraveyard(player2, List.of(new TorchTheTower(), new WitchsMark(), new HearthElemental()));
        harness.setHand(player1, List.of(new HearthElemental()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void excessReductionStillRequiresRedMana() {
        harness.setGraveyard(player1, List.of(new HearthElemental(), new HearthElemental(),
                new HearthElemental(), new HearthElemental(), new HearthElemental(), new HearthElemental()));
        harness.setHand(player1, List.of(new HearthElemental()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void creatureCostReductionDoesNotReduceAdventureCost() {
        harness.setGraveyard(player1, List.of(new TorchTheTower(), new WitchsMark(), new HearthElemental()));
        harness.setHand(player1, List.of(new HearthElemental()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castAdventure(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAdventure(player1, 0, List.of());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void adventureDrawsTwoWithNoOtherCardsInHandAndCreatureCanBeCastFromExile() {
        HearthElemental card = new HearthElemental();
        Forest forest = new Forest();
        Mountain mountain = new Mountain();
        harness.setHand(player1, List.of(card));
        harness.setLibrary(player1, List.of(forest, mountain));
        harness.setGraveyard(player1, List.of(new TorchTheTower(), new WitchsMark(), new HearthElemental()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(forest, mountain);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hearth Elemental");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
