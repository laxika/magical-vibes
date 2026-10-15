package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.ChromaticStar;
import com.github.laxika.magicalvibes.model.CardColor;
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

@CardUsed({OpalGuardian.class, AshcoatBear.class, ChromaticStar.class, Cancel.class})
class OpalGuardianTest extends BaseCardTest {

    private Permanent addOpalGuardian() {
        return harness.addToBattlefieldAndReturn(player1, new OpalGuardian());
    }

    private void prepareOpponentCast() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void castOpponentCreature() {
        harness.castFromHand(player2, new AshcoatBear(), "{1}{G}");
    }

    @Test
    @DisplayName("An opponent's creature spell makes Opal Guardian a 3/4 Gargoyle with flying and protection from red")
    void becomesGargoyleWithFlyingAndProtectionFromRed() {
        Permanent opal = addOpalGuardian();
        assertThat(gqs.hasProtectionFrom(gd, opal, CardColor.RED)).isFalse();
        prepareOpponentCast();

        castOpponentCreature();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opal)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opal)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, opal)).containsExactly(CardSubtype.GARGOYLE);
        assertThat(gqs.hasKeyword(gd, opal, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, opal, CardColor.RED)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, opal, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("The trigger does not fire after Opal Guardian has become a creature")
    void doesNotTriggerWhenAlreadyCreature() {
        Permanent opal = addOpalGuardian();
        prepareOpponentCast();

        castOpponentCreature();
        resolveAllTriggers();
        castOpponentCreature();

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
    }

    @Test
    @DisplayName("A noncreature spell does not trigger Opal Guardian")
    void doesNotTriggerForNoncreatureSpell() {
        Permanent opal = addOpalGuardian();
        prepareOpponentCast();

        harness.castFromHand(player2, new ChromaticStar(), "{1}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isEnchantment(gd, opal)).isTrue();
        assertThat(gqs.isCreature(gd, opal)).isFalse();
    }

    @Test
    @DisplayName("A creature spell cast by Opal Guardian's controller does not trigger it")
    void doesNotTriggerForControllerCreatureSpell() {
        Permanent opal = addOpalGuardian();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new AshcoatBear(), "{1}{G}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.isEnchantment(gd, opal)).isTrue();
        assertThat(gqs.isCreature(gd, opal)).isFalse();
    }

    @Test
    @DisplayName("The trigger still resolves if the opponent's creature spell is countered")
    void triggersWhenOpponentCreatureSpellIsCountered() {
        Permanent opal = addOpalGuardian();
        prepareOpponentCast();

        castOpponentCreature();
        harness.setHand(player1, List.of(new Cancel()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.passPriority(player2);
        harness.castInstant(player1, 0, gd.stack.getFirst().getCard().getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
    }

    @Test
    @DisplayName("Pending triggers recheck whether Opal Guardian is still an enchantment")
    void pendingTriggersDoNotTransformItAgain() {
        Permanent opal = addOpalGuardian();
        prepareOpponentCast();

        castOpponentCreature();
        castOpponentCreature();

        assertThat(gd.stack).hasSize(4);
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
        assertThat(gd.gameLog.stream()
                .filter(entry -> entry.plainText().contains("Opal Guardian becomes a 3/4 creature.")))
                .hasSize(1);
        assertThat(countPermanents(player2, "Ashcoat Bear")).isEqualTo(2);
    }

    @Test
    @DisplayName("Each Opal Guardian transforms independently for the same creature spell")
    void transformsEachGuardianIndependently() {
        Permanent first = addOpalGuardian();
        Permanent second = addOpalGuardian();
        prepareOpponentCast();

        castOpponentCreature();
        assertThat(gd.stack).hasSize(3);
        resolveAllTriggers();

        for (Permanent opal : List.of(first, second)) {
            assertThat(gqs.isCreature(gd, opal)).isTrue();
            assertThat(gqs.isEnchantment(gd, opal)).isFalse();
            assertThat(gqs.getEffectivePower(gd, opal)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, opal)).isEqualTo(4);
            assertThat(gqs.hasKeyword(gd, opal, Keyword.FLYING)).isTrue();
            assertThat(gqs.hasProtectionFrom(gd, opal, CardColor.RED)).isTrue();
        }
    }

    @Test
    @DisplayName("The transformation and granted abilities persist into the next turn")
    void transformationHasNoEndOfTurnDuration() {
        Permanent opal = addOpalGuardian();
        prepareOpponentCast();
        castOpponentCreature();
        resolveAllTriggers();

        advanceToUpkeep(player1);

        assertThat(gqs.isCreature(gd, opal)).isTrue();
        assertThat(gqs.isEnchantment(gd, opal)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opal)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opal)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, opal, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, opal, CardColor.RED)).isTrue();
    }
}
