package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.v.ValakutTheMoltenPinnacle;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LumraBellowOfTheWoods.class, Forest.class, Island.class, Mountain.class,
        GrizzlyBears.class, Shock.class, ValakutTheMoltenPinnacle.class})
class LumraBellowOfTheWoodsTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of lands its controller controls")
    void powerAndToughnessEqualControlledLands() {
        Permanent lumra = harness.addToBattlefieldAndReturn(player1, new LumraBellowOfTheWoods());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Mountain());

        assertThat(gqs.getEffectivePower(gd, lumra)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lumra)).isEqualTo(2);
    }

    @Test
    @DisplayName("Mills four cards, then returns all own graveyard lands tapped")
    void millsThenReturnsAllOwnGraveyardLandsTapped() {
        Card milledForest = new Forest();
        Card milledIsland = new Island();
        Card milledCreature = new GrizzlyBears();
        Card milledInstant = new Shock();
        Card graveyardMountain = new Mountain();
        Card graveyardCreature = new GrizzlyBears();
        Card opponentForest = new Forest();

        harness.addToBattlefield(player1, new Forest());
        harness.setLibrary(player1, List.of(milledForest, milledIsland, milledCreature, milledInstant));
        harness.setGraveyard(player1, List.of(graveyardMountain, graveyardCreature));
        harness.setGraveyard(player2, List.of(opponentForest));
        harness.setHand(player1, List.of(new LumraBellowOfTheWoods()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> returnedLands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() == milledForest
                        || p.getCard() == milledIsland
                        || p.getCard() == graveyardMountain)
                .toList();
        assertThat(returnedLands).hasSize(3).allMatch(Permanent::isTapped);
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(
                p -> p.getCard().hasType(CardType.LAND)).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(graveyardCreature, milledCreature, milledInstant);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentForest);
    }

    @Test
    void characteristicPowerAndToughnessApplyOutsideTheBattlefield() {
        Card lumra = new LumraBellowOfTheWoods();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(lumra));

        assertThat(gqs.getEffectiveCardPower(gd, lumra)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, lumra)).isEqualTo(1);

        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(lumra));
        harness.addToBattlefield(player1, new Island());

        assertThat(gqs.getEffectiveCardPower(gd, lumra)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardToughness(gd, lumra)).isEqualTo(2);
    }

    @Test
    void shortLibraryStillReturnsExistingAndMilledLands() {
        Card forest = new Forest();
        Card island = new Island();
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new Mountain());
        harness.setLibrary(player1, List.of(forest, creature));
        harness.setGraveyard(player1, List.of(island));
        harness.setHand(player1, List.of(new LumraBellowOfTheWoods()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() == forest || p.getCard() == island)
                .hasSize(2).allMatch(Permanent::isTapped);
        Permanent lumra = findPermanent(player1, "Lumra, Bellow of the Woods");
        assertThat(gqs.getEffectivePower(gd, lumra)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lumra)).isEqualTo(3);
    }

    @Test
    void triggerReturnsLandsEvenAfterZeroToughnessLumraDies() {
        Card lumra = new LumraBellowOfTheWoods();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(forest));
        harness.setHand(player1, List.of(lumra));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(lumra);
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getCard() == lumra);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(lumra);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() == forest).hasSize(1).allMatch(Permanent::isTapped);
    }

    @Test
    void returnedMountainsEnterSimultaneouslyForValakut() {
        harness.addToBattlefield(player1, new ValakutTheMoltenPinnacle());
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player1, new Mountain());
        }
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(new Mountain(), new Mountain()));
        harness.setHand(player1, List.of(new LumraBellowOfTheWoods()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player2, 14);
    }
}
