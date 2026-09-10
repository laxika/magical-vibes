package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.s.StopCold;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Choke.class, Island.class, Forest.class})
class ChokeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped Island does not untap while Choke is out")
    void islandStaysTapped() {
        harness.addToBattlefield(player1, new Choke());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.tap();

        advanceToUpkeep(player1);

        assertThat(island.isTapped()).isTrue();
    }

    @Test
    @DisplayName("All Islands stay tapped while Choke is out")
    void allIslandsStayTapped() {
        harness.addToBattlefield(player1, new Choke());
        Permanent firstIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent secondIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        firstIsland.tap();
        secondIsland.tap();

        advanceToUpkeep(player1);

        assertThat(firstIsland.isTapped()).isTrue();
        assertThat(secondIsland.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Non-Island land untaps normally")
    void forestUntaps() {
        harness.addToBattlefield(player1, new Choke());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        forest.tap();

        advanceToUpkeep(player1);

        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Affects opponents' Islands during their untap step")
    void affectsOpponentIslands() {
        harness.addToBattlefield(player1, new Choke());
        Permanent opponentIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        opponentIsland.tap();

        advanceToUpkeep(player2);

        assertThat(opponentIsland.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Once Choke leaves, Islands untap again")
    void untapsAfterChokeLeaves() {
        Permanent choke = harness.addToBattlefieldAndReturn(player1, new Choke());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.tap();

        gd.playerBattlefields.get(player1.getId()).remove(choke);

        advanceToUpkeep(player1);

        assertThat(island.isTapped()).isFalse();
    }

    @Test
    @CardUsed({Opalescence.class, StopCold.class})
    @DisplayName("A Choke that loses all abilities no longer locks Islands")
    void losesEffectWhenChokeLosesAllAbilities() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent choke = harness.addToBattlefieldAndReturn(player1, new Choke());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        island.tap();

        assertThat(gqs.isCreature(gd, choke)).isTrue();

        harness.setHand(player1, List.of(new StopCold()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, choke.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.hasLostAllAbilities(gd, choke)).isTrue();
        advanceToUpkeep(player1);

        assertThat(island.isTapped()).isFalse();
    }
}
