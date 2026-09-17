package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HonorGuard;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Mobilization.class, HonorGuard.class, GrizzlyBears.class})
class MobilizationTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Mobilization puts it on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new Mobilization(), "{2}{W}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
    }

    @Test
    @DisplayName("Resolving puts Mobilization onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new Mobilization(), "{2}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    // ===== Token creation via mana-activated ability =====

    @Test
    @DisplayName("Activating ability puts token creation on the stack")
    void activatingAbilityPutsOnStack() {
        addCreatureReady(player1, new Mobilization());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Activating the ability does not tap Mobilization")
    void activatingAbilityDoesNotTapMobilization() {
        Permanent mobilization = addCreatureReady(player1, new Mobilization());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(mobilization.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Resolving ability creates a 1/1 Soldier token")
    void resolvingAbilityCreatesToken() {
        addCreatureReady(player1, new Mobilization());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent token = findPermanent(player1, "Soldier");
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
    }

    @Test
    @DisplayName("Token enters with summoning sickness")
    void tokenEntersWithSummoningSickness() {
        addCreatureReady(player1, new Mobilization());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Soldier");
        assertThat(token.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Can create multiple tokens")
    void canCreateMultipleTokens() {
        addCreatureReady(player1, new Mobilization());
        harness.addMana(player1, ManaColor.WHITE, 9);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        long tokenCount = countPermanents(player1, "Soldier");
        assertThat(tokenCount).isEqualTo(3);
    }

    // ===== Static effect: vigilance for Soldiers =====

    @Test
    @DisplayName("Soldier creatures have vigilance with Mobilization on battlefield")
    void soldiersGetVigilance() {
        addCreatureReady(player1, new Mobilization());
        harness.addToBattlefield(player1, new HonorGuard());

        Permanent soldier = findPermanent(player1, "Honor Guard");

        assertThat(gqs.hasKeyword(gd, soldier, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Non-Soldier creatures do not get vigilance")
    void nonSoldiersDoNotGetVigilance() {
        addCreatureReady(player1, new Mobilization());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Token Soldier also gets vigilance from Mobilization")
    void tokenGetsVigilance() {
        addCreatureReady(player1, new Mobilization());
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Soldier");

        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Soldier creatures an opponent controls also have vigilance")
    void opponentsSoldiersGetVigilance() {
        addCreatureReady(player1, new Mobilization());
        Permanent soldier = addCreatureReady(player2, new HonorGuard());

        assertThat(gqs.hasKeyword(gd, soldier, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Vigilance is removed when Mobilization leaves the battlefield")
    void vigilanceRemovedWhenMobilizationLeaves() {
        addCreatureReady(player1, new Mobilization());
        harness.addToBattlefield(player1, new HonorGuard());

        Permanent soldier = findPermanent(player1, "Honor Guard");

        assertThat(gqs.hasKeyword(gd, soldier, Keyword.VIGILANCE)).isTrue();

        // Remove Mobilization
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Mobilization"));

        assertThat(gqs.hasKeyword(gd, soldier, Keyword.VIGILANCE)).isFalse();
    }

    // ===== Vigilance in combat: attacker doesn't tap =====

    @Test
    @DisplayName("Soldier with vigilance does not tap when attacking")
    void soldierWithVigilanceDoesNotTapWhenAttacking() {
        addCreatureReady(player1, new Mobilization());
        Permanent soldier = addCreatureReady(player1, new HonorGuard());

        // Soldier is at index 1 (Mobilization is at 0)
        declareAttackers(List.of(1));

        // Combat resolves fully via auto-pass, clearing isAttacking; tapped state persists
        assertThat(soldier.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Non-Soldier creature still taps when attacking")
    void nonSoldierStillTapsWhenAttacking() {
        addCreatureReady(player1, new Mobilization());
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        // Bears is at index 1 (Mobilization is at 0)
        declareAttackers(List.of(1));

        // Combat resolves fully via auto-pass, clearing isAttacking; tapped state persists
        assertThat(bearsPerm.isTapped()).isTrue();
    }
}
