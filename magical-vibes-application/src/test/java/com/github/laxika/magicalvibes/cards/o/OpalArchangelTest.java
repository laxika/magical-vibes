package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.Cathodion;
import com.github.laxika.magicalvibes.cards.c.ClawsOfGix;
import com.github.laxika.magicalvibes.cards.p.PouncingCheetah;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OpalArchangel.class, Cathodion.class, ClawsOfGix.class})
class OpalArchangelTest extends BaseCardTest {

    private Permanent addOpalArchangel() {
        return harness.addToBattlefieldAndReturn(player1, new OpalArchangel());
    }

    private void prepareOpponentCast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void castOpponentCreature() {
        harness.setHand(player2, List.of(new Cathodion()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castCreature(player2, 0);
    }

    private void castOpponentFlashCreature() {
        harness.setHand(player2, List.of(new PouncingCheetah()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.castCreature(player2, 0);
    }

    @Test
    @DisplayName("An opponent's creature spell makes Opal Archangel a 5/5 Angel creature with flying and vigilance")
    void becomesAngelCreatureWhenOpponentCastsCreature() {
        Permanent opal = addOpalArchangel();
        prepareOpponentCast();

        castOpponentCreature();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opal)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, opal)).isEqualTo(5);
        assertThat(gqs.effectiveCreatureSubtypes(gd, opal)).containsExactly(CardSubtype.ANGEL);
        assertThat(gqs.hasKeyword(gd, opal, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, opal, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("The trigger does not fire after Opal Archangel has become a creature")
    void doesNotTriggerWhenAlreadyCreature() {
        Permanent opal = addOpalArchangel();
        prepareOpponentCast();

        castOpponentCreature();
        resolveAllTriggers();
        castOpponentCreature();

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
    }

    @Test
    @DisplayName("A noncreature spell does not trigger Opal Archangel")
    void doesNotTriggerForNoncreatureSpell() {
        Permanent opal = addOpalArchangel();
        prepareOpponentCast();

        harness.setHand(player2, List.of(new ClawsOfGix()));
        harness.castArtifact(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isEnchantment(gd, opal)).isTrue();
        assertThat(gqs.isCreature(gd, opal)).isFalse();
    }

    @Test
    @DisplayName("A creature spell cast by Opal Archangel's controller does not trigger it")
    void doesNotTriggerForControllerCreatureSpell() {
        Permanent opal = addOpalArchangel();

        harness.setHand(player1, List.of(new Cathodion()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isEnchantment(gd, opal)).isTrue();
        assertThat(gqs.isCreature(gd, opal)).isFalse();
    }

    @Test
    @CardUsed(PouncingCheetah.class)
    @DisplayName("A queued trigger does nothing if Opal Archangel is no longer an enchantment when it resolves")
    void queuedTriggerChecksEnchantmentAgainAtResolution() {
        Permanent opal = addOpalArchangel();
        prepareOpponentCast();

        castOpponentCreature();
        castOpponentFlashCreature();
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
        assertThat(gd.gameLog.stream()
                .map(entry -> entry.plainText())
                .filter(log -> log.contains("becomes a 5/5 creature")))
                .hasSize(1);
    }
}
