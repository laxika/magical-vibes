package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.r.Rewind;
import com.github.laxika.magicalvibes.cards.w.WildDogs;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OpalCaryatid.class, WildDogs.class, WornPowerstone.class, Rewind.class})
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

    @Test
    @DisplayName("A creature entering without being cast does not animate Opal Caryatid")
    void doesNotTriggerForCreatureEnteringWithoutCast() {
        Permanent opal = addOpalCaryatid();

        harness.enterBattlefieldAndReturn(player2, new WildDogs());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isEnchantment(gd, opal)).isTrue();
        assertThat(gqs.isCreature(gd, opal)).isFalse();
    }

    @Test
    @DisplayName("Countering the creature spell does not stop Opal Caryatid's trigger")
    void becomesCreatureEvenWhenTriggeringSpellIsCountered() {
        Permanent opal = addOpalCaryatid();
        prepareOpponentCast();
        WildDogs dogs = new WildDogs();
        harness.castFromHand(player2, dogs, "{G}");

        assertThat(gd.stack).hasSize(2);
        harness.setHand(player1, List.of(new Rewind()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.passPriority(player2);
        harness.castInstant(player1, 0, dogs.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Wild Dogs");
        assertThat(gqs.isEnchantment(gd, opal)).isTrue();
        assertThat(gqs.isCreature(gd, opal)).isFalse();

        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opal)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opal)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, opal)).containsExactly(CardSubtype.SOLDIER);
        harness.assertNotOnBattlefield(player2, "Wild Dogs");
    }
}
