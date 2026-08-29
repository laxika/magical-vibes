package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HiddenGibbonsTest extends BaseCardTest {

    private Permanent addHiddenGibbons() {
        return harness.addToBattlefieldAndReturn(player1, new HiddenGibbons());
    }

    private void prepareOpponentCast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("An opponent's instant makes Hidden Gibbons a 4/4 Ape creature")
    void becomesApeCreatureWhenOpponentCastsInstant() {
        Permanent gibbons = addHiddenGibbons();
        prepareOpponentCast();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, gibbons)).isTrue();
        assertThat(gqs.isEnchantment(gd, gibbons)).isFalse();
        assertThat(gqs.getEffectivePower(gd, gibbons)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gibbons)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, gibbons)).containsExactly(CardSubtype.APE);
    }

    @Test
    @DisplayName("A non-instant spell does not trigger Hidden Gibbons")
    void doesNotTriggerForNonInstantSpell() {
        Permanent gibbons = addHiddenGibbons();
        prepareOpponentCast();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isEnchantment(gd, gibbons)).isTrue();
        assertThat(gqs.isCreature(gd, gibbons)).isFalse();
    }

    @Test
    @DisplayName("Hidden Gibbons does not trigger after becoming a creature")
    void doesNotTriggerWhenAlreadyCreature() {
        Permanent gibbons = addHiddenGibbons();
        prepareOpponentCast();

        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isCreature(gd, gibbons)).isTrue();
        assertThat(gqs.isEnchantment(gd, gibbons)).isFalse();
    }
}
