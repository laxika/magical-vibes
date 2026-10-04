package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.SphereOfResistance;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IllusionaryMask.class, GrizzlyBears.class, CrawWurm.class,
        LightningBolt.class, SphereOfResistance.class})
class IllusionaryMaskTest extends BaseCardTest {

    @Test
    void castsAQualifyingCreatureFaceDown() {
        harness.addToBattlefield(player1, new IllusionaryMask());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.isFaceDown()).isTrue();
        assertThat(bears.getFaceDownPower()).isEqualTo(2);
        assertThat(bears.getFaceDownToughness()).isEqualTo(2);

        bears.addMarkedDamage(null, 1);
        assertThat(bears.isFaceDown()).isFalse();
    }

    @Test
    void doesNotOfferAColoredCostTheSpentManaCannotPay() {
        harness.addToBattlefield(player1, new IllusionaryMask());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void mayDeclineCastingTheCreature() {
        harness.addToBattlefield(player1, new IllusionaryMask());
        GrizzlyBears card = new GrizzlyBears();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    void turnsFaceUpBeforeAssigningCombatDamageAsABlocker() {
        harness.addToBattlefield(player1, new IllusionaryMask());
        harness.setHand(player1, List.of(new CrawWurm()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.activateAbility(player1, 0, 6, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        Permanent blocker = findPermanent(player1, "Craw Wurm");
        Permanent attacker = addCreatureReady(player2, new CrawWurm());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));
        resolveCombat(player2);

        assertThat(blocker.isFaceDown()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(blocker.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(attacker.getCard());
    }

    @Test
    void damageStillTurnsTheCreatureFaceUpAfterCleanup() {
        harness.addToBattlefield(player1, new IllusionaryMask());
        harness.setHand(player1, List.of(new CrawWurm()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.activateAbility(player1, 0, 6, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        Permanent wurm = findPermanent(player1, "Craw Wurm");

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, wurm.getId());

        assertThat(wurm.isFaceDown()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wurm);
        assertThat(wurm.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void paysSpellCostIncreasesWhenCastingFaceDown() {
        harness.addToBattlefield(player1, new IllusionaryMask());
        harness.addToBattlefield(player2, new SphereOfResistance());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Grizzly Bears").isFaceDown()).isTrue();
    }
}
