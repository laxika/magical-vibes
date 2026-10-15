package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BogRaiders;
import com.github.laxika.magicalvibes.cards.c.Cathodion;
import com.github.laxika.magicalvibes.cards.g.GorillaWarrior;
import com.github.laxika.magicalvibes.cards.t.TolarianAcademy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Expunge.class, BogRaiders.class, Cathodion.class, GorillaWarrior.class,
        TolarianAcademy.class})
class ExpungeTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a nonartifact, nonblack creature and prevents regeneration")
    void destroysValidCreatureWithoutRegeneration() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GorillaWarrior());
        creature.setRegenerationShield(1);

        castExpunge(creature);

        harness.assertNotOnBattlefield(player2, "Gorilla Warrior");
        harness.assertInGraveyard(player2, "Gorilla Warrior");
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BogRaiders());

        assertThatThrownBy(() -> castExpunge(creature))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Cathodion());

        assertThatThrownBy(() -> castExpunge(creature))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TolarianAcademy());

        assertThatThrownBy(() -> castExpunge(land))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cycling discards Expunge and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new Expunge()));
        harness.setLibrary(player1, List.of(new GorillaWarrior()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Expunge");
        harness.assertInHand(player1, "Gorilla Warrior");
    }

    @Test
    @DisplayName("Can destroy a creature controlled by the caster")
    void destroysOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GorillaWarrior());

        castExpunge(creature);

        harness.assertNotOnBattlefield(player1, "Gorilla Warrior");
        harness.assertInGraveyard(player1, "Gorilla Warrior");
    }

    @Test
    @DisplayName("Cycling discards as a cost and draws only on resolution")
    void cyclingDiscardsBeforeDrawing() {
        harness.setHand(player1, List.of(new Expunge()));
        harness.setLibrary(player1, List.of(new GorillaWarrior()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertNotInHand(player1, "Expunge");
        harness.assertInGraveyard(player1, "Expunge");
        harness.assertNotInHand(player1, "Gorilla Warrior");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Gorilla Warrior");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling cannot be activated with only one mana")
    void cyclingRequiresTwoMana() {
        harness.setHand(player1, List.of(new Expunge()));
        harness.setLibrary(player1, List.of(new GorillaWarrior()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Expunge");
        harness.assertNotInGraveyard(player1, "Expunge");
        harness.assertNotInHand(player1, "Gorilla Warrior");
        assertThat(gd.stack).isEmpty();
    }

    private void castExpunge(Permanent target) {
        harness.setHand(player1, List.of(new Expunge()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
