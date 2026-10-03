package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DeconstructionHammer;
import com.github.laxika.magicalvibes.cards.g.GoldfuryStrider;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AncestorsAid.class, GoldfuryStrider.class, DeconstructionHammer.class})
class AncestorsAidTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts a target creature, grants first strike, and creates a Treasure")
    void boostsCreatureAndCreatesTreasure() {
        harness.addToBattlefield(player1, new GoldfuryStrider());
        harness.setHand(player1, List.of(new AncestorsAid()));
        addMana();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Goldfury Strider"));

        Permanent bear = findPermanent(player1, "Goldfury Strider");
        assertThat(bear.getPowerModifier()).isEqualTo(2);
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(bear.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    @DisplayName("The pump and first strike wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GoldfuryStrider());
        harness.setHand(player1, List.of(new AncestorsAid()));
        addMana();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Goldfury Strider"));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Goldfury Strider");
        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
        assertThat(bear.hasKeyword(Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new DeconstructionHammer());
        harness.setHand(player1, List.of(new AncestorsAid()));
        addMana();

        UUID targetId = harness.getPermanentId(player1, "Deconstruction Hammer");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Targeting an opponent's creature still gives the caster the Treasure")
    void canTargetOpponentsCreature() {
        harness.addToBattlefield(player2, new GoldfuryStrider());
        harness.setHand(player1, List.of(new AncestorsAid()));
        addMana();

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Goldfury Strider"));

        Permanent creature = findPermanent(player2, "Goldfury Strider");
        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(creature.hasKeyword(Keyword.FIRST_STRIKE)).isTrue();
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Creates no Treasure when the target leaves before resolution")
    void createsNoTreasureWhenTargetLeaves() {
        harness.addToBattlefield(player1, new GoldfuryStrider());
        harness.setHand(player1, List.of(new AncestorsAid()));
        addMana();
        Permanent creature = findPermanent(player1, "Goldfury Strider");

        harness.castInstant(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.assertInGraveyard(player1, "Ancestors' Aid");
        assertThat(gd.stack).isEmpty();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
