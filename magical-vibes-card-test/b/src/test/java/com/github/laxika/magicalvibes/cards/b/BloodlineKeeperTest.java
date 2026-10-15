package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.v.VampireInterloper;
import com.github.laxika.magicalvibes.cards.v.VillageCannibals;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BloodlineKeeper.class, VampireInterloper.class, VillageCannibals.class})
class BloodlineKeeperTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability creates a 2/2 black Vampire creature token with flying")
    void tapAbilityCreatesVampireToken() {
        Permanent keeper = harness.addToBattlefieldAndReturn(player1, new BloodlineKeeper());

        keeper.setSummoningSick(false);

        int keeperIdx = gd.playerBattlefields.get(player1.getId()).indexOf(keeper);
        harness.activateAbility(player1, keeperIdx, null, null);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getName()).isEqualTo("Vampire");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.VAMPIRE);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
    }

    @Test
    @DisplayName("Cannot transform with fewer than 5 Vampires")
    void cannotTransformWithFewerThan5Vampires() {
        Permanent keeper = harness.addToBattlefieldAndReturn(player1, new BloodlineKeeper());
        addVampires(player1, 3); // Total: 4 (Keeper + 3)
        harness.addMana(player1, ManaColor.BLACK, 1);

        keeper.setSummoningSick(false);

        int keeperIdx = gd.playerBattlefields.get(player1.getId()).indexOf(keeper);

        // Ability index 1 is the transform ability
        assertThatThrownBy(() -> harness.activateAbility(player1, keeperIdx, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can transform with exactly 5 Vampires")
    void canTransformWithExactly5Vampires() {
        Permanent keeper = harness.addToBattlefieldAndReturn(player1, new BloodlineKeeper());
        addVampires(player1, 4); // Total: 5 (Keeper + 4)

        keeper.setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLACK, 1);

        int keeperIdx = gd.playerBattlefields.get(player1.getId()).indexOf(keeper);
        harness.activateAbility(player1, keeperIdx, 1, null, null);
        harness.passBothPriorities();

        // Should now be Lord of Lineage
        assertThat(keeper.getCard().getName()).isEqualTo("Lord of Lineage");
        assertThat(keeper.isTransformed()).isTrue();
        assertThat(gqs.getEffectivePower(gd, keeper)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, keeper)).isEqualTo(5);
    }

    @Test
    @DisplayName("Lord of Lineage gives other Vampires +2/+2")
    void lordOfLineageBuffsOtherVampires() {
        Permanent keeper = harness.addToBattlefieldAndReturn(player1, new BloodlineKeeper());
        addVampires(player1, 4); // Total: 5

        keeper.setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLACK, 1);

        // Transform
        int keeperIdx = gd.playerBattlefields.get(player1.getId()).indexOf(keeper);
        harness.activateAbility(player1, keeperIdx, 1, null, null);
        harness.passBothPriorities();

        // Verify other Vampires get +2/+2
        Permanent otherVampire = findPermanent(player1, "Vampire Interloper");

        // Vampire Interloper is 2/1 base; with +2/+2 from Lord of Lineage = 4/3
        assertThat(gqs.getEffectivePower(gd, otherVampire)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, otherVampire)).isEqualTo(3);
    }

    @Test
    @DisplayName("Lord of Lineage does not buff itself")
    void lordOfLineageDoesNotBuffItself() {
        Permanent keeper = harness.addToBattlefieldAndReturn(player1, new BloodlineKeeper());
        addVampires(player1, 4);

        keeper.setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLACK, 1);

        int keeperIdx = gd.playerBattlefields.get(player1.getId()).indexOf(keeper);
        harness.activateAbility(player1, keeperIdx, 1, null, null);
        harness.passBothPriorities();

        // Lord of Lineage is 5/5 base, no self-buff
        assertThat(gqs.getEffectivePower(gd, keeper)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, keeper)).isEqualTo(5);
    }

    @Test
    @DisplayName("Lord of Lineage does not buff opponent's Vampires")
    void lordOfLineageDoesNotBuffOpponentVampires() {
        Permanent keeper = harness.addToBattlefieldAndReturn(player1, new BloodlineKeeper());
        addVampires(player1, 4);
        harness.addToBattlefield(player2, new VampireInterloper());

        keeper.setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLACK, 1);

        int keeperIdx = gd.playerBattlefields.get(player1.getId()).indexOf(keeper);
        harness.activateAbility(player1, keeperIdx, 1, null, null);
        harness.passBothPriorities();

        Permanent opponentVampire = findPermanent(player2, "Vampire Interloper");
        // Vampire Interloper is 2/1 base, no buff from opponent's Lord of Lineage
        assertThat(gqs.getEffectivePower(gd, opponentVampire)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentVampire)).isEqualTo(1);
    }

    @Test
    @DisplayName("Lord of Lineage tap ability creates a 2/2 Vampire token with flying")
    void lordOfLineageTapAbilityCreatesToken() {
        Permanent keeper = harness.addToBattlefieldAndReturn(player1, new BloodlineKeeper());
        addVampires(player1, 4);

        keeper.setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLACK, 1);

        // Transform first
        int keeperIdx = gd.playerBattlefields.get(player1.getId()).indexOf(keeper);
        harness.activateAbility(player1, keeperIdx, 1, null, null);
        harness.passBothPriorities();

        // Untap for the tap ability
        keeper.untap();

        // Activate tap ability (ability index 0 on Lord of Lineage)
        keeperIdx = gd.playerBattlefields.get(player1.getId()).indexOf(keeper);
        harness.activateAbility(player1, keeperIdx, 0, null, null);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getName()).isEqualTo("Vampire");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2 + 2); // +2 from Lord of Lineage
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2 + 2); // +2 from Lord of Lineage
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.VAMPIRE);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    void transformCanBeActivatedWhileTappedAndSummoningSick() {
        Permanent keeper = harness.addToBattlefieldAndReturn(player1, new BloodlineKeeper());
        keeper.setSummoningSick(true);
        keeper.tap();
        addVampires(player1, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(keeper.isTransformed()).isTrue();
        assertThat(keeper.isTapped()).isTrue();
        assertThat(keeper.isSummoningSick()).isTrue();
        keeper.untap();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tapAbilityCannotBeActivatedWhileSummoningSick() {
        Permanent keeper = harness.addToBattlefieldAndReturn(player1, new BloodlineKeeper());
        keeper.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(keeper.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void transformRequiresBlackMana() {
        Permanent keeper = harness.addToBattlefieldAndReturn(player1, new BloodlineKeeper());
        addVampires(player1, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(keeper.isTransformed()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tokenAbilityTapsKeeperAndCannotBeActivatedAgainWhileTapped() {
        Permanent keeper = harness.addToBattlefieldAndReturn(player1, new BloodlineKeeper());
        keeper.setSummoningSick(false);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(keeper.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).toList()).hasSize(1);
    }

    @Test
    void tokenAbilityResolvesAfterKeeperLeavesBattlefield() {
        Permanent keeper = harness.addToBattlefieldAndReturn(player1, new BloodlineKeeper());
        keeper.setSummoningSick(false);
        harness.activateAbility(player1, 0, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(keeper);

        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).toList();
        assertThat(tokens).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, tokens.getFirst())).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tokens.getFirst())).isEqualTo(2);
    }

    @Test
    void opposingVampiresAndOwnNonVampiresDoNotMeetTransformRestriction() {
        Permanent keeper = harness.addToBattlefieldAndReturn(player1, new BloodlineKeeper());
        addVampires(player1, 3);
        addVampires(player2, 4);
        harness.addToBattlefield(player1, new VillageCannibals());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(keeper.isTransformed()).isFalse();
    }

    @Test
    void losingVampiresAfterActivationDoesNotPreventTransformation() {
        Permanent keeper = harness.addToBattlefieldAndReturn(player1, new BloodlineKeeper());
        addVampires(player1, 4);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 1, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(1);
        harness.passBothPriorities();

        assertThat(keeper.isTransformed()).isTrue();
    }

    @Test
    void multiplePendingTransformActivationsDoNotTransformBack() {
        Permanent keeper = harness.addToBattlefieldAndReturn(player1, new BloodlineKeeper());
        addVampires(player1, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        assertThat(keeper.isTransformed()).isTrue();
        harness.passBothPriorities();

        assertThat(keeper.isTransformed()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void lordDoesNotBoostNonVampiresAndItsBoostEndsWhenItLeaves() {
        Permanent keeper = harness.addToBattlefieldAndReturn(player1, new BloodlineKeeper());
        addVampires(player1, 4);
        Permanent human = harness.addToBattlefieldAndReturn(player1, new VillageCannibals());
        Permanent vampire = findPermanent(player1, "Vampire Interloper");
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(keeper);

        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(1);
    }

    private void addVampires(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefieldAndReturn(player, new VampireInterloper()).setSummoningSick(false);
        }
    }

}
