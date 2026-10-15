package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SaguPummeler;
import com.github.laxika.magicalvibes.cards.s.SaguWildling;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HerdHeirloom.class, Forest.class, SaguPummeler.class, SaguWildling.class})
class HerdHeirloomTest extends BaseCardTest {

    @Test
    @DisplayName("Adds one creature-spell-only mana of the chosen color")
    void addsCreatureSpellOnlyMana() {
        Permanent heirloom = harness.addToBattlefieldAndReturn(player1, new HerdHeirloom());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(heirloom.isTapped()).isTrue();
        assertThat(pool.getCreatureSpellOnlyMana(ManaColor.GREEN)).isEqualTo(1);
        assertThat(pool.get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Grants trample and a temporary combat-damage draw trigger to a qualifying creature")
    void grantsTemporaryCombatDamageDraw() {
        Permanent heirloom = harness.addToBattlefieldAndReturn(player1, new HerdHeirloom());
        Permanent lowPowerCreature = addCreatureReady(player1, new SaguWildling());
        Permanent qualifyingCreature = addCreatureReady(player1, new SaguPummeler());
        Permanent opposingCreature = addCreatureReady(player2, new SaguPummeler());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 1, null, lowPowerCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 1, null, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.activateAbility(player1, 0, 1, null, qualifyingCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, qualifyingCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(heirloom.isTapped()).isTrue();

        declareAttackers(List.of(2));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, qualifyingCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Restricted mana pays for a creature spell")
    void restrictedManaPaysForCreature() {
        harness.addToBattlefield(player1, new HerdHeirloom());
        harness.setHand(player1, List.of(new SaguPummeler()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sagu Pummeler");
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyMana(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Restricted mana cannot pay for a noncreature spell")
    void restrictedManaCannotPayForArtifact() {
        harness.addToBattlefield(player1, new HerdHeirloom());
        harness.setHand(player1, List.of(new HerdHeirloom()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThatThrownBy(() -> harness.castArtifact(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellOnlyMana(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature below four power at resolution gains neither ability")
    void powerRestrictionIsRecheckedOnResolution() {
        harness.addToBattlefield(player1, new HerdHeirloom());
        Permanent creature = addCreatureReady(player1, new SaguPummeler());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        creature.setPowerModifier(-1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Dropping below four power after resolution does not remove the granted abilities")
    void powerDropAfterResolutionRetainsAbilities() {
        harness.addToBattlefield(player1, new HerdHeirloom());
        Permanent creature = addCreatureReady(player1, new SaguPummeler());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();
        creature.setPowerModifier(-1);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A boosted creature qualifies using its current power")
    void boostedCreatureQualifies() {
        harness.addToBattlefield(player1, new HerdHeirloom());
        Permanent creature = addCreatureReady(player1, new SaguWildling());
        creature.setPowerModifier(1);

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The granted draw ability expires at cleanup")
    void drawAbilityExpiresAtCleanup() {
        harness.addToBattlefield(player1, new HerdHeirloom());
        Permanent creature = addCreatureReady(player1, new SaguPummeler());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.activateAbility(player1, 0, 1, null, creature.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        harness.setHand(player1, List.of());
        creature.untap();
        declareAttackers(List.of(1));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
