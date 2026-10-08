package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.Deathmark;
import com.github.laxika.magicalvibes.cards.g.GelidShackles;
import com.github.laxika.magicalvibes.cards.k.KrovikanScoundrel;
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

@CardUsed({WhiteShieldCrusader.class, Deathmark.class, GelidShackles.class,
        KrovikanScoundrel.class})
class WhiteShieldCrusaderTest extends BaseCardTest {

    @Test
    @DisplayName("White Shield Crusader has protection from black")
    void protectionFromBlackPreventsTargeting() {
        Permanent crusader = addCreatureReady(player2, new WhiteShieldCrusader());
        harness.setHand(player1, List.of(new Deathmark()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, crusader.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");
    }

    @Test
    @DisplayName("White Shield Crusader can be targeted by a white spell")
    void protectionFromBlackDoesNotPreventWhiteTargeting() {
        Permanent crusader = addCreatureReady(player1, new WhiteShieldCrusader());
        harness.setHand(player1, List.of(new GelidShackles()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castEnchantment(player1, 0, crusader.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Protection from black prevents combat damage from black creatures")
    void protectionFromBlackPreventsCombatDamage() {
        addCreatureReady(player1, new KrovikanScoundrel());
        Permanent crusader = addCreatureReady(player2, new WhiteShieldCrusader());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(crusader.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Krovikan Scoundrel");
        harness.assertOnBattlefield(player2, "White Shield Crusader");
    }

    @Test
    @DisplayName("Protection from black prevents black creatures from blocking")
    void protectionFromBlackPreventsBlocking() {
        addCreatureReady(player1, new WhiteShieldCrusader());
        addCreatureReady(player2, new KrovikanScoundrel());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Flying ability requires white mana")
    void cannotActivateFlyingWithoutWhiteMana() {
        addCreatureReady(player1, new WhiteShieldCrusader());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("White ability grants flying until end of turn")
    void grantsFlyingUntilEndOfTurn() {
        Permanent crusader = addCreatureReady(player1, new WhiteShieldCrusader());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, crusader, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, crusader, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Two white ability gives +1/+0 until end of turn")
    void boostsPowerUntilEndOfTurn() {
        Permanent crusader = addCreatureReady(player1, new WhiteShieldCrusader());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(2);
    }

    @Test
    @DisplayName("Power boost requires two white mana")
    void cannotActivateBoostWithOnlyOneWhiteMana() {
        addCreatureReady(player1, new WhiteShieldCrusader());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Repeated power boosts accumulate only on their source and expire together")
    void repeatedBoostsAccumulateOnlyOnSource() {
        Permanent crusader = addCreatureReady(player1, new WhiteShieldCrusader());
        Permanent other = addCreatureReady(player1, new WhiteShieldCrusader());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, crusader)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(2);
    }

    @Test
    @DisplayName("Both abilities can be activated while tapped and summoning sick")
    void activatesWhileTappedAndSummoningSick() {
        Permanent crusader = addCreatureReady(player1, new WhiteShieldCrusader());
        Permanent other = addCreatureReady(player1, new WhiteShieldCrusader());
        crusader.setSummoningSick(true);
        crusader.setTapped(true);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, crusader, Keyword.FLYING)).isTrue();
        assertThat(gqs.getEffectivePower(gd, crusader)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, other, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
    }
}
