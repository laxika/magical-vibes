package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Knife.class, GrizzlyBears.class})
class KnifeTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+0 during your turn")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent knife = addCreatureReady(player1, new Knife());
        knife.setAttachedTo(creature.getId());
        harness.forceActivePlayer(player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equipped creature has first strike only during the controller's turn")
    void firstStrikeDependsOnTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent knife = addCreatureReady(player1, new Knife());
        knife.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Equipping attaches Knife to a creature you control")
    void equippingAttachesKnife() {
        Permanent knife = addCreatureReady(player1, new Knife());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(knife.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Sacrificing Knife draws a card")
    void sacrificingKnifeDrawsCard() {
        addCreatureReady(player1, new Knife());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInGraveyard(player1, "Knife");
        harness.assertNotOnBattlefield(player1, "Knife");
    }

    @Test
    @DisplayName("Equipped creature gets no boost during the opponent's turn")
    void boostDoesNotApplyDuringOpponentsTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent knife = addCreatureReady(player1, new Knife());
        knife.setAttachedTo(creature.getId());
        harness.forceActivePlayer(player2);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The equipment controller's turn determines both bonuses")
    void bonusesFollowEquipmentController() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent knife = addCreatureReady(player1, new Knife());
        knife.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Sacrifice is paid before drawing and removes the equipment's bonuses")
    void sacrificeCostRemovesBonusesBeforeResolution() {
        Permanent knife = addCreatureReady(player1, new Knife());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        knife.setAttachedTo(creature.getId());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Knife");
        harness.assertNotOnBattlefield(player1, "Knife");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }
}
