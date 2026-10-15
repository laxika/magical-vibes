package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.g.GroomsFinery;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.d.DawnhartDisciple;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BridesGown.class, DawnhartDisciple.class, GroomsFinery.class})
class BridesGownTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsPlusTwoPlusZero() {
        Permanent creature = addCreatureReady(player1, new DawnhartDisciple());
        Permanent gown = addGownReady(player1);
        gown.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void groomFineryAttachedToCreatureYouControlAddsToughnessAndFirstStrike() {
        Permanent creature = addCreatureReady(player1, new DawnhartDisciple());
        Permanent gown = addGownReady(player1);
        gown.setAttachedTo(creature.getId());

        Permanent finery = harness.addToBattlefieldAndReturn(player2, new GroomsFinery());
        finery.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void groomFineryAttachedToOpponentCreatureDoesNotEnableBonus() {
        Permanent creature = addCreatureReady(player1, new DawnhartDisciple());
        Permanent gown = addGownReady(player1);
        gown.setAttachedTo(creature.getId());

        Permanent opponentCreature = addCreatureReady(player2, new DawnhartDisciple());
        Permanent finery = harness.addToBattlefieldAndReturn(player2, new GroomsFinery());
        finery.setAttachedTo(opponentCreature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void differentlyNamedEquipmentDoesNotEnableBonus() {
        Permanent creature = addCreatureReady(player1, new DawnhartDisciple());
        Permanent gown = addGownReady(player1);
        gown.setAttachedTo(creature.getId());

        Permanent otherEquipment = harness.addToBattlefieldAndReturn(player1, new BridesGown());
        otherEquipment.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void fineryOnAnotherControlledCreatureEnablesBonusUntilDetached() {
        Permanent creature = addCreatureReady(player1, new DawnhartDisciple());
        Permanent otherCreature = addCreatureReady(player1, new DawnhartDisciple());
        Permanent gown = addGownReady(player1);
        gown.setAttachedTo(creature.getId());
        Permanent finery = harness.addToBattlefieldAndReturn(player1, new GroomsFinery());

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();

        finery.setAttachedTo(otherCreature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();

        finery.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void equipAbilityAttachesAndMovesBonusesToNewCreature() {
        Permanent gown = addGownReady(player1);
        Permanent firstCreature = addCreatureReady(player1, new DawnhartDisciple());
        Permanent secondCreature = addCreatureReady(player1, new DawnhartDisciple());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, firstCreature.getId());
        harness.passBothPriorities();

        assertThat(gown.getAttachedTo()).isEqualTo(firstCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(2);

        harness.activateAbility(player1, 0, null, secondCreature.getId());
        harness.passBothPriorities();

        assertThat(gown.getAttachedTo()).isEqualTo(secondCreature.getId());
        assertThat(gqs.getEffectivePower(gd, firstCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, secondCreature)).isEqualTo(4);
    }

    private Permanent addGownReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new BridesGown());
    }
}
