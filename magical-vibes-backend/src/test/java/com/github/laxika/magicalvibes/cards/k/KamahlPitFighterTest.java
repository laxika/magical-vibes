package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KamahlPitFighterTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Kamahl, Pit Fighter has correct card properties")
    void hasCorrectProperties() {
        KamahlPitFighter card = new KamahlPitFighter();

        assertThat(card.getName()).isEqualTo("Kamahl, Pit Fighter");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{4}{R}{R}");
        assertThat(card.getColor()).isEqualTo(CardColor.RED);
        assertThat(card.getPower()).isEqualTo(6);
        assertThat(card.getToughness()).isEqualTo(1);
        assertThat(card.getKeywords()).contains(Keyword.HASTE);
        assertThat(card.getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isNull();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(DealDamageToAnyTargetEffect.class);
        DealDamageToAnyTargetEffect effect = (DealDamageToAnyTargetEffect) card.getActivatedAbilities().get(0).getEffects().getFirst();
        assertThat(effect.damage()).isEqualTo(3);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new KamahlPitFighter()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Kamahl, Pit Fighter");
    }

    @Test
    @DisplayName("Resolving puts it on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new KamahlPitFighter()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Kamahl, Pit Fighter"));
    }

    // ===== Haste — can activate ability with summoning sickness =====

    @Test
    @DisplayName("Can activate tap ability the turn it enters the battlefield (Haste)")
    void canActivateWithSummoningSicknessDueToHaste() {
        KamahlPitFighter card = new KamahlPitFighter();
        Permanent kamahl = new Permanent(card);
        // summoningSick is true by default
        harness.getGameData().playerBattlefields.get(player1.getId()).add(kamahl);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(kamahl.isTapped()).isTrue();
        assertThat(harness.getGameData().stack).hasSize(1);
    }

    // ===== Activating ability =====

    @Test
    @DisplayName("Activating ability targeting player puts it on the stack")
    void activatingTargetingPlayerPutsOnStack() {
        Permanent kamahl = addReadyKamahl(player1);

        harness.activateAbility(player1, 0, null, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Kamahl, Pit Fighter");
        assertThat(entry.getTargetPermanentId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Activating ability targeting creature puts it on the stack")
    void activatingTargetingCreaturePutsOnStack() {
        addReadyKamahl(player1);
        Permanent target = addReadyCreature(player2);

        harness.activateAbility(player1, 0, null, target.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetPermanentId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Activating ability taps Kamahl")
    void activatingTapsKamahl() {
        Permanent kamahl = addReadyKamahl(player1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(kamahl.isTapped()).isTrue();
    }

    @Test
    @DisplayName("No mana is consumed when activating ability (tap-only cost)")
    void noManaConsumed() {
        addReadyKamahl(player1);
        harness.addMana(player1, ManaColor.RED, 3);

        harness.activateAbility(player1, 0, null, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    // ===== Dealing damage to player =====

    @Test
    @DisplayName("Deals 3 damage to target player")
    void deals3DamageToPlayer() {
        harness.setLife(player2, 20);
        addReadyKamahl(player1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    // ===== Dealing damage to creature =====

    @Test
    @DisplayName("Deals 3 damage to target creature, destroying a 2/2")
    void deals3DamageDestroying2Toughness() {
        addReadyKamahl(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenTapped() {
        Permanent kamahl = addReadyKamahl(player1);
        kamahl.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if target creature is removed before resolution")
    void fizzlesIfTargetCreatureRemoved() {
        addReadyKamahl(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.activateAbility(player1, 0, null, targetId);

        // Remove target before resolution
        harness.getGameData().playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Helpers =====

    private Permanent addReadyKamahl(Player player) {
        KamahlPitFighter card = new KamahlPitFighter();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
