package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LiquidFire.class, CrawWurm.class, GrizzlyBears.class})
class LiquidFireTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage to the creature and 5 minus X to its controller")
    void dealsSplitDamage() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new CrawWurm()).getId();
        harness.setHand(player1, List.of(new LiquidFire()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 2, targetId);

        harness.assertOnBattlefield(player2, "Craw Wurm");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Still damages the creature's controller when the creature dies")
    void dealsRemainingDamageWhenTargetDies() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new LiquidFire()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 4, targetId);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Fizzles when the target creature leaves before resolution")
    void fizzlesWhenTargetLeavesBeforeResolution() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new CrawWurm()).getId();
        harness.setHand(player1, List.of(new LiquidFire()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 2, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Liquid Fire");
    }

    @Test
    @DisplayName("Rejects a player as the target")
    void rejectsPlayerTarget() {
        harness.setHand(player1, List.of(new LiquidFire()));
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(6);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Supports both ends of the chosen range")
    void supportsZeroAndFive() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new CrawWurm()).getId();
        harness.setHand(player1, List.of(new LiquidFire()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 0, targetId);

        harness.assertOnBattlefield(player2, "Craw Wurm");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);

        harness.setHand(player1, List.of(new LiquidFire()));
        harness.addMana(player1, ManaColor.RED, 6);
        targetId = harness.getPermanentId(player2, "Craw Wurm");
        harness.castAndResolveSorcery(player1, 0, 5, targetId);

        harness.assertInGraveyard(player2, "Craw Wurm");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    @DisplayName("Rejects a chosen value outside the printed range")
    void rejectsOutOfRangeChoice() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new LiquidFire()));
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 6, targetId))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(6);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can target its caster's creature and damages that creature's controller")
    void damagesCasterWhenTargetingOwnCreature() {
        var target = harness.addToBattlefieldAndReturn(player1, new CrawWurm());
        harness.setHand(player1, List.of(new LiquidFire()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 3, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Craw Wurm");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("A zero choice deals no damage to the creature")
    void zeroChoiceDoesNotDamageCreature() {
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LiquidFire()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertLife(player2, 15);
    }

    @Test
    @DisplayName("Rejects a negative chosen value without paying mana")
    void rejectsNegativeChoice() {
        var target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LiquidFire()));
        harness.addMana(player1, ManaColor.RED, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, -1, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(6);
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Liquid Fire");
    }
}
