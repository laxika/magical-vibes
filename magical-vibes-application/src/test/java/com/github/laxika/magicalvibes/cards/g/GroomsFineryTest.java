package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BridesGown;
import com.github.laxika.magicalvibes.cards.d.DawnhartDisciple;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GroomsFinery.class, DawnhartDisciple.class, BridesGown.class})
class GroomsFineryTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsPlusTwoPlusZero() {
        Permanent creature = addCreatureReady(player1, new DawnhartDisciple());
        Permanent finery = addFineryReady(player1);
        finery.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void brideGownAttachedToCreatureYouControlAddsToughnessAndDeathtouch() {
        Permanent creature = addCreatureReady(player1, new DawnhartDisciple());
        Permanent finery = addFineryReady(player1);
        finery.setAttachedTo(creature.getId());

        Permanent gown = harness.addToBattlefieldAndReturn(player2, new BridesGown());
        gown.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();
    }

    @Test
    void brideGownAttachedToOpponentCreatureDoesNotEnableBonus() {
        Permanent creature = addCreatureReady(player1, new DawnhartDisciple());
        Permanent finery = addFineryReady(player1);
        finery.setAttachedTo(creature.getId());

        Permanent opponentCreature = addCreatureReady(player2, new DawnhartDisciple());
        Permanent gown = harness.addToBattlefieldAndReturn(player2, new BridesGown());
        gown.setAttachedTo(opponentCreature.getId());

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void differentlyNamedEquipmentDoesNotEnableBonus() {
        Permanent creature = addCreatureReady(player1, new DawnhartDisciple());
        Permanent finery = addFineryReady(player1);
        finery.setAttachedTo(creature.getId());

        Permanent otherEquipment = harness.addToBattlefieldAndReturn(player1, new GroomsFinery());
        otherEquipment.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void equipAbilityAttachesToCreatureYouControl() {
        Permanent finery = addFineryReady(player1);
        Permanent creature = addCreatureReady(player1, new DawnhartDisciple());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(finery.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void gownOnAnotherControlledCreatureEnablesBonusOnlyWhileAttached() {
        Permanent creature = addCreatureReady(player1, new DawnhartDisciple());
        Permanent otherCreature = addCreatureReady(player1, new DawnhartDisciple());
        Permanent finery = addFineryReady(player1);
        finery.setAttachedTo(creature.getId());
        Permanent gown = harness.addToBattlefieldAndReturn(player1, new BridesGown());

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();

        gown.setAttachedTo(otherCreature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();

        gown.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void bonusUsesFineryControllerEvenWhenEquippedCreatureChangesControl() {
        Permanent creature = addCreatureReady(player1, new DawnhartDisciple());
        Permanent otherCreature = addCreatureReady(player1, new DawnhartDisciple());
        Permanent finery = addFineryReady(player1);
        finery.setAttachedTo(creature.getId());
        Permanent gown = harness.addToBattlefieldAndReturn(player2, new BridesGown());
        gown.setAttachedTo(otherCreature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerBattlefields.get(player2.getId()).add(creature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(otherCreature);
        gd.playerBattlefields.get(player2.getId()).add(otherCreature);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.DEATHTOUCH)).isFalse();
    }

    @Test
    void reequippingMovesPowerBonusToNewCreature() {
        Permanent finery = addFineryReady(player1);
        Permanent firstCreature = addCreatureReady(player1, new DawnhartDisciple());
        Permanent secondCreature = addCreatureReady(player1, new DawnhartDisciple());
        finery.setAttachedTo(firstCreature.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(finery.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(4);
    }

    private Permanent addFineryReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GroomsFinery());
    }
}
