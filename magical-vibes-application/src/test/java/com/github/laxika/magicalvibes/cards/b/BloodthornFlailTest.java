package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodthornFlail.class, GrizzlyBears.class, Spellbook.class})
class BloodthornFlailTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsPlusTwoPlusOne() {
        Permanent creature = addCreatureReady(player1);
        Permanent flail = addFlailReady(player1);
        flail.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    void equipCanBePaidWithThreeMana() {
        Permanent flail = addFlailReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(flail.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void equipCanBePaidByDiscardingACard() {
        Permanent flail = addFlailReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.setHand(player1, List.of(new Spellbook()));

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(flail.getAttachedTo()).isEqualTo(creature.getId());
        harness.assertInGraveyard(player1, "Spellbook");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void equipCannotTargetAnOpponentsCreature(int abilityIndex) {
        Permanent flail = addFlailReady(player1);
        Permanent creature = addCreatureReady(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new Spellbook()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(flail.getAttachedTo()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void equipCannotBeActivatedOutsideAMainPhase(int abilityIndex) {
        Permanent flail = addFlailReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setHand(player1, List.of(new Spellbook()));
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(flail.getAttachedTo()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void discardEquipRequiresACardInHand() {
        Permanent flail = addFlailReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(flail.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void discardCostIsPaidEvenWhenTargetLeavesBeforeResolution() {
        Permanent flail = addFlailReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.setHand(player1, List.of(new Spellbook()));

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Spellbook");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(flail.getAttachedTo()).isNull();
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(flail.getAttachedTo()).isNull();
        harness.assertInGraveyard(player1, "Spellbook");
    }

    private Permanent addFlailReady(Player player) {
        return addCreatureReady(player, new BloodthornFlail());
    }

    private Permanent addCreatureReady(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
