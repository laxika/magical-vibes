package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ImmersturmBattlefield.class, GrizzlyBears.class})
class ImmersturmBattlefieldTest extends BaseCardTest {

    @Test
    @CardUsed(Opalescence.class)
    void animatedRealmCanHostItselfAndReceiveItsOwnBonus() {
        Permanent realm = harness.addToBattlefieldAndReturn(player1, new ImmersturmBattlefield());
        harness.addToBattlefield(player1, new Opalescence());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(realm),
                null, realm.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, realm)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, realm)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, realm, Keyword.HASTE)).isTrue();
    }

    @Test
    void repeatedHostingDoesNotStackButDifferentRealmsDo() {
        Permanent firstRealm = harness.addToBattlefieldAndReturn(player1, new ImmersturmBattlefield());
        Permanent secondRealm = harness.addToBattlefieldAndReturn(player1, new ImmersturmBattlefield());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 6);

        int firstIndex = gd.playerBattlefields.get(player1.getId()).indexOf(firstRealm);
        for (int activation = 0; activation < 2; activation++) {
            harness.activateAbility(player1, firstIndex, null, creature.getId());
            harness.passBothPriorities();
            assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        }

        int secondIndex = gd.playerBattlefields.get(player1.getId()).indexOf(secondRealm);
        harness.activateAbility(player1, secondIndex, null, creature.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, firstRealm));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, secondRealm));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isFalse();
    }

    @Test
    void hostingDoesNotFollowCreatureThatLeavesAndReturns() {
        Permanent realm = harness.addToBattlefieldAndReturn(player1, new ImmersturmBattlefield());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(realm),
                null, creature.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, creature));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, gd.playerHands.get(player1.getId()).indexOf(creature.getCard()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.HASTE)).isFalse();
    }

    @Test
    void hostsMultipleCreaturesAndBoostsOnlyHostedCreatures() {
        Permanent realm = harness.addToBattlefieldAndReturn(player1, new ImmersturmBattlefield());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent unhostedCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 4);

        int realmIndex = gd.playerBattlefields.get(player1.getId()).indexOf(realm);
        harness.activateAbility(player1, realmIndex, null, ownCreature.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, realmIndex, null, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, unhostedCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, unhostedCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    void hostAbilityRequiresCreatureTargetAndSorcerySpeed() {
        Permanent realm = harness.addToBattlefieldAndReturn(player1, new ImmersturmBattlefield());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        int realmIndex = gd.playerBattlefields.get(player1.getId()).indexOf(realm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, realmIndex, null, realm.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.UPKEEP);
        assertThatThrownBy(() -> harness.activateAbility(player1, realmIndex, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
