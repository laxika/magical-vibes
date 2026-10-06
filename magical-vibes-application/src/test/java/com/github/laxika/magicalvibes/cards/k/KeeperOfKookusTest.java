package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.m.MortalWound;
import com.github.laxika.magicalvibes.cards.t.Tremor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeeperOfKookus.class, LightningBolt.class, MortalWound.class, Tremor.class})
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
    @CardUsed({KeeperOfKookus.class, LightningBolt.class})
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
    @CardUsed({KeeperOfKookus.class, Tremor.class})
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
    @CardUsed({KeeperOfKookus.class, MortalWound.class})
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

    @Test
    @CardUsed({KeeperOfKookus.class, LightningBolt.class})
    @DisplayName("Protection gained in response makes red removal lose its target")
    void protectionInResponseStopsRedRemoval() {
        Permanent keeper = addCreatureReady(player1, new KeeperOfKookus());
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player2, 0, keeper.getId());
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(keeper);
        assertThat(keeper.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Lightning Bolt");
    }

    @Test
    @DisplayName("Protection from red prevents a red creature from blocking")
    void protectionStopsRedBlocker() {
        addCreatureReady(player1, new KeeperOfKookus());
        addCreatureReady(player2, new KeeperOfKookus());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activating one Keeper does not grant protection to another")
    void protectionAppliesOnlyToSource() {
        Permanent source = addCreatureReady(player1, new KeeperOfKookus());
        Permanent other = addCreatureReady(player1, new KeeperOfKookus());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Tremor()));
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source).doesNotContain(other);
        assertThat(source.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Keeper of Kookus");
    }
}
