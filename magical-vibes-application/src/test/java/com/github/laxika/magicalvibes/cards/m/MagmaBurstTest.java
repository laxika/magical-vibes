package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AncientSpider;
import com.github.laxika.magicalvibes.cards.t.TerminalMoraine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MagmaBurst.class, TerminalMoraine.class, AncientSpider.class})
class MagmaBurstTest extends BaseCardTest {

    @Test
    void dealsThreeDamageToAnyTargetWithoutKicker() {
        harness.setHand(player1, List.of(new MagmaBurst()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void kickedSpellDealsThreeDamageToAnotherTargetAndSacrificesTwoLands() {
        var firstLand = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        var secondLand = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        var creature = harness.addToBattlefieldAndReturn(player2, new AncientSpider());
        harness.setHand(player1, List.of(new MagmaBurst()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castKickedInstantWithSacrifices(player1, 0, player2.getId(),
                List.of(creature.getId()), List.of(firstLand.getId(), secondLand.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(creature.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    void kickerRequiresExactlyTwoLands() {
        var land = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        var creature = harness.addToBattlefieldAndReturn(player2, new AncientSpider());
        harness.setHand(player1, List.of(new MagmaBurst()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifices(player1, 0, player2.getId(),
                List.of(creature.getId()), List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must sacrifice 2");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(4);
    }

    @Test
    void kickerRejectsNonLandSacrifice() {
        var nonland = harness.addToBattlefieldAndReturn(player1, new AncientSpider());
        var land = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        var creature = harness.addToBattlefieldAndReturn(player2, new AncientSpider());
        harness.setHand(player1, List.of(new MagmaBurst()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifices(player1, 0, player2.getId(),
                List.of(creature.getId()), List.of(nonland.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(4);
    }

    @Test
    void kickedSpellStillDamagesPrimaryTargetIfAnotherTargetLeavesBeforeResolution() {
        var firstLand = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        var secondLand = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        var creature = harness.addToBattlefieldAndReturn(player2, new AncientSpider());
        harness.setHand(player1, List.of(new MagmaBurst()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castKickedInstantWithSacrifices(player1, 0, player2.getId(),
                List.of(creature.getId()), List.of(firstLand.getId(), secondLand.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void kickedSpellRequiresAnotherTarget() {
        var firstLand = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        var secondLand = harness.addToBattlefieldAndReturn(player1, new TerminalMoraine());
        harness.setHand(player1, List.of(new MagmaBurst()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castKickedInstantWithSacrifices(player1, 0, player2.getId(),
                List.of(player2.getId()), List.of(firstLand.getId(), secondLand.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
