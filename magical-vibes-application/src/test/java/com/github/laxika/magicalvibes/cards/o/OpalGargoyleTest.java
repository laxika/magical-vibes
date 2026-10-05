package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.p.PouncingCheetah;
import com.github.laxika.magicalvibes.cards.v.VizierOfManyFaces;
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

@CardUsed({OpalGargoyle.class, PouncingCheetah.class, VizierOfManyFaces.class, WildDogs.class, WornPowerstone.class})
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

    @Test
    @DisplayName("A creature entering without being cast does not animate Opal Gargoyle")
    void doesNotTriggerForCreatureEnteringWithoutBeingCast() {
        Permanent opal = addOpalGargoyle();

        harness.enterBattlefieldAndReturn(player2, new WildDogs());
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.isEnchantment(gd, opal)).isTrue();
        assertThat(gqs.isCreature(gd, opal)).isFalse();
    }

    @Test
    @DisplayName("The animation resolves before the triggering creature spell")
    void animatesBeforeCreatureSpellResolves() {
        Permanent opal = addOpalGargoyle();
        prepareOpponentCast();

        castOpponentCreature();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Wild Dogs");
        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.hasKeyword(gd, opal, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The animation persists into the next turn")
    void animationPersistsIntoNextTurn() {
        Permanent opal = addOpalGargoyle();
        prepareOpponentCast();
        castOpponentCreature();
        resolveAllTriggers();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opal)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opal)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opal, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Copying an animated Opal Gargoyle copies its enchantment form")
    void copyDoesNotInheritAnimation() {
        Permanent opal = addOpalGargoyle();
        prepareOpponentCast();
        castOpponentCreature();
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new VizierOfManyFaces(), "{2}{U}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, opal.getId());

        Permanent copy = findPermanents(player1, "Opal Gargoyle").stream()
                .filter(permanent -> !permanent.getId().equals(opal.getId()))
                .findFirst().orElseThrow();
        assertThat(gqs.isEnchantment(gd, copy)).isTrue();
        assertThat(gqs.isCreature(gd, copy)).isFalse();
        assertThat(gqs.hasKeyword(gd, copy, Keyword.FLYING)).isFalse();
        assertThat(gqs.isCreature(gd, opal)).isTrue();
    }
}
