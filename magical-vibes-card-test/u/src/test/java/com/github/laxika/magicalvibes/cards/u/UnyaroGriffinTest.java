package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.e.EarlyHarvest;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoblinEliteInfantry;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.cards.s.StoneRain;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnyaroGriffin.class, Incinerate.class, EarlyHarvest.class, GoblinEliteInfantry.class,
        StoneRain.class, Forest.class})
class UnyaroGriffinTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a red instant spell, sacrificing itself as a cost")
    void countersRedInstantSpell() {
        harness.addToBattlefield(player1, new UnyaroGriffin());

        Incinerate incinerate = new Incinerate();
        harness.setHand(player2, List.of(incinerate));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, incinerate.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Incinerate");
        // Unyaro Griffin sacrificed as a cost
        harness.assertInGraveyard(player1, "Unyaro Griffin");
        harness.assertNotOnBattlefield(player1, "Unyaro Griffin");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a green instant spell")
    void cannotTargetGreenInstant() {
        harness.addToBattlefield(player1, new UnyaroGriffin());

        EarlyHarvest earlyHarvest = new EarlyHarvest();
        harness.setHand(player2, List.of(earlyHarvest));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player2.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, earlyHarvest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a red creature spell (not an instant or sorcery)")
    void cannotTargetRedCreatureSpell() {
        harness.addToBattlefield(player1, new UnyaroGriffin());

        GoblinEliteInfantry goblin = new GoblinEliteInfantry();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, goblin, "{1}{R}");
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, goblin.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counters a red sorcery spell, sacrificing itself as a cost")
    void countersRedSorcerySpell() {
        harness.addToBattlefield(player1, new UnyaroGriffin());
        harness.addToBattlefield(player2, new Forest());

        StoneRain stoneRain = new StoneRain();
        harness.setHand(player2, List.of(stoneRain));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, harness.getPermanentId(player2, "Forest"));
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, stoneRain.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Stone Rain");
        harness.assertInGraveyard(player1, "Unyaro Griffin");
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifice is paid before the counter ability resolves")
    void sacrificeIsPaidImmediately() {
        harness.addToBattlefield(player1, new UnyaroGriffin());
        Incinerate incinerate = new Incinerate();
        harness.setHand(player2, List.of(incinerate));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, incinerate.getId());

        harness.assertInGraveyard(player1, "Unyaro Griffin");
        harness.assertNotOnBattlefield(player1, "Unyaro Griffin");
        harness.assertNotInGraveyard(player2, "Incinerate");
        assertThat(gd.stack).hasSize(2);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Incinerate");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Griffin can counter its controller's red spell")
    void tappedSummoningSickGriffinCanCounterOwnSpell() {
        var griffin = harness.addToBattlefieldAndReturn(player1, new UnyaroGriffin());
        griffin.tap();
        griffin.setSummoningSick(true);
        Incinerate incinerate = new Incinerate();
        harness.setHand(player1, List.of(incinerate));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.forceActivePlayer(player1);
        harness.castInstant(player1, 0, player2.getId());

        harness.activateAbility(player1, 0, null, incinerate.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Unyaro Griffin");
        harness.assertInGraveyard(player1, "Incinerate");
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Rejecting an illegal target does not sacrifice the Griffin")
    void illegalTargetDoesNotPaySacrificeCost() {
        harness.addToBattlefield(player1, new UnyaroGriffin());
        EarlyHarvest earlyHarvest = new EarlyHarvest();
        harness.setHand(player2, List.of(earlyHarvest));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player2.getId());
        harness.passPriority(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, earlyHarvest.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Unyaro Griffin");
        harness.assertNotInGraveyard(player1, "Unyaro Griffin");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A sacrifice is not refunded when another ability counters the target first")
    void sacrificeRemainsPaidWhenTargetLeavesStack() {
        harness.addToBattlefield(player1, new UnyaroGriffin());
        harness.addToBattlefield(player1, new UnyaroGriffin());
        Incinerate incinerate = new Incinerate();
        harness.setHand(player2, List.of(incinerate));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, incinerate.getId());
        harness.activateAbility(player1, 0, null, incinerate.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Unyaro Griffin");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Unyaro Griffin")).hasSize(2);
        harness.assertInGraveyard(player2, "Incinerate");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flying prevents a creature without flying or reach from blocking")
    void cannotBeBlockedByGroundCreature() {
        addCreatureReady(player1, new UnyaroGriffin());
        addCreatureReady(player2, new GoblinEliteInfantry());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }
}
