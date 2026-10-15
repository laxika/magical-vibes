package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MartialCoup.class, GrizzlyBears.class, SerraAngel.class})
class MartialCoupTest extends BaseCardTest {

    private long soldierTokenCount(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .filter(p -> "Soldier".equals(p.getCard().getName()))
                .count();
    }

    @Test
    void zeroCreatesNoTokensAndLeavesCreaturesAlone() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MartialCoup()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(soldierTokenCount(player1)).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Martial Coup");
    }

    @Test
    void fourDoesNotDestroyOtherCreatures() {
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new MartialCoup()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, 4);
        harness.passBothPriorities();

        assertThat(soldierTokenCount(player1)).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Serra Angel");
    }

    @Test
    void laterCoupDestroysSoldiersFromEarlierResolution() {
        harness.setHand(player1, List.of(new MartialCoup(), new MartialCoup()));
        harness.addMana(player1, ManaColor.WHITE, 11);

        harness.castSorcery(player1, 0, 2);
        harness.passBothPriorities();
        var earlierSoldierIds = gd.playerBattlefields.get(player1.getId()).stream()
                .map(com.github.laxika.magicalvibes.model.Permanent::getId)
                .toList();
        assertThat(earlierSoldierIds).hasSize(2);

        harness.castSorcery(player1, 0, 5);
        harness.passBothPriorities();

        assertThat(soldierTokenCount(player1)).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> earlierSoldierIds.contains(permanent.getId()));
    }

    @Test
    @DisplayName("X below 5 creates X Soldier tokens and leaves other creatures alone")
    void belowThresholdCreatesTokensNoWipe() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new SerraAngel());

        harness.setHand(player1, List.of(new MartialCoup()));
        harness.addMana(player1, ManaColor.WHITE, 5); // X=3 + {W}{W}

        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(soldierTokenCount(player1)).isEqualTo(3);
        // No board wipe below X=5 — pre-existing creatures survive.
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Serra Angel");
    }

    @Test
    @DisplayName("X of 5 or more creates the tokens and destroys all other creatures, sparing the new Soldiers")
    void atThresholdCreatesTokensAndWipesOtherCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears()); // caster's own creature is also destroyed
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new SerraAngel());

        harness.setHand(player1, List.of(new MartialCoup()));
        harness.addMana(player1, ManaColor.WHITE, 7); // X=5 + {W}{W}

        harness.castSorcery(player1, 0, 5);
        harness.passBothPriorities();

        // The five Soldier tokens are created first and are spared by the wipe.
        assertThat(soldierTokenCount(player1)).isEqualTo(5);

        // Every other creature is destroyed — including the caster's own.
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Serra Angel");
    }
}
