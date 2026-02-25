package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DarksteelSentinel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetAndControllerLosesLifePerCreatureDeathsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FleshAllergyTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Flesh Allergy has correct effects and targeting")
    void hasCorrectProperties() {
        FleshAllergy card = new FleshAllergy();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getTargetFilter()).isEqualTo(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ));
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(SacrificeCreatureCost.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(DestroyTargetAndControllerLosesLifePerCreatureDeathsEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Flesh Allergy sacrifices a creature and puts spell on stack")
    void castingSacrificesCreatureAndPutsOnStack() {
        Permanent sacrifice = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);

        Permanent target = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new FleshAllergy()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Flesh Allergy");
        assertThat(entry.getTargetPermanentId()).isEqualTo(target.getId());

        // Sacrificed creature should be gone from battlefield and in graveyard
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cannot cast Flesh Allergy without a creature to sacrifice")
    void cannotCastWithoutCreatureToSacrifice() {
        Permanent target = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new FleshAllergy()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void cannotSacrificeOpponentsCreature() {
        Permanent opponentCreature = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player2.getId()).add(opponentCreature);

        Permanent target = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new FleshAllergy()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, target.getId(), opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Resolving destroys target creature and controller loses life for creature deaths this turn")
    void resolvingDestroysTargetAndCausesLifeLoss() {
        Permanent sacrifice = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);

        Permanent target = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new FleshAllergy()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        // Target creature should be destroyed
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");

        // 2 creature deaths this turn: sacrificed Llanowar Elves + destroyed Grizzly Bears
        // Target's controller (player2) loses 2 life
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Life loss counts creature deaths from earlier in the turn")
    void lifeLossCountsEarlierDeathsThisTurn() {
        Permanent sacrifice = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);

        Permanent target = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(target);

        // Simulate a creature that died earlier this turn (e.g. from combat)
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.setHand(player1, List.of(new FleshAllergy()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        // 3 creature deaths: earlier death + sacrificed Llanowar Elves + destroyed Grizzly Bears
        // Target's controller (player2) loses 3 life
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Indestructible target is not destroyed but life loss still applies from sacrifice")
    void indestructibleTargetNotDestroyedButLifeLossApplies() {
        Permanent sacrifice = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);

        Permanent indestructibleTarget = new Permanent(new DarksteelSentinel());
        gd.playerBattlefields.get(player2.getId()).add(indestructibleTarget);

        harness.setHand(player1, List.of(new FleshAllergy()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithSacrifice(player1, 0, indestructibleTarget.getId(), sacrifice.getId());
        harness.passBothPriorities();

        // Target survives (indestructible)
        harness.assertOnBattlefield(player2, "Darksteel Sentinel");

        // 1 creature death: only the sacrificed Llanowar Elves
        // Target's controller (player2) loses 1 life
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Spell fizzles if target is removed before resolution — sacrifice still happens")
    void spellFizzlesIfTargetRemoved() {
        Permanent sacrifice = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);

        Permanent target = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new FleshAllergy()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());

        // Sacrifice already happened
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");

        // Remove target before resolution (simulating another removal spell)
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(target.getId()));

        harness.passBothPriorities();

        // Spell fizzles — no destroy, no life loss
        // Player 2 life should remain at 20
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can sacrifice the same type of creature you are targeting")
    void canSacrificeAndTargetSameType() {
        // Player 1 has two creatures, sacrifices one
        Permanent sacrifice = new Permanent(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(sacrifice);

        // Target is player 2's creature
        Permanent target = new Permanent(new LlanowarElves());
        gd.playerBattlefields.get(player2.getId()).add(target);

        harness.setHand(player1, List.of(new FleshAllergy()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");

        // 2 deaths: sacrificed + destroyed
        harness.assertLife(player2, 18);
    }
}
