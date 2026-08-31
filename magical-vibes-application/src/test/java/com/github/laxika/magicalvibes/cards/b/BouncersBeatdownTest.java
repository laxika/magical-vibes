package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BouncersBeatdown.class, GrizzlyBears.class, HillGiant.class, ScatheZombies.class})
class BouncersBeatdownTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {2} less when targeting a black permanent")
    void costsLessWhenTargetingBlackPermanent() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new ScatheZombies());
        UUID targetId = harness.getPermanentId(player2, "Scathe Zombies");
        harness.setHand(player1, List.of(new BouncersBeatdown()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Scathe Zombies");
    }

    @Test
    @DisplayName("Requires the full cost when targeting a nonblack permanent")
    void requiresFullCostWhenTargetingNonblackPermanent() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new BouncersBeatdown()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Deals damage equal to the greatest power among creatures you control")
    void dealsGreatestControlledPowerAsDamage() {
        harness.addToBattlefield(player1, new HillGiant());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BouncersBeatdown()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(target.getId())
                        && permanent.getMarkedDamage() == 3);
    }

    @Test
    @DisplayName("Exiles a creature killed by the damage instead of putting it into the graveyard")
    void exilesCreatureKilledByDamage() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new ScatheZombies());
        UUID targetId = harness.getPermanentId(player2, "Scathe Zombies");
        harness.setHand(player1, List.of(new BouncersBeatdown()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Scathe Zombies");
        harness.assertNotInGraveyard(player2, "Scathe Zombies");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getName().equals("Scathe Zombies"));
    }
}
