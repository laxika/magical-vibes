package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Pariah.class, GrizzlyBears.class, Hurricane.class, HowlingMine.class})
class PariahTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Pariah puts it on the stack as enchantment spell")
    void castingPutsOnStack() {
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

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
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Pariah()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        gs.playCard(gd, player1, 0, 0, bearsPerm.getId(), null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Pariah")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    // ===== Combat damage redirection =====

    @Test
    @DisplayName("Combat damage is redirected to enchanted creature and player takes no damage")
    void combatDamageRedirectedToEnchantedCreature() {
        // Player2 has a creature with Pariah attached
        Permanent wallPerm = addCreatureReady(player2, new GrizzlyBears()); // 2/2

        Permanent pariahPerm = new Permanent(new Pariah());
        pariahPerm.setAttachedTo(wallPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(pariahPerm);

        // Player1 has an unblocked attacker (2/2)
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        // Player2 life should remain at 20 (damage redirected to creature)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Enchanted creature dies when redirected damage >= toughness")
    void enchantedCreatureDiesFromRedirectedDamage() {
        // Player2 has a 2/2 creature with Pariah attached
        Permanent wallPerm = addCreatureReady(player2, new GrizzlyBears()); // 2/2

        Permanent pariahPerm = new Permanent(new Pariah());
        pariahPerm.setAttachedTo(wallPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(pariahPerm);

        // Player1 has an unblocked attacker (2/2)
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        // Enchanted creature (2/2) takes 2 damage -> dies
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        // Grizzly Bears goes to graveyard
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Pariah goes to graveyard when enchanted creature dies")
    void pariahGoesToGraveyardWhenCreatureDies() {
        // Player2 has a 2/2 creature with Pariah attached
        Permanent wallPerm = addCreatureReady(player2, new GrizzlyBears());

        Permanent pariahPerm = new Permanent(new Pariah());
        pariahPerm.setAttachedTo(wallPerm.getId());
        gd.playerBattlefields.get(player2.getId()).add(pariahPerm);

        // Player1 has an unblocked attacker (2/2) — enough to kill the 2/2
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        // Pariah should be in graveyard (orphaned aura cleanup)
        harness.assertNotOnBattlefield(player2, "Pariah");
        harness.assertInGraveyard(player2, "Pariah");
    }

    // ===== Hurricane damage redirection =====

    @Test
    @DisplayName("Hurricane damage is redirected to enchanted creature")
    void hurricaneDamageRedirected() {
        // Player1 has a creature with Pariah attached (will redirect Hurricane damage)
        Permanent wallPerm = addCreatureReady(player1, new GrizzlyBears()); // 2/2

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

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Can target a creature with Pariah")
    void canTargetCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Pariah()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Can enchant an opponent's creature and redirects damage to it for Pariah's controller")
    void canEnchantOpponentsCreatureAndRedirectsControllerDamage() {
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Pariah()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, opponentCreature.getId());
        harness.passBothPriorities();

        Permanent pariah = findPermanent(player1, "Pariah");
        assertThat(pariah.getAttachedTo()).isEqualTo(opponentCreature.getId());

        harness.setHand(player1, List.of(new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(opponentCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Pariah")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new HowlingMine());
        harness.setHand(player1, List.of(new Pariah()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        Permanent artifact = findPermanent(player1, "Howling Mine");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Without Pariah, damage is dealt normally =====

    @Test
    @DisplayName("Without Pariah, combat damage is dealt normally to player")
    void withoutPariahDamageDealtNormally() {
        // Player1 has an unblocked attacker (2/2)
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        // Player2 takes 2 combat damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}

