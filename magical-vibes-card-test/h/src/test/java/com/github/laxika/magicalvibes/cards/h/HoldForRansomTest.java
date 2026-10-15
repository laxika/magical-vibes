package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BrokersVeteran;
import com.github.laxika.magicalvibes.cards.b.BrokenWings;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.o.OminousParcel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HoldForRansom.class, BrokersVeteran.class, OminousParcel.class, Mountain.class, BrokenWings.class})
class HoldForRansomTest extends BaseCardTest {

    private Permanent attachTo(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new HoldForRansom());
        aura.setAttachedTo(creature.getId());
        return aura;
    }

    @Test
    void enchantedCreatureCannotAttack() {
        Permanent creature = addCreatureReady(player2, new BrokersVeteran());
        attachTo(creature);

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void enchantedCreatureCannotBlock() {
        Permanent creature = addCreatureReady(player2, new BrokersVeteran());
        attachTo(creature);
        Permanent attacker = addCreatureReady(player1, new BrokersVeteran());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    void creatureControllerCanPayToSacrificeAuraAndAuraControllerDraws() {
        Permanent creature = addCreatureReady(player2, new BrokersVeteran());
        attachTo(creature);
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setLibrary(player2, List.of(new Mountain()));
        harness.addMana(player2, ManaColor.COLORLESS, 7);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int auraControllerHandBefore = gd.playerHands.get(player1.getId()).size();
        int creatureControllerHandBefore = gd.playerHands.get(player2.getId()).size();
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hold for Ransom");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(auraControllerHandBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(creatureControllerHandBefore);
    }

    @Test
    void ransomAbilityCanOnlyBeActivatedAtSorcerySpeed() {
        addCreatureReady(player2, new BrokersVeteran());
        attachTo(gd.playerBattlefields.get(player2.getId()).getFirst());
        harness.addMana(player2, ManaColor.COLORLESS, 7);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery");
    }

    @Test
    void cannotEnchantNoncreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new OminousParcel());
        harness.setHand(player1, List.of(new HoldForRansom()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void auraControllerStillDrawsWhenAuraIsDestroyedInResponse() {
        Permanent creature = addCreatureReady(player2, new BrokersVeteran());
        Permanent aura = attachTo(creature);
        harness.setHand(player1, List.of(new BrokenWings()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, null);
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, aura.getId());
        harness.assertInGraveyard(player1, "Hold for Ransom");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Mountain");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    void cannotActivateRansomDuringAnotherPlayersMainPhase() {
        Permanent creature = addCreatureReady(player2, new BrokersVeteran());
        attachTo(creature);
        harness.addMana(player2, ManaColor.COLORLESS, 7);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery");
    }

    @Test
    void cannotActivateRansomWhileAnotherRansomAbilityIsOnStack() {
        Permanent creature = addCreatureReady(player2, new BrokersVeteran());
        attachTo(creature);
        harness.addMana(player2, ManaColor.COLORLESS, 14);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
    }

    @Test
    void canCastAuraAndActivateRansomOnOwnSummoningSickCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BrokersVeteran());
        harness.setHand(player1, List.of(new HoldForRansom()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Hold for Ransom");
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hold for Ransom");
        harness.assertInHand(player1, "Mountain");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }
}
