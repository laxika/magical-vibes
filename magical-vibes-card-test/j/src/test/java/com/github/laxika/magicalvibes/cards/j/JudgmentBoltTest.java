package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AstrologiansPlanisphere;
import com.github.laxika.magicalvibes.cards.d.DwarvenCastleGuard;
import com.github.laxika.magicalvibes.cards.i.IronGiant;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JudgmentBolt.class, AstrologiansPlanisphere.class, DwarvenCastleGuard.class, IronGiant.class})
class JudgmentBoltTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to a creature and damage equal to controlled Equipment to its controller")
    void dealsDamageToCreatureAndItsController() {
        harness.addToBattlefield(player1, new AstrologiansPlanisphere());
        harness.addToBattlefield(player1, new AstrologiansPlanisphere());
        harness.addToBattlefield(player2, new DwarvenCastleGuard());
        harness.setHand(player1, List.of(new JudgmentBolt()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Dwarven Castle Guard"));

        harness.assertInGraveyard(player2, "Dwarven Castle Guard");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Counts only Equipment controlled by the spell's controller")
    void countsOnlyControllersEquipment() {
        harness.addToBattlefield(player1, new AstrologiansPlanisphere());
        harness.addToBattlefield(player2, new AstrologiansPlanisphere());
        harness.addToBattlefield(player2, new IronGiant());
        harness.setHand(player1, List.of(new JudgmentBolt()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Iron Giant"));

        harness.assertOnBattlefield(player2, "Iron Giant");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Counts Equipment at resolution")
    void countsEquipmentAtResolution() {
        harness.addToBattlefield(player1, new AstrologiansPlanisphere());
        harness.addToBattlefield(player1, new AstrologiansPlanisphere());
        harness.addToBattlefield(player2, new IronGiant());
        harness.setHand(player1, List.of(new JudgmentBolt()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        var equipmentId = harness.getPermanentId(player1, "Astrologian's Planisphere");
        var targetId = harness.getPermanentId(player2, "Iron Giant");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent.getId().equals(equipmentId));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Iron Giant");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Fizzles if the target creature leaves before resolution")
    void fizzlesWhenTargetLeaves() {
        harness.addToBattlefield(player1, new AstrologiansPlanisphere());
        harness.addToBattlefield(player2, new DwarvenCastleGuard());
        harness.setHand(player1, List.of(new JudgmentBolt()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        var targetId = harness.getPermanentId(player2, "Dwarven Castle Guard");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Deals exactly five creature damage even with no Equipment")
    void dealsFiveDamageWithoutEquipment() {
        var target = harness.addToBattlefieldAndReturn(player2, new IronGiant());
        harness.setHand(player1, List.of(new JudgmentBolt()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Iron Giant");
        assertThat(target.getMarkedDamage()).isEqualTo(5);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can target your own creature and damages you rather than the opponent")
    void damagesControllerOfOwnTarget() {
        harness.addToBattlefield(player1, new AstrologiansPlanisphere());
        var target = harness.addToBattlefieldAndReturn(player1, new DwarvenCastleGuard());
        harness.setHand(player1, List.of(new JudgmentBolt()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player1, "Dwarven Castle Guard");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a noncreature Equipment or a player")
    void rejectsNoncreatureTargets() {
        var equipment = harness.addToBattlefieldAndReturn(player2, new AstrologiansPlanisphere());
        harness.setHand(player1, List.of(new JudgmentBolt()));
        harness.addMana(player1, ManaColor.RED, 4);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, equipment.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Judgment Bolt");
    }
}
