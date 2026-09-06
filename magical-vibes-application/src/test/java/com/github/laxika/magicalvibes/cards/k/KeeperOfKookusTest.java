package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.MortalWound;
import com.github.laxika.magicalvibes.cards.t.Tremor;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(KeeperOfKookus.class)
class KeeperOfKookusTest extends BaseCardTest {

    @Test
    @DisplayName("{R}: gains protection from red until end of turn")
    void grantsProtectionFromRed() {
        Permanent keeper = addCreatureReady(player1, new KeeperOfKookus());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(keeper.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
    }

    @Test
    @CardUsed(LightningBolt.class)
    @DisplayName("Protection from red stops a red spell from targeting this creature")
    void protectionStopsRedRemoval() {
        Permanent keeper = addCreatureReady(player1, new KeeperOfKookus());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player2, java.util.List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, keeper.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @CardUsed(Tremor.class)
    @DisplayName("Protection from red prevents non-targeted red damage")
    void protectionPreventsNonTargetedRedDamage() {
        Permanent keeper = addCreatureReady(player1, new KeeperOfKookus());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, java.util.List.of(new Tremor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(keeper);
        assertThat(keeper.getMarkedDamage()).isZero();
    }

    @Test
    @CardUsed(MortalWound.class)
    @DisplayName("Protection from red does not stop a green Aura from targeting this creature")
    void protectionAllowsNonRedAura() {
        Permanent keeper = addCreatureReady(player1, new KeeperOfKookus());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, java.util.List.of(new MortalWound()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, keeper.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof MortalWound
                        && keeper.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("The ability can be activated without tapping a summoning-sick creature")
    void abilityDoesNotTapSummoningSickCreature() {
        Permanent keeper = harness.addToBattlefieldAndReturn(player1, new KeeperOfKookus());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(keeper.isTapped()).isFalse();
        assertThat(keeper.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
    }

    @Test
    @DisplayName("The ability cannot be activated without red mana")
    void cannotActivateWithoutRedMana() {
        Permanent keeper = addCreatureReady(player1, new KeeperOfKookus());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(keeper.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.RED);
    }

    @Test
    @DisplayName("Protection from red wears off at end of turn")
    void protectionWearsOffAtEndOfTurn() {
        Permanent keeper = addCreatureReady(player1, new KeeperOfKookus());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(keeper.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(keeper.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.RED);
    }
}
