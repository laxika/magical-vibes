package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.ExquisiteFirecraft;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.y.YomijiWhoBarsTheWay;
import com.github.laxika.magicalvibes.cards.a.ArniBrokenbrow;
import com.github.laxika.magicalvibes.cards.a.AxgardCavalry;
import com.github.laxika.magicalvibes.cards.d.DemonBolt;
import com.github.laxika.magicalvibes.cards.n.NikoAris;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ToralfGodOfFury.class, ExquisiteFirecraft.class, HillGiant.class, YomijiWhoBarsTheWay.class,
        ArniBrokenbrow.class, AxgardCavalry.class, DemonBolt.class, NikoAris.class,
        ToskiBearerOfSecrets.class})
class ToralfGodOfFuryTest extends BaseCardTest {

    @Test
    void dealsExcessNoncombatDamageToAnotherTarget() {
        harness.addToBattlefield(player1, new ToralfGodOfFury());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ExquisiteFirecraft()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void hammerDealsDamageReturnsToHandAndOnlyBoostsLegendaryCreature() {
        Permanent legendaryCreature = harness.addToBattlefieldAndReturn(player1, new YomijiWhoBarsTheWay());
        Permanent nonlegendaryCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        legendaryCreature.setSummoningSick(false);
        int legendaryPowerBefore = gqs.getEffectivePower(gd, legendaryCreature);
        int nonlegendaryPowerBefore = gqs.getEffectivePower(gd, nonlegendaryCreature);

        ToralfGodOfFury toralf = new ToralfGodOfFury();
        harness.setHand(player1, List.of(toralf));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();

        Permanent hammer = findPermanent(player1, "Toralf's Hammer");
        hammer.setAttachedTo(legendaryCreature.getId());
        harness.setLife(player2, 20);

        assertThat(gqs.getEffectivePower(gd, legendaryCreature)).isEqualTo(legendaryPowerBefore + 3);
        assertThat(gqs.getEffectivePower(gd, nonlegendaryCreature)).isEqualTo(nonlegendaryPowerBefore);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(toralf.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(hammer);
    }

    @Test
    void doesNotTriggerForExcessCombatDamage() {
        harness.addToBattlefield(player1, new ToralfGodOfFury());
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new ArniBrokenbrow());
        attacker.setAttacking(true);
        harness.addToBattlefield(player2, new AxgardCavalry());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        harness.resolveCombatDamage();

        harness.assertInGraveyard(player2, "Axgard Cavalry");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.pendingInteractions).isEmpty();
    }

    @Test
    void excessDamageCanChainThroughAnotherOpponentCreature() {
        harness.addToBattlefield(player1, new ToralfGodOfFury());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new AxgardCavalry());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new AxgardCavalry());
        first.setMarkedDamage(1);
        harness.setHand(player1, List.of(new DemonBolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void triggersForExcessDamageToOpponentPlaneswalker() {
        harness.addToBattlefield(player1, new ToralfGodOfFury());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NikoAris());
        target.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new DemonBolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertInGraveyard(player2, "Niko Aris");
    }

    @Test
    void doesNotTriggerForExcessDamageToOwnCreature() {
        harness.addToBattlefield(player1, new ToralfGodOfFury());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AxgardCavalry());
        harness.setHand(player1, List.of(new DemonBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player1, "Axgard Cavalry");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void exactlyLethalDamageDoesNotTrigger() {
        harness.addToBattlefield(player1, new ToralfGodOfFury());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ToralfGodOfFury());
        harness.setHand(player1, List.of(new DemonBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInGraveyard(player2, "Toralf, God of Fury");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void indestructibleCreatureStillTakesExcessDamageButCannotBeChosenForThatTrigger() {
        harness.addToBattlefield(player1, new ToralfGodOfFury());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ToskiBearerOfSecrets());
        harness.setHand(player1, List.of(new DemonBolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Toski, Bearer of Secrets");
        assertThat(target.getMarkedDamage()).isEqualTo(4);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
    }

    @Test
    void excessDamageTriggerStillResolvesAfterToralfLeavesBattlefield() {
        Permanent toralf = harness.addToBattlefieldAndReturn(player1, new ToralfGodOfFury());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AxgardCavalry());
        harness.setHand(player1, List.of(new DemonBolt()));
        harness.setHand(player2, List.of(new DemonBolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player2, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.castAndResolveInstant(player2, 0, toralf.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Toralf, God of Fury");
        harness.assertLife(player2, 18);
    }

    @Test
    void hammerEquipsNonlegendaryCreatureWithoutBoostAndUnattachesAsCost() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AxgardCavalry());
        creature.setSummoningSick(false);
        int powerBefore = gqs.getEffectivePower(gd, creature);
        harness.setHand(player1, List.of(new ToralfGodOfFury()));
        harness.addMana(player1, ManaColor.RED, 6);
        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();
        Permanent hammer = findPermanent(player1, "Toralf's Hammer");
        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();
        assertThat(hammer.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(powerBefore);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(hammer.getAttachedTo()).isNull();
        assertThat(creature.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Toralf's Hammer");
        harness.assertLife(player2, 20);
        harness.passBothPriorities();
        harness.assertLife(player2, 17);
        harness.assertInHand(player1, "Toralf, God of Fury");
        harness.assertNotOnBattlefield(player1, "Toralf's Hammer");
    }

    @Test
    void illegalHammerTargetPreventsDamageAndReturnButDoesNotReattach() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArniBrokenbrow());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AxgardCavalry());
        harness.setHand(player1, List.of(new ToralfGodOfFury(), new DemonBolt()));
        harness.addMana(player1, ManaColor.RED, 7);
        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();
        Permanent hammer = findPermanent(player1, "Toralf's Hammer");
        hammer.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(hammer.getAttachedTo()).isNull();
        harness.assertOnBattlefield(player1, "Toralf's Hammer");
        harness.assertNotInHand(player1, "Toralf, God of Fury");
    }
}
