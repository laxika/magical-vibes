package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostCreaturesBySubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.cards.a.AvenCloudchaser;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HonorGuard;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MobilizationTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameQueryService gqs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gqs = harness.getGameQueryService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Mobilization has correct card properties")
    void hasCorrectProperties() {
        Mobilization card = new Mobilization();

        assertThat(card.getName()).isEqualTo("Mobilization");
        assertThat(card.getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(card.getManaCost()).isEqualTo("{2}{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(BoostCreaturesBySubtypeEffect.class);
        BoostCreaturesBySubtypeEffect staticEffect = (BoostCreaturesBySubtypeEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(staticEffect.affectedSubtypes()).containsExactly(CardSubtype.SOLDIER);
        assertThat(staticEffect.powerBoost()).isEqualTo(0);
        assertThat(staticEffect.toughnessBoost()).isEqualTo(0);
        assertThat(staticEffect.grantedKeywords()).containsExactly(Keyword.VIGILANCE);
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst()).isInstanceOf(CreateCreatureTokenEffect.class);
        CreateCreatureTokenEffect tokenEffect = (CreateCreatureTokenEffect) card.getActivatedAbilities().get(0).getEffects().getFirst();
        assertThat(tokenEffect.tokenName()).isEqualTo("Soldier");
        assertThat(tokenEffect.power()).isEqualTo(1);
        assertThat(tokenEffect.toughness()).isEqualTo(1);
        assertThat(tokenEffect.color()).isEqualTo(CardColor.WHITE);
        assertThat(tokenEffect.subtypes()).containsExactly(CardSubtype.SOLDIER);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{2}{W}");
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Mobilization puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new Mobilization()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Mobilization");
    }

    @Test
    @DisplayName("Resolving puts Mobilization onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new Mobilization()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mobilization"));
    }

    // ===== Token creation via mana-activated ability =====

    @Test
    @DisplayName("Activating ability puts token creation on the stack")
    void activatingAbilityPutsOnStack() {
        addMobilizationReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Mobilization");
    }

    @Test
    @DisplayName("Resolving ability creates a 1/1 Soldier token")
    void resolvingAbilityCreatesToken() {
        addMobilizationReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        Permanent token = battlefield.stream()
                .filter(p -> p.getCard().getName().equals("Soldier"))
                .findFirst().orElseThrow();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
    }

    @Test
    @DisplayName("Token enters with summoning sickness")
    void tokenEntersWithSummoningSickness() {
        addMobilizationReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Soldier"))
                .findFirst().orElseThrow();
        assertThat(token.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Can create multiple tokens")
    void canCreateMultipleTokens() {
        addMobilizationReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 9);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        long tokenCount = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Soldier"))
                .count();
        assertThat(tokenCount).isEqualTo(3);
    }

    // ===== Static effect: vigilance for Soldiers =====

    @Test
    @DisplayName("Soldier creatures have vigilance with Mobilization on battlefield")
    void soldiersGetVigilance() {
        addMobilizationReady(player1);
        harness.addToBattlefield(player1, new HonorGuard());

        Permanent soldier = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Honor Guard"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasKeyword(gd, soldier, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Non-Soldier creatures do not get vigilance")
    void nonSoldiersDoNotGetVigilance() {
        addMobilizationReady(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Token Soldier also gets vigilance from Mobilization")
    void tokenGetsVigilance() {
        addMobilizationReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Soldier"))
                .findFirst().orElseThrow();

        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Vigilance is removed when Mobilization leaves the battlefield")
    void vigilanceRemovedWhenMobilizationLeaves() {
        addMobilizationReady(player1);
        harness.addToBattlefield(player1, new HonorGuard());

        Permanent soldier = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Honor Guard"))
                .findFirst().orElseThrow();

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
        addMobilizationReady(player1);

        HonorGuard guard = new HonorGuard();
        Permanent soldier = new Permanent(guard);
        soldier.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(soldier);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // Soldier is at index 1 (Mobilization is at 0)
        gs.declareAttackers(gd, player1, List.of(1));

        // Combat resolves fully via auto-pass, clearing isAttacking; tapped state persists
        assertThat(soldier.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Non-Soldier creature still taps when attacking")
    void nonSoldierStillTapsWhenAttacking() {
        addMobilizationReady(player1);

        GrizzlyBears bears = new GrizzlyBears();
        Permanent bearsPerm = new Permanent(bears);
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        // Bears is at index 1 (Mobilization is at 0)
        gs.declareAttackers(gd, player1, List.of(1));

        // Combat resolves fully via auto-pass, clearing isAttacking; tapped state persists
        assertThat(bearsPerm.isTapped()).isTrue();
    }

    // ===== Helper methods =====

    private Permanent addMobilizationReady(Player player) {
        Mobilization card = new Mobilization();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}


