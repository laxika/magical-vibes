package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GelidShackles;
import com.github.laxika.magicalvibes.cards.k.KjeldoranOutrider;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StromgaldCrusader.class, GelidShackles.class, KjeldoranOutrider.class})
class StromgaldCrusaderTest extends BaseCardTest {

    @Test
    @DisplayName("Stromgald Crusader has protection from white")
    void hasProtectionFromWhite() {
        Permanent crusader = addCreatureReady(player1, new StromgaldCrusader());

        assertThat(gqs.hasProtectionFrom(gd, crusader, CardColor.WHITE)).isTrue();
        assertThat(gqs.hasProtectionFrom(gd, crusader, CardColor.BLACK)).isFalse();
    }

    @Test
    @DisplayName("Protection from white prevents white spells from targeting Stromgald Crusader")
    void protectionFromWhitePreventsTargeting() {
        Permanent crusader = addCreatureReady(player1, new StromgaldCrusader());
        harness.setHand(player1, List.of(new GelidShackles()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, crusader.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from white");
    }

    @Test
    @DisplayName("Protection from white prevents white creatures from blocking Stromgald Crusader")
    void protectionFromWhitePreventsBlocking() {
        addCreatureReady(player1, new StromgaldCrusader());
        addCreatureReady(player2, new KjeldoranOutrider());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from white prevents combat damage from white creatures")
    void protectionFromWhitePreventsCombatDamage() {
        Permanent crusader = addCreatureReady(player1, new StromgaldCrusader());
        addCreatureReady(player2, new KjeldoranOutrider());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(crusader.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Kjeldoran Outrider");
        harness.assertOnBattlefield(player1, "Stromgald Crusader");
    }

    @Test
    @DisplayName("Resolving the first ability grants flying until end of turn")
    void firstAbilityGrantsFlying() {
        Permanent crusader = addCreatureReady(player1, new StromgaldCrusader());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, crusader, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying granted by the first ability resets at end of turn cleanup")
    void flyingResetsAtEndOfTurn() {
        Permanent crusader = addCreatureReady(player1, new StromgaldCrusader());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, crusader, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, crusader, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate the flying ability without black mana")
    void cannotActivateFlyingWithoutMana() {
        addCreatureReady(player1, new StromgaldCrusader());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Resolving the second ability gives +1/+0 until end of turn")
    void secondAbilityBoostsPower() {
        Permanent crusader = addCreatureReady(player1, new StromgaldCrusader());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost granted by the second ability resets at end of turn cleanup")
    void boostResetsAtEndOfTurn() {
        Permanent crusader = addCreatureReady(player1, new StromgaldCrusader());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate the boost ability with only one black mana")
    void cannotActivateBoostWithoutEnoughMana() {
        addCreatureReady(player1, new StromgaldCrusader());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Repeated boosts accumulate on the source without boosting another Crusader")
    void repeatedBoostsAccumulateOnlyOnSource() {
        Permanent crusader = addCreatureReady(player1, new StromgaldCrusader());
        Permanent other = addCreatureReady(player1, new StromgaldCrusader());
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 0, 1, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Both abilities can be activated while tapped and summoning sick")
    void abilitiesWorkWhileTappedAndSummoningSick() {
        Permanent crusader = harness.addToBattlefieldAndReturn(player1, new StromgaldCrusader());
        crusader.setSummoningSick(true);
        crusader.tap();
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 1, null, null);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, crusader, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(1);
        assertThat(crusader.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Granted flying prevents a nonflying black creature from blocking")
    void grantedFlyingPreventsGroundBlocker() {
        addCreatureReady(player1, new StromgaldCrusader());
        addCreatureReady(player2, new StromgaldCrusader());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

}
