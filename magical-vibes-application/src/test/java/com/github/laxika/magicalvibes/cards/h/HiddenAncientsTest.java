package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.r.Rescind;
import com.github.laxika.magicalvibes.cards.t.Telepathy;
import com.github.laxika.magicalvibes.cards.v.VoltaicKey;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HiddenAncients.class, Rescind.class, Telepathy.class, VoltaicKey.class})
class HiddenAncientsTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's enchantment spell makes Hidden Ancients a 5/5 Treefolk creature")
    void becomesTreefolkCreatureWhenOpponentCastsEnchantment() {
        Permanent hiddenAncients = harness.addToBattlefieldAndReturn(player1, new HiddenAncients());
        prepareOpponentCast();
        harness.castFromHand(player2, new Telepathy(), "{U}");
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, hiddenAncients)).isTrue();
        assertThat(gqs.isEnchantment(gd, hiddenAncients)).isFalse();
        assertThat(gqs.getEffectivePower(gd, hiddenAncients)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, hiddenAncients)).isEqualTo(5);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hiddenAncients)).containsExactly(CardSubtype.TREEFOLK);
    }

    @Test
    @DisplayName("A non-enchantment spell does not trigger Hidden Ancients")
    void doesNotTriggerForNonEnchantmentSpell() {
        Permanent hiddenAncients = harness.addToBattlefieldAndReturn(player1, new HiddenAncients());
        prepareOpponentCast();
        harness.castFromHand(player2, new VoltaicKey(), "{1}");
        harness.passBothPriorities();

        assertThat(gqs.isEnchantment(gd, hiddenAncients)).isTrue();
        assertThat(gqs.isCreature(gd, hiddenAncients)).isFalse();
    }

    @Test
    @DisplayName("The ability does not trigger when its controller casts an enchantment spell")
    void doesNotTriggerForControllerEnchantmentSpell() {
        Permanent hiddenAncients = harness.addToBattlefieldAndReturn(player1, new HiddenAncients());
        harness.castFromHand(player1, new Telepathy(), "{U}");
        harness.passBothPriorities();

        assertThat(gqs.isEnchantment(gd, hiddenAncients)).isTrue();
        assertThat(gqs.isCreature(gd, hiddenAncients)).isFalse();
    }

    @Test
    @DisplayName("Hidden Ancients does not trigger again after becoming a creature")
    void doesNotTriggerAfterBecomingCreature() {
        Permanent hiddenAncients = harness.addToBattlefieldAndReturn(player1, new HiddenAncients());
        prepareOpponentCast();
        harness.castFromHand(player2, new Telepathy(), "{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, hiddenAncients)).isTrue();

        harness.castFromHand(player2, new Telepathy(), "{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isCreature(gd, hiddenAncients)).isTrue();
    }

    @Test
    @DisplayName("A queued trigger does nothing if Hidden Ancients leaves before it resolves")
    void checksEnchantmentConditionAgainAtResolution() {
        Permanent hiddenAncients = harness.addToBattlefieldAndReturn(player1, new HiddenAncients());
        prepareOpponentCast();

        harness.castFromHand(player2, new Telepathy(), "{U}");
        harness.setHand(player1, List.of(new Rescind()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, hiddenAncients.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Hidden Ancients");
        harness.assertInHand(player1, "Hidden Ancients");
    }

    private void prepareOpponentCast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

}
