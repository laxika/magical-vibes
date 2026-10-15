package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.k.KithkinHealer;
import com.github.laxika.magicalvibes.cards.d.DiregrafGhoul;
import com.github.laxika.magicalvibes.cards.s.SecludedGlen;
import com.github.laxika.magicalvibes.cards.w.WizenedCenn;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AmoeboidChangeling.class, WizenedCenn.class, Aethersnipe.class, KithkinHealer.class, SecludedGlen.class})
class AmoeboidChangelingTest extends BaseCardTest {

    /** Adds Amoeboid Changeling at battlefield index 0, ready to tap. */
    private void addAmoeboidReady() {
        addCreatureReady(player1, new AmoeboidChangeling());
    }

    private Permanent find(String name) {
        return findPermanent(player1, name);
    }

    @Test
    @DisplayName("Ability 1 makes a non-Kithkin count as a Kithkin, so Wizened Cenn buffs it")
    void gainAllCreatureTypesTriggersTribalBuff() {
        addAmoeboidReady();
        harness.addToBattlefield(player1, new WizenedCenn());
        harness.addToBattlefield(player1, new Aethersnipe());

        Permanent aethersnipe = find("Aethersnipe");
        assertThat(gqs.getEffectivePower(gd, aethersnipe)).isEqualTo(4); // not a Kithkin yet

        UUID targetId = harness.getPermanentId(player1, "Aethersnipe");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aethersnipe)).isEqualTo(5); // now every creature type incl. Kithkin
        assertThat(gqs.getEffectiveToughness(gd, aethersnipe)).isEqualTo(5);
    }

    @Test
    @DisplayName("Ability 1 can target an opponent's creature")
    void gainAllCreatureTypesCanTargetOpponentCreature() {
        addAmoeboidReady();
        harness.addToBattlefield(player2, new WizenedCenn());
        harness.addToBattlefield(player2, new Aethersnipe());

        Permanent aethersnipe = findPermanent(player2, "Aethersnipe");
        assertThat(gqs.getEffectivePower(gd, aethersnipe)).isEqualTo(4); // not a Kithkin yet

        UUID targetId = harness.getPermanentId(player2, "Aethersnipe");
        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, aethersnipe)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, aethersnipe)).isEqualTo(5);
    }

    @Test
    @DisplayName("Ability 2 strips a base Kithkin's creature types, removing Wizened Cenn's buff")
    void loseAllCreatureTypesRemovesTribalBuff() {
        addAmoeboidReady();
        harness.addToBattlefield(player1, new WizenedCenn());
        harness.addToBattlefield(player1, new KithkinHealer());

        Permanent kithkin = find("Kithkin Healer");
        assertThat(gqs.getEffectivePower(gd, kithkin)).isEqualTo(3); // 2/2 + Wizened Cenn

        UUID targetId = harness.getPermanentId(player1, "Kithkin Healer");
        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, kithkin)).isEqualTo(2); // no longer a Kithkin
        assertThat(gqs.getEffectiveToughness(gd, kithkin)).isEqualTo(2);
    }

    @Test
    @DisplayName("Lost creature types return at end of turn")
    void loseAllCreatureTypesWearsOff() {
        addAmoeboidReady();
        harness.addToBattlefield(player1, new WizenedCenn());
        harness.addToBattlefield(player1, new KithkinHealer());

        Permanent kithkin = find("Kithkin Healer");
        UUID targetId = harness.getPermanentId(player1, "Kithkin Healer");
        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, kithkin)).isEqualTo(2);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, kithkin)).isEqualTo(3); // Kithkin again
    }

    @Test
    @DisplayName("Abilities can only target creatures")
    void cannotTargetNonCreature() {
        addAmoeboidReady();
        harness.addToBattlefield(player1, new SecludedGlen());

        UUID targetId = harness.getPermanentId(player1, "Secluded Glen");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
    @Test
    @DisplayName("Gained creature types expire during turn cleanup")
    void gainedTypesExpireDuringCleanup() {
        addAmoeboidReady();
        harness.addToBattlefield(player1, new WizenedCenn());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Aethersnipe());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("A later gain of all creature types restores tribal bonuses after type loss")
    void laterTypeGainOverridesEarlierTypeLoss() {
        addAmoeboidReady();
        addCreatureReady(player1, new AmoeboidChangeling());
        harness.addToBattlefield(player1, new WizenedCenn());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Aethersnipe());

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);

        harness.activateAbility(player1, 1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(5);
    }

    @Test
    @DisplayName("A later loss of all creature types overrides an earlier gain")
    void laterTypeLossOverridesEarlierTypeGain() {
        addAmoeboidReady();
        addCreatureReady(player1, new AmoeboidChangeling());
        harness.addToBattlefield(player1, new WizenedCenn());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Aethersnipe());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);

        harness.activateAbility(player1, 1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gaining every creature type does not grant the changeling ability")
    void gainingTypesDoesNotGrantChangeling() {
        addAmoeboidReady();
        harness.addToBattlefield(player1, new WizenedCenn());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Aethersnipe());

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, target, Keyword.CHANGELING)).isFalse();
    }

    @Test
    @DisplayName("Losing every creature type removes tribal bonuses but leaves changeling intact")
    void losingTypesDoesNotRemoveChangeling() {
        Permanent target = addCreatureReady(player1, new AmoeboidChangeling());
        harness.addToBattlefield(player1, new WizenedCenn());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.CHANGELING)).isTrue();
    }

    @Test
    @DisplayName("Type loss can target an opponent's creature")
    void typeLossCanTargetOpponentCreature() {
        addAmoeboidReady();
        harness.addToBattlefield(player2, new WizenedCenn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KithkinHealer());
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("Both tap abilities are unavailable while summoning sick")
    void summoningSicknessPreventsBothAbilities() {
        harness.addToBattlefield(player1, new AmoeboidChangeling());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Aethersnipe());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activating either ability pays the tap cost immediately")
    void activationTapsSourceAndPreventsAnotherActivation() {
        for (int abilityIndex : List.of(0, 1)) {
            Permanent source = addCreatureReady(player1, new AmoeboidChangeling());
            int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(source);
            harness.activateAbility(player1, sourceIndex, abilityIndex, null, source.getId());

            assertThat(source.isTapped()).isTrue();
            int otherAbilityIndex = 1 - abilityIndex;
            assertThatThrownBy(() -> harness.activateAbility(player1, sourceIndex, otherAbilityIndex, null, source.getId()))
                    .isInstanceOf(IllegalStateException.class);
            harness.passBothPriorities();
        }
    }

    @Test
    @CardUsed({ArchghoulOfThraben.class, DiregrafGhoul.class})
    @DisplayName("A Zombie that loses every creature type does not trigger Zombie death abilities")
    void lostPrintedZombieTypeStaysAbsentForDeathTriggers() {
        assertTypeLossSuppressesZombieDeathTrigger(new DiregrafGhoul());
    }

    @Test
    @CardUsed(ArchghoulOfThraben.class)
    @DisplayName("A changeling that loses every creature type does not trigger Zombie death abilities")
    void lostChangelingTypesStayAbsentForDeathTriggers() {
        assertTypeLossSuppressesZombieDeathTrigger(new AmoeboidChangeling());
    }

    private void assertTypeLossSuppressesZombieDeathTrigger(Card creature) {
        addAmoeboidReady();
        harness.addToBattlefield(player1, new ArchghoulOfThraben());
        Permanent target = harness.addToBattlefieldAndReturn(player1, creature);
        Card topZombie = new ArchghoulOfThraben();
        harness.setLibrary(player1, List.of(topZombie));

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topZombie);
        harness.assertInGraveyard(player1, creature.getName());
    }

    @Test
    @CardUsed(ArchghoulOfThraben.class)
    @DisplayName("A creature that gains every creature type triggers Zombie death abilities")
    void gainedZombieTypeIsRememberedForDeathTriggers() {
        addAmoeboidReady();
        harness.addToBattlefield(player1, new ArchghoulOfThraben());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new Aethersnipe());
        Card topZombie = new ArchghoulOfThraben();
        harness.setLibrary(player1, List.of(topZombie));

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, target));

        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(topZombie);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
}
