package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.r.Rescind;
import com.github.laxika.magicalvibes.cards.t.Telepathy;
import com.github.laxika.magicalvibes.cards.v.VoltaicKey;
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

@CardUsed({HiddenGuerrillas.class, Rescind.class, Telepathy.class, VoltaicKey.class})
class HiddenGuerrillasTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's artifact spell makes Hidden Guerrillas a 5/3 Soldier creature with trample")
    void becomesSoldierCreatureWhenOpponentCastsArtifact() {
        Permanent hiddenGuerrillas = harness.addToBattlefieldAndReturn(player1, new HiddenGuerrillas());
        prepareOpponentCast();
        harness.castFromHand(player2, new VoltaicKey(), "{1}");
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, hiddenGuerrillas)).isTrue();
        assertThat(gqs.isEnchantment(gd, hiddenGuerrillas)).isFalse();
        assertThat(gqs.getEffectivePower(gd, hiddenGuerrillas)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, hiddenGuerrillas)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hiddenGuerrillas)).containsExactly(CardSubtype.SOLDIER);
        assertThat(gqs.hasKeyword(gd, hiddenGuerrillas, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("A non-artifact spell does not trigger Hidden Guerrillas")
    void doesNotTriggerForNonArtifactSpell() {
        Permanent hiddenGuerrillas = harness.addToBattlefieldAndReturn(player1, new HiddenGuerrillas());
        prepareOpponentCast();
        harness.castFromHand(player2, new Telepathy(), "{U}");
        harness.passBothPriorities();

        assertThat(gqs.isEnchantment(gd, hiddenGuerrillas)).isTrue();
        assertThat(gqs.isCreature(gd, hiddenGuerrillas)).isFalse();
    }

    @Test
    @DisplayName("The ability does not trigger when its controller casts an artifact spell")
    void doesNotTriggerForControllerArtifactSpell() {
        Permanent hiddenGuerrillas = harness.addToBattlefieldAndReturn(player1, new HiddenGuerrillas());
        harness.castFromHand(player1, new VoltaicKey(), "{1}");
        harness.passBothPriorities();

        assertThat(gqs.isEnchantment(gd, hiddenGuerrillas)).isTrue();
        assertThat(gqs.isCreature(gd, hiddenGuerrillas)).isFalse();
    }

    @Test
    @DisplayName("Hidden Guerrillas does not trigger again after becoming a creature")
    void doesNotTriggerAfterBecomingCreature() {
        Permanent hiddenGuerrillas = harness.addToBattlefieldAndReturn(player1, new HiddenGuerrillas());
        prepareOpponentCast();
        harness.castFromHand(player2, new VoltaicKey(), "{1}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, hiddenGuerrillas)).isTrue();

        harness.castFromHand(player2, new VoltaicKey(), "{1}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isCreature(gd, hiddenGuerrillas)).isTrue();
    }

    @Test
    @DisplayName("A queued trigger does nothing if Hidden Guerrillas leaves before it resolves")
    void checksEnchantmentConditionAgainAtResolution() {
        Permanent hiddenGuerrillas = harness.addToBattlefieldAndReturn(player1, new HiddenGuerrillas());
        prepareOpponentCast();

        harness.castFromHand(player2, new VoltaicKey(), "{1}");
        harness.setHand(player1, List.of(new Rescind()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, hiddenGuerrillas.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Hidden Guerrillas");
        harness.assertInHand(player1, "Hidden Guerrillas");
    }

    @Test
    @DisplayName("The transformation resolves before the artifact spell enters the battlefield")
    void transformsBeforeArtifactResolves() {
        Permanent hiddenGuerrillas = harness.addToBattlefieldAndReturn(player1, new HiddenGuerrillas());
        prepareOpponentCast();
        harness.castFromHand(player2, new VoltaicKey(), "{1}");

        assertThat(gd.stack).hasSize(2);
        harness.assertNotOnBattlefield(player2, "Voltaic Key");
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, hiddenGuerrillas)).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Voltaic Key");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Voltaic Key");
    }

    @Test
    @DisplayName("Returning and recasting the animated card restores its enchantment form")
    void recastingRestoresEnchantmentAndCanTriggerAgain() {
        Permanent hiddenGuerrillas = harness.addToBattlefieldAndReturn(player1, new HiddenGuerrillas());
        prepareOpponentCast();
        harness.castFromHand(player2, new VoltaicKey(), "{1}");
        resolveAllTriggers();

        harness.setHand(player1, List.of(new Rescind()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, hiddenGuerrillas.getId());
        harness.assertInHand(player1, "Hidden Guerrillas");

        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0);
        resolveAllTriggers();
        Permanent recast = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hidden Guerrillas"))
                .findFirst().orElseThrow();
        assertThat(gqs.isEnchantment(gd, recast)).isTrue();
        assertThat(gqs.isCreature(gd, recast)).isFalse();

        prepareOpponentCast();
        harness.castFromHand(player2, new VoltaicKey(), "{1}");
        resolveAllTriggers();
        assertThat(gqs.isCreature(gd, recast)).isTrue();
        assertThat(gqs.hasKeyword(gd, recast, Keyword.TRAMPLE)).isTrue();
    }

    private void prepareOpponentCast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

}
