package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LumraBellowOfTheWoods.class, Forest.class, Island.class, Mountain.class,
        GrizzlyBears.class, Shock.class})
class LumraBellowOfTheWoodsTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the number of lands its controller controls")
    void powerAndToughnessEqualControlledLands() {
        Permanent lumra = addReadyLumra(player1);
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

    private Permanent addReadyLumra(Player player) {
        Permanent lumra = new Permanent(new LumraBellowOfTheWoods());
        lumra.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(lumra);
        return lumra;
    }
}
