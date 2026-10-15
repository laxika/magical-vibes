package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.t.TravelingMinister;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CeremonialKnife.class, GrizzlyBears.class, SerraAngel.class, TravelingMinister.class})
class CeremonialKnifeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipping Ceremonial Knife gives the creature +1/+0")
    void equippingGivesPowerBoost() {
        Permanent knife = addKnifeReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(knife.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature creates a Blood token when it deals combat damage to a player")
    void createsBloodTokenOnCombatDamageToPlayer() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent knife = addKnifeReady(player1);
        knife.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Blood")).hasSize(1);
    }

    @Test
    @DisplayName("Equipped creature creates a Blood token when it deals combat damage to a creature")
    void createsBloodTokenOnCombatDamageToCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent knife = addKnifeReady(player1);
        knife.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new SerraAngel());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Blood")).hasSize(1);
    }

    @Test
    void creatureControllerCreatesBloodWhenOpponentControlsKnife() {
        Permanent creature = addCreatureReady(player1, new TravelingMinister());
        Permanent knife = addKnifeReady(player2);
        knife.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Blood")).hasSize(1);
        assertThat(findPermanents(player2, "Blood")).isEmpty();
    }

    @Test
    void blockingCreatureCreatesBloodEvenWhenBothCreaturesDie() {
        Permanent attacker = addCreatureReady(player1, new TravelingMinister());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new TravelingMinister());
        Permanent knife = addKnifeReady(player2);
        knife.setAttachedTo(blocker.getId());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(findPermanents(player2, "Blood")).hasSize(1);
        assertThat(findPermanents(player1, "Blood")).isEmpty();
    }

    @Test
    void unattachedKnifeDoesNotCreateBlood() {
        Permanent creature = addCreatureReady(player1, new TravelingMinister());
        addKnifeReady(player1);
        creature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Blood")).isEmpty();
    }

    @Test
    void reequippingMovesBonusAndCombatDamageAbility() {
        Permanent knife = addKnifeReady(player1);
        Permanent oldCreature = addCreatureReady(player1, new TravelingMinister());
        Permanent newCreature = addCreatureReady(player1, new TravelingMinister());
        knife.setAttachedTo(oldCreature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, newCreature.getId());
        harness.passBothPriorities();

        assertThat(knife.getAttachedTo()).isEqualTo(newCreature.getId());
        assertThat(gqs.getEffectivePower(gd, oldCreature)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, newCreature)).isEqualTo(2);
        oldCreature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Blood")).isEmpty();
    }

    @Test
    void equipCannotTargetOpponentCreature() {
        Permanent knife = addKnifeReady(player1);
        Permanent creature = addCreatureReady(player2, new TravelingMinister());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(knife.getAttachedTo()).isNull();
    }

    @Test
    void equipRequiresTwoMana() {
        Permanent knife = addKnifeReady(player1);
        Permanent creature = addCreatureReady(player1, new TravelingMinister());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(knife.getAttachedTo()).isNull();
    }

    @Test
    void equipCannotBeActivatedDuringCombat() {
        Permanent knife = addKnifeReady(player1);
        Permanent creature = addCreatureReady(player1, new TravelingMinister());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(knife.getAttachedTo()).isNull();
    }

    @Test
    void createdBloodTokenDiscardsAndSacrificesToDraw() {
        Permanent creature = addCreatureReady(player1, new TravelingMinister());
        Permanent knife = addKnifeReady(player1);
        knife.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();
        Permanent blood = findPermanent(player1, "Blood");
        Card discarded = new TravelingMinister();
        Card drawn = new CeremonialKnife();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(blood), null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(blood);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    private Permanent addKnifeReady(Player player) {
        return addCreatureReady(player, new CeremonialKnife());
    }
}
