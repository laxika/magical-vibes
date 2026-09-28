package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheMotherlodeExcavator.class, EvolvingWilds.class, Forest.class,
        GrizzlyBears.class, Ornithopter.class})
class TheMotherlodeExcavatorTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with energy equal to target opponent's nonbasic lands")
    void entersWithEnergyForTargetOpponentsNonbasicLands() {
        harness.addToBattlefield(player2, new EvolvingWilds());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new TheMotherlodeExcavator()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Pays four energy to destroy a nonbasic land and stop nonfliers from blocking")
    void paysEnergyToDestroyLandAndStopNonfliersFromBlocking() {
        Permanent motherlode = addCreatureReady(player1, new TheMotherlodeExcavator());
        Permanent nonbasicLand = harness.addToBattlefieldAndReturn(player2, new EvolvingWilds());
        Permanent basicLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent groundCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent flyingCreature = addCreatureReady(player2, new Ornithopter());
        gd.playerEnergyCounters.put(player1.getId(), 4);

        declareAttackers(List.of(0));

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds()).containsExactly(nonbasicLand.getId());
        harness.handlePermanentChosen(player1, nonbasicLand.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(nonbasicLand);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(basicLand, flyingCreature);
        assertThat(bls.canBlockAttacker(gd, groundCreature, motherlode,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, flyingCreature, motherlode,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
    }

    @Test
    @DisplayName("Declining the payment does nothing")
    void decliningPaymentDoesNothing() {
        addCreatureReady(player1, new TheMotherlodeExcavator());
        Permanent nonbasicLand = harness.addToBattlefieldAndReturn(player2, new EvolvingWilds());
        Permanent groundCreature = addCreatureReady(player2, new GrizzlyBears());
        gd.playerEnergyCounters.put(player1.getId(), 4);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, nonbasicLand.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(nonbasicLand);
        assertThat(groundCreature.isCantBlockThisTurn()).isFalse();
    }

    @Test
    @DisplayName("Cannot pay the attack cost without enough energy")
    void cannotPayWithoutEnoughEnergy() {
        addCreatureReady(player1, new TheMotherlodeExcavator());
        Permanent nonbasicLand = harness.addToBattlefieldAndReturn(player2, new EvolvingWilds());
        Permanent groundCreature = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, nonbasicLand.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(nonbasicLand);
        assertThat(groundCreature.isCantBlockThisTurn()).isFalse();
    }
}
