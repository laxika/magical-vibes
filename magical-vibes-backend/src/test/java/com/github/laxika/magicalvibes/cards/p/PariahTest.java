package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PariahTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Pariah has correct card properties")
    void hasCorrectProperties() {
        Pariah card = new Pariah();

        assertThat(card.getName()).isEqualTo("Pariah");
        assertThat(card.getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(card.getManaCost()).isEqualTo("{2}{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.AURA);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(RedirectPlayerDamageToEnchantedCreatureEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Pariah puts it on the stack as enchantment spell")
    void castingPutsOnStack() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new Pariah()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Pariah");
    }

    @Test
    @DisplayName("Resolving Pariah attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = new Permanent(new GrizzlyBears());
        bearsPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bearsPerm);

        harness.setHand(player1, List.of(new Pariah()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Pariah")
                        && p.getAttachedTo() != null
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    // ===== Combat damage redirection =====

    @Test
    @DisplayName("Combat damage is redirected to enchanted creature and player takes no damage")
    void combatDamageRedirectedToEnchantedCreature() {
        // Player2 has a creature with Pariah attached
        Permanent wallPerm = new Permanent(new GrizzlyBears()); // 2/2
        wallPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(wallPerm);

        Permanent pariahPerm = new Permanent(new Pariah());
        pariahPerm.setAttachedTo(wallPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(pariahPerm);

        // Player1 has an unblocked attacker (2/2)
        Permanent attackerPerm = new Permanent(new GrizzlyBears());
        attackerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(attackerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.ATTACKER_DECLARATION;

        gs.declareAttackers(gd, player1, List.of(0));

        // No blockers
        gd.interaction.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;
        gs.declareBlockers(gd, player2, List.of());

        // Player2 life should remain at 20 (damage redirected to creature)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Enchanted creature dies when redirected damage >= toughness")
    void enchantedCreatureDiesFromRedirectedDamage() {
        // Player2 has a 2/2 creature with Pariah attached
        Permanent wallPerm = new Permanent(new GrizzlyBears()); // 2/2
        wallPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(wallPerm);

        Permanent pariahPerm = new Permanent(new Pariah());
        pariahPerm.setAttachedTo(wallPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(pariahPerm);

        // Player1 has an unblocked attacker (2/2)
        Permanent attackerPerm = new Permanent(new GrizzlyBears());
        attackerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(attackerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.ATTACKER_DECLARATION;

        gs.declareAttackers(gd, player1, List.of(0));

        gd.interaction.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;
        gs.declareBlockers(gd, player2, List.of());

        // Enchanted creature (2/2) takes 2 damage -> dies
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Grizzly Bears goes to graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Pariah goes to graveyard when enchanted creature dies")
    void pariahGoesToGraveyardWhenCreatureDies() {
        // Player2 has a 2/2 creature with Pariah attached
        Permanent wallPerm = new Permanent(new GrizzlyBears());
        wallPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(wallPerm);

        Permanent pariahPerm = new Permanent(new Pariah());
        pariahPerm.setAttachedTo(wallPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(pariahPerm);

        // Player1 has an unblocked attacker (2/2) — enough to kill the 2/2
        Permanent attackerPerm = new Permanent(new GrizzlyBears());
        attackerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(attackerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.ATTACKER_DECLARATION;

        gs.declareAttackers(gd, player1, List.of(0));

        gd.interaction.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;
        gs.declareBlockers(gd, player2, List.of());

        // Pariah should be in graveyard (orphaned aura cleanup)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Pariah"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Pariah"));
    }

    // ===== Hurricane damage redirection =====

    @Test
    @DisplayName("Hurricane damage is redirected to enchanted creature")
    void hurricaneDamageRedirected() {
        // Player1 has a creature with Pariah attached (will redirect Hurricane damage)
        Permanent wallPerm = new Permanent(new GrizzlyBears()); // 2/2
        wallPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(wallPerm);

        Permanent pariahPerm = new Permanent(new Pariah());
        pariahPerm.setAttachedTo(wallPerm.getId());
        gd.playerBattlefields.get(player1.getId()).add(pariahPerm);

        // Cast Hurricane for X=1
        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        // Player1 takes 0 (redirected to creature), player2 takes 1
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    // ===== Without Pariah, damage is dealt normally =====

    @Test
    @DisplayName("Without Pariah, combat damage is dealt normally to player")
    void withoutPariahDamageDealtNormally() {
        // Player1 has an unblocked attacker (2/2)
        Permanent attackerPerm = new Permanent(new GrizzlyBears());
        attackerPerm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(attackerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.ATTACKER_DECLARATION;

        gs.declareAttackers(gd, player1, List.of(0));

        gd.interaction.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;
        gs.declareBlockers(gd, player2, List.of());

        // Player2 takes 2 combat damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}

