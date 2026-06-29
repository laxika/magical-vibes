package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellToSourceEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryHasTargetPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SpellskiteTest extends BaseCardTest {

    @Test
    @DisplayName("Spellskite has correct activated ability")
    void hasCorrectAbility() {
        Spellskite card = new Spellskite();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        ActivatedAbility ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getManaCost()).isEqualTo("{U/P}");
        assertThat(ability.isNeedsSpellTarget()).isTrue();
        assertThat(ability.getEffects()).hasSize(1);
        assertThat(ability.getEffects().getFirst()).isInstanceOf(ChangeTargetOfTargetSpellToSourceEffect.class);
        assertThat(ability.getTargetFilter()).isEqualTo(new StackEntryPredicateTargetFilter(
                new StackEntryHasTargetPredicate(),
                "Target must be a spell or ability on the stack."
        ));
    }

    @Test
    @DisplayName("Spellskite redirects a targeted spell to itself")
    void redirectsTargetedSpellToSelf() {
        Spellskite spellskite = new Spellskite();
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, spellskite);
        harness.addToBattlefield(player1, bears);
        UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");

        // Make player2 the active player so they can cast spells
        harness.forceActivePlayer(player2);
        Boomerang boomerang = new Boomerang();
        harness.setHand(player2, List.of(boomerang));
        harness.addMana(player2, ManaColor.BLUE, 2);

        // Player2 casts Boomerang targeting Grizzly Bears
        harness.castInstant(player2, 0, bearsPermId);
        harness.passPriority(player2);

        // Player1 activates Spellskite's ability targeting Boomerang, paying blue mana
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, boomerang.getId());
        harness.passBothPriorities();

        // Spellskite's ability resolves — Boomerang now targets Spellskite
        // Resolve Boomerang
        harness.passBothPriorities();

        // Spellskite should be bounced, bears should remain
        harness.assertNotOnBattlefield(player1, "Spellskite");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Spellskite can pay with 2 life instead of blue mana")
    void canPayWithLife() {
        Spellskite spellskite = new Spellskite();
        GrizzlyBears bears = new GrizzlyBears();
        harness.addToBattlefield(player1, spellskite);
        harness.addToBattlefield(player1, bears);
        UUID bearsPermId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceActivePlayer(player2);
        Boomerang boomerang = new Boomerang();
        harness.setHand(player2, List.of(boomerang));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.setLife(player1, 20);
        harness.castInstant(player2, 0, bearsPermId);
        harness.passPriority(player2);

        // Activate paying with life (no mana added)
        harness.activateAbility(player1, 0, null, boomerang.getId());
        harness.passBothPriorities();

        // Spellskite's ability resolves
        harness.passBothPriorities();

        // Paid 2 life
        harness.assertLife(player1, 18);
        // Spellskite bounced, bears remain
        harness.assertNotOnBattlefield(player1, "Spellskite");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Spellskite does nothing if it is not a legal target for the spell")
    void doesNothingIfNotLegalTarget() {
        Spellskite spellskite = new Spellskite();
        harness.addToBattlefield(player1, spellskite);

        // Lava Axe targets "target player" — Spellskite is not a player
        harness.forceActivePlayer(player2);
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player2, List.of(lavaAxe));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.setLife(player1, 20);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passPriority(player2);

        // Activate Spellskite targeting Lava Axe
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, lavaAxe.getId());
        harness.passBothPriorities();

        // Spellskite's ability resolves, but Spellskite is not a legal target for Lava Axe
        // Resolve Lava Axe — it still targets player1
        harness.passBothPriorities();

        harness.assertLife(player1, 15);
    }

    @Test
    @DisplayName("Spellskite does nothing when targeting a spell with no targets")
    void doesNothingAgainstSpellWithNoTargets() {
        // Per ruling: "You can activate Spellskite's ability even if that spell or ability
        // has no targets. In this case, no targets are changed."
        Spellskite spellskite = new Spellskite();
        harness.addToBattlefield(player1, spellskite);

        // Cast a non-targeted spell (Counsel of the Soratami draws 2 cards, no target)
        harness.forceActivePlayer(player2);
        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setHand(player2, List.of(counsel));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.castSorcery(player2, 0, 0);
        harness.passPriority(player2);

        // Activation is legal — targets the spell on the stack
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, counsel.getId());
        harness.passBothPriorities();

        // Spellskite's ability resolves but does nothing (spell has no targets)
        assertThat(gd.gameLog).anyMatch(log -> log.contains("has no targets"));

        // Counsel of the Soratami still resolves normally
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Spellskite does nothing if target spell already targets Spellskite")
    void doesNothingIfAlreadyTargetingSpellskite() {
        Spellskite spellskite = new Spellskite();
        harness.addToBattlefield(player1, spellskite);
        UUID spellskitePermId = harness.getPermanentId(player1, "Spellskite");

        // Opponent casts Boomerang targeting Spellskite already
        harness.forceActivePlayer(player2);
        Boomerang boomerang = new Boomerang();
        harness.setHand(player2, List.of(boomerang));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player2, 0, spellskitePermId);
        harness.passPriority(player2);

        // Activate Spellskite targeting Boomerang
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, boomerang.getId());
        harness.passBothPriorities();

        // Resolve Boomerang — still targets Spellskite
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spellskite");
        assertThat(gd.gameLog).anyMatch(log -> log.contains("already targets"));
    }
}
