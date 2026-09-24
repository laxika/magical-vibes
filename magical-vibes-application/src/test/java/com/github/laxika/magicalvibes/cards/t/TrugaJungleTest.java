package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrugaJungle.class, Forest.class, Island.class, Shock.class})
class TrugaJungleTest extends BaseCardTest {

    private void addPlane() {
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new TrugaJungle(), gd.nextTimestamp()));
    }

    @Test
    void allLandsGainAnyColorManaAbility() {
        addPlane();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Island());

        Permanent forest = findPermanent(player1, "Forest");
        int forestIndex = gd.playerBattlefields.get(player1.getId()).indexOf(forest);
        harness.activateAbility(player1, forestIndex, null, null);
        harness.handleListChoice(player1, "RED");

        Permanent island = findPermanent(player2, "Island");
        int islandIndex = gd.playerBattlefields.get(player2.getId()).indexOf(island);
        harness.activateAbility(player2, islandIndex, null, null);
        harness.handleListChoice(player2, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    void chaosPutsRevealedLandsIntoHandAndRestOnBottom() {
        addPlane();
        Forest forest = new Forest();
        Shock shock = new Shock();
        Island island = new Island();
        List<com.github.laxika.magicalvibes.model.Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(forest, shock, island));

        PlanechaseService planar = com.github.laxika.magicalvibes.testutil.GameTestEngineContext.get()
                .getBean(PlanechaseService.class);
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(forest, island);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(deck).containsExactly(shock);
    }
}
