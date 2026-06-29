package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BloodlineKeeperTest extends BaseCardTest {

    // ===== Front face: Tap ability to create 2/2 Vampire token with flying =====

    @Test
    @DisplayName("Tap ability creates a 2/2 black Vampire creature token with flying")
    void tapAbilityCreatesVampireToken() {
        harness.addToBattlefield(player1, new BloodlineKeeper());
        Permanent keeper = findPermanent(player1, "Bloodline Keeper");
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

    // ===== Transform ability: {B} to transform if 5+ Vampires =====

    @Test
    @DisplayName("Cannot transform with fewer than 5 Vampires")
    void cannotTransformWithFewerThan5Vampires() {
        harness.addToBattlefield(player1, new BloodlineKeeper());
        addVampires(player1, 3); // Total: 4 (Keeper + 3)
        Permanent keeper = findPermanent(player1, "Bloodline Keeper");
        keeper.setSummoningSick(false);

        int keeperIdx = gd.playerBattlefields.get(player1.getId()).indexOf(keeper);

        // Ability index 1 is the transform ability
        assertThatThrownBy(() -> harness.activateAbility(player1, keeperIdx, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can transform with exactly 5 Vampires")
    void canTransformWithExactly5Vampires() {
        harness.addToBattlefield(player1, new BloodlineKeeper());
        addVampires(player1, 4); // Total: 5 (Keeper + 4)
        Permanent keeper = findPermanent(player1, "Bloodline Keeper");
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

    // ===== Back face: Lord of Lineage =====

    @Test
    @DisplayName("Lord of Lineage gives other Vampires +2/+2")
    void lordOfLineageBuffsOtherVampires() {
        harness.addToBattlefield(player1, new BloodlineKeeper());
        addVampires(player1, 4); // Total: 5
        Permanent keeper = findPermanent(player1, "Bloodline Keeper");
        keeper.setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLACK, 1);

        // Transform
        int keeperIdx = gd.playerBattlefields.get(player1.getId()).indexOf(keeper);
        harness.activateAbility(player1, keeperIdx, 1, null, null);
        harness.passBothPriorities();

        // Verify other Vampires get +2/+2
        Permanent otherVampire = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Barony Vampire"))
                .findFirst().orElseThrow();

        // Barony Vampire is 3/2 base; with +2/+2 from Lord of Lineage = 5/4
        assertThat(gqs.getEffectivePower(gd, otherVampire)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, otherVampire)).isEqualTo(4);
    }

    @Test
    @DisplayName("Lord of Lineage does not buff itself")
    void lordOfLineageDoesNotBuffItself() {
        harness.addToBattlefield(player1, new BloodlineKeeper());
        addVampires(player1, 4);
        Permanent keeper = findPermanent(player1, "Bloodline Keeper");
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
        harness.addToBattlefield(player1, new BloodlineKeeper());
        addVampires(player1, 4);
        harness.addToBattlefield(player2, new BaronyVampire());
        Permanent keeper = findPermanent(player1, "Bloodline Keeper");
        keeper.setSummoningSick(false);
        harness.addMana(player1, ManaColor.BLACK, 1);

        int keeperIdx = gd.playerBattlefields.get(player1.getId()).indexOf(keeper);
        harness.activateAbility(player1, keeperIdx, 1, null, null);
        harness.passBothPriorities();

        Permanent opponentVampire = findPermanent(player2, "Barony Vampire");
        // Barony Vampire is 3/2 base, no buff from opponent's Lord of Lineage
        assertThat(gqs.getEffectivePower(gd, opponentVampire)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, opponentVampire)).isEqualTo(2);
    }

    @Test
    @DisplayName("Lord of Lineage tap ability creates a 2/2 Vampire token with flying")
    void lordOfLineageTapAbilityCreatesToken() {
        harness.addToBattlefield(player1, new BloodlineKeeper());
        addVampires(player1, 4);
        Permanent keeper = findPermanent(player1, "Bloodline Keeper");
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
    @DisplayName("Card has correct effects configured")
    void hasCorrectEffects() {
        BloodlineKeeper card = new BloodlineKeeper();

        // Two activated abilities on front face
        assertThat(card.getActivatedAbilities()).hasSize(2);

        // First ability: tap to create token
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getEffects())
                .anyMatch(e -> e instanceof CreateTokenEffect);

        // Second ability: {B} transform with subtype restriction
        assertThat(card.getActivatedAbilities().get(1).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(1).getManaCost()).isEqualTo("{B}");
        assertThat(card.getActivatedAbilities().get(1).getEffects())
                .anyMatch(e -> e instanceof TransformSelfEffect);
        assertThat(card.getActivatedAbilities().get(1).getRequiredControlledSubtype()).isEqualTo(CardSubtype.VAMPIRE);
        assertThat(card.getActivatedAbilities().get(1).getRequiredControlledSubtypeCount()).isEqualTo(5);

        // Back face should exist
        assertThat(card.getBackFaceCard()).isNotNull();
        assertThat(card.getBackFaceCard().getEffects(EffectSlot.STATIC))
                .anyMatch(e -> e instanceof StaticBoostEffect);
    }

    // ===== Helpers =====

    private void addVampires(Player player, int count) {
        for (int i = 0; i < count; i++) {
            Permanent vamp = new Permanent(new BaronyVampire());
            vamp.setSummoningSick(false);
            gd.playerBattlefields.get(player.getId()).add(vamp);
        }
    }

}
