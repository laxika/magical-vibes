package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.r.RoyalAssassin;
import com.github.laxika.magicalvibes.cards.s.SenuKeenEyedProtector;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MjLnirStormHammer.class, RoyalAssassin.class, SenuKeenEyedProtector.class})
class MjLnirStormHammerTest extends BaseCardTest {

    @Test
    @DisplayName("Mjölnir enters attached to a target legendary creature you control")
    void entersAttachedToLegendaryCreature() {
        Permanent legendary = addCreatureReady(player1, new SenuKeenEyedProtector());
        harness.setHand(player1, List.of(new MjLnirStormHammer()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0, legendary.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent hammer = findPermanent(player1, "Mjölnir, Storm Hammer");
        assertThat(hammer.getAttachedTo()).isEqualTo(legendary.getId());
    }

    @Test
    @DisplayName("ETB attachment rejects a nonlegendary creature")
    void entersCannotTargetNonlegendaryCreature() {
        Permanent creature = addCreatureReady(player1, new RoyalAssassin());
        harness.setHand(player1, List.of(new MjLnirStormHammer()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
    }

    @Test
    @DisplayName("Equipped creature attacks by tapping and stunning a defending creature, then damages each opponent for their tapped creatures")
    void attacksTapStunAndDamageForTappedCreatures() {
        Permanent attacker = addCreatureReady(player1, new SenuKeenEyedProtector());
        Permanent hammer = harness.addToBattlefieldAndReturn(player1, new MjLnirStormHammer());
        hammer.setAttachedTo(attacker.getId());
        Permanent alreadyTapped = addCreatureReady(player2, new RoyalAssassin());
        alreadyTapped.tap();
        Permanent target = addCreatureReady(player2, new RoyalAssassin());

        declareAttackers(player1, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPermanentIds()).containsExactlyInAnyOrder(alreadyTapped.getId(), target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Equip {4} can attach Mjölnir to any creature you control")
    void equipAttachesToCreatureYouControl() {
        Permanent hammer = harness.addToBattlefieldAndReturn(player1, new MjLnirStormHammer());
        Permanent creature = addCreatureReady(player1, new RoyalAssassin());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(hammer.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void entersWithoutLegendaryCreatureRemainsUnattached() {
        harness.setHand(player1, List.of(new MjLnirStormHammer()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Mjölnir, Storm Hammer").getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void entersCannotTargetOpponentsLegendaryCreature() {
        Permanent legendary = addCreatureReady(player2, new SenuKeenEyedProtector());
        harness.setHand(player1, List.of(new MjLnirStormHammer()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, legendary.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void alreadyTappedTargetGetsAnotherStunCounterAndCountsOnce() {
        Permanent attacker = addCreatureReady(player1, new SenuKeenEyedProtector());
        Permanent hammer = harness.addToBattlefieldAndReturn(player1, new MjLnirStormHammer());
        hammer.setAttachedTo(attacker.getId());
        Permanent target = addCreatureReady(player2, new RoyalAssassin());
        target.tap();
        target.setCounterCount(CounterType.STUN, 1);
        Permanent untapped = addCreatureReady(player2, new RoyalAssassin());
        Permanent tappedEquipment = harness.addToBattlefieldAndReturn(player2, new MjLnirStormHammer());
        tappedEquipment.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(2);
        assertThat(untapped.isTapped()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void illegalAttackTargetPreventsAllDamage() {
        Permanent attacker = addCreatureReady(player1, new SenuKeenEyedProtector());
        Permanent hammer = harness.addToBattlefieldAndReturn(player1, new MjLnirStormHammer());
        hammer.setAttachedTo(attacker.getId());
        Permanent target = addCreatureReady(player2, new RoyalAssassin());
        Permanent tapped = addCreatureReady(player2, new RoyalAssassin());
        tapped.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(tapped.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    void targetLeavingDefendersControlPreventsAllEffects() {
        Permanent attacker = addCreatureReady(player1, new SenuKeenEyedProtector());
        Permanent hammer = harness.addToBattlefieldAndReturn(player1, new MjLnirStormHammer());
        hammer.setAttachedTo(attacker.getId());
        Permanent target = addCreatureReady(player2, new RoyalAssassin());
        Permanent tapped = addCreatureReady(player2, new RoyalAssassin());
        tapped.tap();

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void attackTriggerStillResolvesAfterHammerLeavesBattlefield() {
        Permanent attacker = addCreatureReady(player1, new SenuKeenEyedProtector());
        Permanent hammer = harness.addToBattlefieldAndReturn(player1, new MjLnirStormHammer());
        hammer.setAttachedTo(attacker.getId());
        Permanent target = addCreatureReady(player2, new RoyalAssassin());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(hammer);
        gd.playerGraveyards.get(player1.getId()).add(hammer.getCard());
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void damageCountsTappedCreaturesAtResolution() {
        Permanent attacker = addCreatureReady(player1, new SenuKeenEyedProtector());
        Permanent hammer = harness.addToBattlefieldAndReturn(player1, new MjLnirStormHammer());
        hammer.setAttachedTo(attacker.getId());
        Permanent target = addCreatureReady(player2, new RoyalAssassin());
        Permanent other = addCreatureReady(player2, new RoyalAssassin());

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        other.tap();
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        harness.addToBattlefield(player1, new MjLnirStormHammer());
        Permanent creature = addCreatureReady(player2, new RoyalAssassin());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
