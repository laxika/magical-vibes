package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.p.PouncingCheetah;
import com.github.laxika.magicalvibes.cards.w.WildDogs;
import com.github.laxika.magicalvibes.cards.w.WornPowerstone;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OpalGargoyle.class, WildDogs.class, WornPowerstone.class})
class OpalGargoyleTest extends BaseCardTest {

    private Permanent addOpalGargoyle() {
        return harness.addToBattlefieldAndReturn(player1, new OpalGargoyle());
    }

    private void prepareOpponentCast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void castOpponentCreature() {
        harness.castFromHand(player2, new WildDogs(), "{G}");
    }

    private void castOpponentFlashCreature() {
        harness.castFromHand(player2, new PouncingCheetah(), "{2}{G}");
    }

    @Test
    @DisplayName("An opponent's creature spell makes Opal Gargoyle a 2/2 Gargoyle creature with flying")
    void becomesGargoyleCreatureWhenOpponentCastsCreature() {
        Permanent opal = addOpalGargoyle();
        prepareOpponentCast();

        castOpponentCreature();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opal)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opal)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, opal)).containsExactly(CardSubtype.GARGOYLE);
        assertThat(gqs.hasKeyword(gd, opal, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The trigger does not fire after Opal Gargoyle has become a creature")
    void doesNotTriggerWhenAlreadyCreature() {
        Permanent opal = addOpalGargoyle();
        prepareOpponentCast();

        castOpponentCreature();
        resolveAllTriggers();
        castOpponentCreature();

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
    }

    @Test
    @DisplayName("A noncreature spell does not trigger Opal Gargoyle")
    void doesNotTriggerForNoncreatureSpell() {
        Permanent opal = addOpalGargoyle();
        prepareOpponentCast();

        harness.castFromHand(player2, new WornPowerstone(), "{3}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isEnchantment(gd, opal)).isTrue();
        assertThat(gqs.isCreature(gd, opal)).isFalse();
    }

    @Test
    @DisplayName("Opal Gargoyle does not trigger for its controller's creature spell")
    void doesNotTriggerForControllerCreatureSpell() {
        Permanent opal = addOpalGargoyle();

        harness.castFromHand(player1, new WildDogs(), "{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isEnchantment(gd, opal)).isTrue();
        assertThat(gqs.isCreature(gd, opal)).isFalse();
    }

    @Test
    @CardUsed(PouncingCheetah.class)
    @DisplayName("A queued trigger does nothing once Opal Gargoyle is no longer an enchantment")
    void queuedTriggerChecksEnchantmentAgainAtResolution() {
        Permanent opal = addOpalGargoyle();
        prepareOpponentCast();

        castOpponentCreature();
        castOpponentFlashCreature();
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
        assertThat(gd.gameLog.stream()
                .map(entry -> entry.plainText())
                .filter(log -> log.contains("becomes a 2/2 creature")))
                .hasSize(1);
    }
}
