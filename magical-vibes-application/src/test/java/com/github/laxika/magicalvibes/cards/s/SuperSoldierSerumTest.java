package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SuperSoldierSerum.class, AccordersShield.class, GrizzlyBears.class})
class SuperSoldierSerumTest extends BaseCardTest {

    @Test
    void enchantedCreatureGetsTheSerumBonuses() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addSerum(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).contains(CardSubtype.SOLDIER);
        assertThat(gqs.computeStaticBonus(gd, creature).grantedSupertypes())
                .contains(CardSupertype.LEGENDARY);
    }

    @Test
    void attackingCreatureCanHaveAnyNumberOfControlledEquipmentAttached() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addSerum(creature);
        Permanent firstEquipment = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        Permanent secondEquipment = harness.addToBattlefieldAndReturn(player1, new AccordersShield());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
        harness.handlePermanentChosen(player1, firstEquipment.getId());
        harness.handlePermanentChosen(player1, secondEquipment.getId());
        harness.passBothPriorities();

        assertThat(firstEquipment.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(secondEquipment.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void blockingCreatureCanHaveTargetedEquipmentAttached() {
        Permanent blocker = addCreatureReady(player1, new GrizzlyBears());
        addSerum(blocker);
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new AccordersShield());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(attacker)));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player1.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player2.getId()).indexOf(attacker))));

        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
        harness.handlePermanentChosen(player1, equipment.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(blocker.getId());
    }

    private Permanent addSerum(Permanent creature) {
        Permanent serum = harness.addToBattlefieldAndReturn(player1, new SuperSoldierSerum());
        serum.setAttachedTo(creature.getId());
        return serum;
    }
}
