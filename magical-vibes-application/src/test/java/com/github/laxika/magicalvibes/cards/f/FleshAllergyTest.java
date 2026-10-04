package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BlightMamba;
import com.github.laxika.magicalvibes.cards.c.CopperMyr;
import com.github.laxika.magicalvibes.cards.d.DarksteelSentinel;
import com.github.laxika.magicalvibes.cards.m.MoriokReaver;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FleshAllergy.class, CopperMyr.class, MoriokReaver.class, DarksteelSentinel.class, BlightMamba.class})
class FleshAllergyTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Flesh Allergy sacrifices a creature and puts spell on stack")
    void castingSacrificesCreatureAndPutsOnStack() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CopperMyr());

        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoriokReaver());

        harness.setHand(player1, List.of(new FleshAllergy()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());

        // Sacrificed creature should be gone from battlefield and in graveyard
        harness.assertNotOnBattlefield(player1, "Copper Myr");
        harness.assertInGraveyard(player1, "Copper Myr");
    }

    @Test
    @DisplayName("Cannot cast Flesh Allergy without a creature to sacrifice")
    void cannotCastWithoutCreatureToSacrifice() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoriokReaver());

        harness.setHand(player1, List.of(new FleshAllergy()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, target.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void cannotSacrificeOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new CopperMyr());

        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoriokReaver());

        harness.setHand(player1, List.of(new FleshAllergy()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, target.getId(), opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    @DisplayName("Resolving destroys target creature and controller loses life for creature deaths this turn")
    void resolvingDestroysTargetAndCausesLifeLoss() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CopperMyr());

        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoriokReaver());

        harness.setHand(player1, List.of(new FleshAllergy()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        // Target creature should be destroyed
        harness.assertNotOnBattlefield(player2, "Moriok Reaver");
        harness.assertInGraveyard(player2, "Moriok Reaver");

        // 2 creature deaths this turn: sacrificed Copper Myr + destroyed Moriok Reaver
        // Target's controller (player2) loses 2 life
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Life loss counts creature deaths from earlier in the turn")
    void lifeLossCountsEarlierDeathsThisTurn() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CopperMyr());

        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoriokReaver());

        // Simulate a creature that died earlier this turn (e.g. from combat)
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.setHand(player1, List.of(new FleshAllergy()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        // 3 creature deaths: earlier death + sacrificed Copper Myr + destroyed Moriok Reaver
        // Target's controller (player2) loses 3 life
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("Indestructible target is not destroyed but life loss still applies from sacrifice")
    void indestructibleTargetNotDestroyedButLifeLossApplies() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CopperMyr());

        Permanent indestructibleTarget = harness.addToBattlefieldAndReturn(player2, new DarksteelSentinel());

        harness.setHand(player1, List.of(new FleshAllergy()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithSacrifice(player1, 0, indestructibleTarget.getId(), sacrifice.getId());
        harness.passBothPriorities();

        // Target survives (indestructible)
        harness.assertOnBattlefield(player2, "Darksteel Sentinel");

        // 1 creature death: only the sacrificed Copper Myr
        // Target's controller (player2) loses 1 life
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Spell fizzles if target is removed before resolution — sacrifice still happens")
    void spellFizzlesIfTargetRemoved() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CopperMyr());

        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoriokReaver());

        harness.setHand(player1, List.of(new FleshAllergy()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());

        // Sacrifice already happened
        harness.assertNotOnBattlefield(player1, "Copper Myr");

        // Remove target before resolution (simulating another removal spell)
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(target.getId()));

        harness.passBothPriorities();

        // Spell fizzles — no destroy, no life loss
        // Player 2 life should remain at 20
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can sacrifice and target different creature types")
    void canSacrificeAndTargetDifferentTypes() {
        // Player 1 sacrifices a creature of a different type from the target
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new MoriokReaver());

        // Target is player 2's creature
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CopperMyr());

        harness.setHand(player1, List.of(new FleshAllergy()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Moriok Reaver");
        harness.assertNotOnBattlefield(player2, "Copper Myr");

        // 2 deaths: sacrificed + destroyed
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Can target and sacrifice the same creature, so no life is lost")
    void canTargetAndSacrificeSameCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CopperMyr());
        harness.setHand(player1, List.of(new FleshAllergy()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithSacrifice(player1, 0, creature.getId(), creature.getId());

        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Copper Myr");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Flesh Allergy");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Targeting your own creature makes you lose life")
    void ownTargetControllerLosesLife() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CopperMyr());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MoriokReaver());
        harness.setHand(player1, List.of(new FleshAllergy()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Copper Myr");
        harness.assertInGraveyard(player1, "Moriok Reaver");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An indestructible creature can be sacrificed and counts as a death")
    void canSacrificeIndestructibleCreature() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelSentinel());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoriokReaver());
        harness.setHand(player1, List.of(new FleshAllergy()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Darksteel Sentinel");
        harness.assertInGraveyard(player1, "Darksteel Sentinel");
        harness.assertInGraveyard(player2, "Moriok Reaver");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Regeneration prevents destruction but does not prevent life loss")
    void regeneratedTargetStillCausesLifeLoss() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new CopperMyr());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BlightMamba());
        harness.setHand(player1, List.of(new FleshAllergy()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), sacrifice.getId());
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Blight Mamba");
        assertThat(target.isTapped()).isTrue();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }
}
