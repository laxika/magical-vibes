package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.d.DuskborneSkymarcher;
import com.github.laxika.magicalvibes.cards.k.KinjallisCaller;
import com.github.laxika.magicalvibes.cards.r.RangingRaptors;
import com.github.laxika.magicalvibes.cards.s.SkymarchBloodletter;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PillarOfOrigins.class, KinjallisCaller.class, RangingRaptors.class,
        SkymarchBloodletter.class, DuskborneSkymarcher.class})
class PillarOfOriginsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting and resolving Pillar prompts for creature type choice")
    void castingPromptsForSubtypeChoice() {
        harness.setHand(player1, List.of(new PillarOfOrigins()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Pillar of Origins");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Choosing a creature type sets chosenSubtype on the permanent")
    void choosingSubtypeSetsOnPermanent() {
        harness.setHand(player1, List.of(new PillarOfOrigins()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "VAMPIRE");

        Permanent pillar = findPermanent(player1, "Pillar of Origins");
        assertThat(pillar.getChosenSubtype()).isEqualTo(CardSubtype.VAMPIRE);
    }

    @Test
    @DisplayName("Tapping Pillar prompts for mana color choice")
    void tappingPromptsForColorChoice() {
        Permanent pillar = harness.addToBattlefieldAndReturn(player1, new PillarOfOrigins());
        pillar.setChosenSubtype(CardSubtype.DINOSAUR);

        harness.activateAbility(player1, 0, null, null);

        assertThat(pillar.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty(); // mana ability does not use the stack
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Choosing a color adds mana to the subtype creature mana pool")
    void choosingColorAddsRestrictedMana() {
        Permanent pillar = harness.addToBattlefieldAndReturn(player1, new PillarOfOrigins());
        pillar.setChosenSubtype(CardSubtype.MERFOLK);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        // Regular blue mana should NOT have increased
        assertThat(pool.get(ManaColor.BLUE)).isEqualTo(0);
        // Subtype creature mana for MERFOLK should have 1 blue
        assertThat(pool.getSubtypeCreatureManaForColor(java.util.Set.of(CardSubtype.MERFOLK), ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Mana from Pillar can be used to cast a creature spell of the chosen type")
    void manaCanCastCreatureOfChosenType() {
        // Set up Pillar with VAMPIRE chosen
        Permanent pillar = harness.addToBattlefieldAndReturn(player1, new PillarOfOrigins());
        pillar.setChosenSubtype(CardSubtype.VAMPIRE);

        // All three mana can be produced by Pillars choosing black.
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeCreatureMana(CardSubtype.VAMPIRE, ManaColor.BLACK, 3);

        harness.setHand(player1, List.of(new SkymarchBloodletter()));

        // Should be able to cast it using only the restricted mana
        harness.castCreature(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Skymarch Bloodletter");
    }

    @Test
    @DisplayName("Mana from Pillar cannot be used to cast a creature spell of a different type")
    void manaCannotCastCreatureOfDifferentType() {
        Permanent pillar = harness.addToBattlefieldAndReturn(player1, new PillarOfOrigins());
        pillar.setChosenSubtype(CardSubtype.VAMPIRE);

        // Add subtype creature mana (restricted to Vampire)
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeCreatureMana(CardSubtype.VAMPIRE, ManaColor.WHITE, 1);

        harness.setHand(player1, List.of(new KinjallisCaller()));

        // Should NOT be able to cast — the only white mana available is restricted to Vampires
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mana from Pillar cannot be used to cast a non-creature spell")
    void manaCannotCastNonCreatureSpell() {
        Permanent pillar = harness.addToBattlefieldAndReturn(player1, new PillarOfOrigins());
        pillar.setChosenSubtype(CardSubtype.VAMPIRE);

        // Add subtype creature mana (restricted to Vampire creatures)
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeCreatureMana(CardSubtype.VAMPIRE, ManaColor.RED, 2);

        harness.setHand(player1, List.of(new PillarOfOrigins()));

        // Should NOT be able to cast — it's not a creature spell
        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Restricted mana supplements regular mana for casting creature of chosen type")
    void restrictedManaSupplementsRegularMana() {
        Permanent pillar = harness.addToBattlefieldAndReturn(player1, new PillarOfOrigins());
        pillar.setChosenSubtype(CardSubtype.DINOSAUR);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        // Green pays the colored symbol; restricted blue pays generic mana.
        pool.add(ManaColor.GREEN, 1);
        pool.addSubtypeCreatureMana(CardSubtype.DINOSAUR, ManaColor.BLUE, 2);

        harness.setHand(player1, List.of(new RangingRaptors()));

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Ranging Raptors");
    }

    @Test
    @DisplayName("Subtype creature mana drains at step/phase transitions")
    void subtypeManaHasDrainBehavior() {
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSubtypeCreatureMana(CardSubtype.VAMPIRE, ManaColor.WHITE, 2);

        assertThat(pool.getSubtypeCreatureManaForColor(java.util.Set.of(CardSubtype.VAMPIRE), ManaColor.WHITE)).isEqualTo(2);

        pool.drainNonPersistent();

        assertThat(pool.getSubtypeCreatureManaForColor(java.util.Set.of(CardSubtype.VAMPIRE), ManaColor.WHITE)).isEqualTo(0);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("Pillar produces exactly one restricted mana in each of the five colors")
    void producesEachColor(ManaColor color) {
        Permanent pillar = harness.addToBattlefieldAndReturn(player1, new PillarOfOrigins());
        pillar.setChosenSubtype(CardSubtype.VAMPIRE);

        harness.activateAbility(player1, 0, null, null);
        PendingInteraction.ColorChoice choice = (PendingInteraction.ColorChoice) gd.interaction.activeInteraction();
        assertThat(choice.options()).containsExactlyInAnyOrder("WHITE", "BLUE", "BLACK", "RED", "GREEN");
        harness.handleListChoice(player1, color.name());

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getSubtypeCreatureManaTotal(java.util.Set.of(CardSubtype.VAMPIRE))).isEqualTo(1);
        assertThat(pool.getSubtypeCreatureManaForColor(java.util.Set.of(CardSubtype.VAMPIRE), color)).isEqualTo(1);
        assertThat(pool.get(color)).isZero();
        assertThat(pillar.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A newly resolved Pillar can immediately pay for a creature's second subtype")
    void newlyResolvedPillarCastsCreatureMatchingSecondSubtype() {
        harness.setHand(player1, List.of(new PillarOfOrigins(), new KinjallisCaller()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "CLERIC");

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kinjalli's Caller");
        assertThat(gd.playerManaPools.get(player1.getId())
                .getSubtypeCreatureManaTotal(java.util.Set.of(CardSubtype.CLERIC))).isZero();
        assertThat(findPermanent(player1, "Pillar of Origins").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Pillar mana cannot pay for an activated ability even on a matching creature")
    void cannotPayForMatchingCreatureAbility() {
        Permanent pillar = harness.addToBattlefieldAndReturn(player1, new PillarOfOrigins());
        pillar.setChosenSubtype(CardSubtype.VAMPIRE);
        Permanent skymarcher = harness.addToBattlefieldAndReturn(player1, new DuskborneSkymarcher());
        skymarcher.setSummoningSick(false);
        skymarcher.setAttacking(true);
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, skymarcher.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(skymarcher.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 1, null, skymarcher.getId());

        assertThat(skymarcher.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getSubtypeCreatureManaTotal(java.util.Set.of(CardSubtype.VAMPIRE))).isEqualTo(1);
    }

    @Test
    @DisplayName("An already tapped Pillar cannot produce more mana")
    void cannotActivateTwiceWithoutUntapping() {
        Permanent pillar = harness.addToBattlefieldAndReturn(player1, new PillarOfOrigins());
        pillar.setChosenSubtype(CardSubtype.VAMPIRE);
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getSubtypeCreatureManaTotal(java.util.Set.of(CardSubtype.VAMPIRE))).isEqualTo(1);
    }
}
