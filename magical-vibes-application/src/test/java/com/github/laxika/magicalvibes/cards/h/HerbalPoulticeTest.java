package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.w.WoodlandChangeling;
import com.github.laxika.magicalvibes.cards.t.Tarfire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HerbalPoultice.class, WoodlandChangeling.class, Tarfire.class})
class HerbalPoulticeTest extends BaseCardTest {

    @Test
    @DisplayName("Activating targets a creature and sacrifices the artifact as a cost")
    void activatingTargetsCreatureAndSacrifices() {
        Permanent poultice = addCreatureReady(player1, new HerbalPoultice());
        Permanent bears = addCreatureReady(player1, new WoodlandChangeling());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bears.getId());
        // Sacrifice is a cost, so the artifact leaves the battlefield immediately.
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(poultice);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c == poultice.getCard());
    }

    @Test
    @DisplayName("Resolving grants a regeneration shield to the target creature")
    void resolvingGrantsShield() {
        addCreatureReady(player1, new HerbalPoultice());
        Permanent bears = addCreatureReady(player1, new WoodlandChangeling());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new HerbalPoultice());
        Permanent otherArtifact = addCreatureReady(player1, new HerbalPoultice());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, otherArtifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new HerbalPoultice());
        Permanent bears = addCreatureReady(player1, new WoodlandChangeling());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Can regenerate an opponent's creature without tapping it on resolution")
    void canRegenerateOpponentsCreature() {
        harness.addToBattlefield(player1, new HerbalPoultice());
        Permanent creature = addCreatureReady(player2, new WoodlandChangeling());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getRegenerationShield()).isEqualTo(1);
        assertThat(creature.isTapped()).isFalse();
        harness.assertOnBattlefield(player2, "Woodland Changeling");
    }

    @Test
    @DisplayName("Regeneration replaces lethal damage once and clears the damage")
    void shieldReplacesLethalDamageOnce() {
        harness.addToBattlefield(player1, new HerbalPoultice());
        Permanent creature = addCreatureReady(player1, new WoodlandChangeling());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Tarfire(), new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertOnBattlefield(player1, "Woodland Changeling");
        assertThat(creature.isTapped()).isTrue();
        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(creature.getRegenerationShield()).isZero();

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player1, "Woodland Changeling");
        harness.assertInGraveyard(player1, "Woodland Changeling");
    }
    @Test
    @DisplayName("A tapped artifact can activate and remains sacrificed when its target dies in response")
    void targetDyingInResponseDoesNotRefundSacrifice() {
        Permanent poultice = harness.addToBattlefieldAndReturn(player1, new HerbalPoultice());
        poultice.tap();
        Permanent creature = addCreatureReady(player1, new WoodlandChangeling());
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Woodland Changeling");
        harness.assertInGraveyard(player1, "Woodland Changeling");
        harness.assertInGraveyard(player1, "Herbal Poultice");
        harness.assertNotOnBattlefield(player1, "Herbal Poultice");
        assertThat(creature.getRegenerationShield()).isZero();
    }
}