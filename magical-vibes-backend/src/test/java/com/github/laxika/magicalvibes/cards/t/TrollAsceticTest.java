package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrollAsceticTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;
    private GameService gs;
    private GameQueryService gqs;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        gs = harness.getGameService();
        gqs = harness.getGameQueryService();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Troll Ascetic has correct card properties")
    void hasCorrectProperties() {
        TrollAscetic card = new TrollAscetic();

        assertThat(card.getName()).isEqualTo("Troll Ascetic");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{1}{G}{G}");
        assertThat(card.getColor()).isEqualTo(CardColor.GREEN);
        assertThat(card.getPower()).isEqualTo(3);
        assertThat(card.getToughness()).isEqualTo(2);
        assertThat(card.getSubtypes()).containsExactlyInAnyOrder(CardSubtype.TROLL, CardSubtype.SHAMAN);
        assertThat(card.getKeywords()).contains(Keyword.HEXPROOF);
        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(RegenerateEffect.class);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{1}{G}");
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Troll Ascetic puts it on the stack and resolves to battlefield")
    void castingAndResolving() {
        harness.setHand(player1, List.of(new TrollAscetic()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Troll Ascetic"));
    }

    // ===== Hexproof: opponent cannot target =====

    @Test
    @DisplayName("Opponent cannot target Troll Ascetic with spells")
    void opponentCannotTargetWithSpells() {
        // Player1 is active and owns the Troll
        Permanent trollPerm = addTrollAsceticReady(player1);

        // Player2 tries to Shock the Troll
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, trollPerm.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Opponent cannot target Troll Ascetic with Boomerang")
    void opponentCannotTargetWithBoomerang() {
        Permanent trollPerm = addTrollAsceticReady(player1);

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Boomerang()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, trollPerm.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    // ===== Hexproof: controller CAN target =====

    @Test
    @DisplayName("Controller can target own Troll Ascetic with spells")
    void controllerCanTargetOwnTrollAscetic() {
        Permanent trollPerm = addTrollAsceticReady(player1);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, trollPerm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Giant Growth");
        assertThat(gd.stack.getFirst().getTargetPermanentId()).isEqualTo(trollPerm.getId());
    }

    // ===== Hexproof: hasKeyword check =====

    @Test
    @DisplayName("Troll Ascetic has hexproof keyword on the battlefield")
    void hasHexproofKeyword() {
        Permanent trollPerm = addTrollAsceticReady(player1);

        assertThat(gqs.hasKeyword(gd, trollPerm, Keyword.HEXPROOF)).isTrue();
    }

    // ===== Regenerate activated ability =====

    @Test
    @DisplayName("Activating regeneration ability puts it on the stack")
    void activatingRegenPutsOnStack() {
        Permanent trollPerm = addTrollAsceticReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Troll Ascetic");
        assertThat(entry.getTargetPermanentId()).isEqualTo(trollPerm.getId());
    }

    @Test
    @DisplayName("Resolving regeneration ability grants a regeneration shield")
    void resolvingRegenGrantsShield() {
        addTrollAsceticReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent troll = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(troll.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Mana is consumed when activating regeneration ability")
    void manaConsumedOnRegenActivation() {
        addTrollAsceticReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate regeneration ability without enough mana")
    void cannotActivateRegenWithoutMana() {
        addTrollAsceticReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== Regeneration saves from combat damage =====

    @Test
    @DisplayName("Regeneration shield saves Troll Ascetic from lethal combat damage")
    void regenSavesFromLethalCombatDamage() {
        Permanent trollPerm = addTrollAsceticReady(player1);
        trollPerm.setRegenerationShield(1);
        trollPerm.setBlocking(true);
        trollPerm.addBlockingTarget(0);

        // 4/4 attacker deals lethal to 3/2 Troll Ascetic
        Permanent attacker = addCreatureReady(player2, 4, 4);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Troll Ascetic"));
        Permanent troll = findPermanent(player1, "Troll Ascetic");
        assertThat(troll.isTapped()).isTrue();
        assertThat(troll.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Troll Ascetic dies without regeneration shield in combat")
    void diesWithoutRegenShield() {
        Permanent trollPerm = addTrollAsceticReady(player1);
        trollPerm.setBlocking(true);
        trollPerm.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, 4, 4);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Troll Ascetic"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Troll Ascetic"));
    }

    // ===== Regeneration shield clears at end of turn =====

    @Test
    @DisplayName("Regeneration shield clears at end of turn cleanup")
    void regenShieldClearsAtEndOfTurn() {
        addTrollAsceticReady(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent troll = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(troll.getRegenerationShield()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(troll.getRegenerationShield()).isEqualTo(0);
    }

    // ===== Helper methods =====

    private Permanent addTrollAsceticReady(Player player) {
        TrollAscetic card = new TrollAscetic();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addCreatureReady(Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent findPermanent(Player player, String name) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals(name))
                .findFirst().orElseThrow();
    }
}
