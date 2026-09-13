package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VeilOfBirds.class, DarkRitual.class})
class VeilOfBirdsTest extends BaseCardTest {

    private Permanent addVeilOfBirds() {
        return harness.addToBattlefieldAndReturn(player1, new VeilOfBirds());
    }

    private void prepareOpponentCast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Becomes a 1/1 Bird creature with flying when an opponent casts a spell")
    void becomesBirdCreatureWhenOpponentCastsSpell() {
        Permanent veil = addVeilOfBirds();
        prepareOpponentCast();

        harness.castFromHand(player2, new DarkRitual(), "{B}");
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, veil)).isTrue();
        assertThat(gqs.isEnchantment(gd, veil)).isFalse();
        assertThat(gqs.getEffectivePower(gd, veil)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, veil)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, veil)).containsExactly(CardSubtype.BIRD);
        assertThat(gqs.hasKeyword(gd, veil, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not trigger when its controller casts a spell")
    void doesNotTriggerForControllerCast() {
        Permanent veil = addVeilOfBirds();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new DarkRitual(), "{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isEnchantment(gd, veil)).isTrue();
        assertThat(gqs.isCreature(gd, veil)).isFalse();
    }

    @Test
    @DisplayName("Does not trigger again after becoming a creature")
    void doesNotTriggerAfterBecomingCreature() {
        Permanent veil = addVeilOfBirds();
        prepareOpponentCast();

        harness.castFromHand(player2, new DarkRitual(), "{B}");
        resolveAllTriggers();

        prepareOpponentCast();
        harness.castFromHand(player2, new DarkRitual(), "{B}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isCreature(gd, veil)).isTrue();
        assertThat(gqs.isEnchantment(gd, veil)).isFalse();
    }

    @Test
    @DisplayName("Only one queued trigger transforms it")
    void onlyOneQueuedTriggerTransformsIt() {
        Permanent veil = addVeilOfBirds();
        prepareOpponentCast();

        harness.castFromHand(player2, new DarkRitual(), "{B}");
        harness.castFromHand(player2, new DarkRitual(), "{B}");
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, veil)).isTrue();
        assertThat(gqs.isEnchantment(gd, veil)).isFalse();
        assertThat(gd.gameLog.stream()
                .map(entry -> entry.plainText())
                .filter(log -> log.contains("becomes a 1/1 creature")))
                .hasSize(1);
    }
}
