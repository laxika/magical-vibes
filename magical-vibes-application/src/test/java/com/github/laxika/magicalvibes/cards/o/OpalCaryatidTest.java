package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.w.WildDogs;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OpalCaryatid.class, WildDogs.class, WornPowerstone.class})
class OpalCaryatidTest extends BaseCardTest {

    private Permanent addOpalCaryatid() {
        return harness.addToBattlefieldAndReturn(player1, new OpalCaryatid());
    }

    private void prepareOpponentCast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("An opponent's creature spell makes Opal Caryatid a 2/2 Soldier creature")
    void becomesSoldierCreatureWhenOpponentCastsCreature() {
        Permanent opal = addOpalCaryatid();
        prepareOpponentCast();

        harness.castFromHand(player2, new WildDogs(), "{G}");
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opal)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opal)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, opal)).containsExactly(CardSubtype.SOLDIER);
    }

    @Test
    @DisplayName("A noncreature spell does not trigger Opal Caryatid")
    void doesNotTriggerForNoncreatureSpell() {
        Permanent opal = addOpalCaryatid();
        prepareOpponentCast();

        harness.castFromHand(player2, new WornPowerstone(), "{3}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isEnchantment(gd, opal)).isTrue();
        assertThat(gqs.isCreature(gd, opal)).isFalse();
    }

    @Test
    @DisplayName("Opal Caryatid does not trigger for its controller's creature spell")
    void doesNotTriggerForControllerCreatureSpell() {
        Permanent opal = addOpalCaryatid();

        harness.castFromHand(player1, new WildDogs(), "{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isEnchantment(gd, opal)).isTrue();
        assertThat(gqs.isCreature(gd, opal)).isFalse();
    }

    @Test
    @DisplayName("A transformed Opal Caryatid does not trigger for later creature spells")
    void doesNotTriggerAfterBecomingCreature() {
        Permanent opal = addOpalCaryatid();
        prepareOpponentCast();

        harness.castFromHand(player2, new WildDogs(), "{G}");
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();

        harness.castFromHand(player2, new WildDogs(), "{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, opal)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opal)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, opal)).containsExactly(CardSubtype.SOLDIER);
    }
}
