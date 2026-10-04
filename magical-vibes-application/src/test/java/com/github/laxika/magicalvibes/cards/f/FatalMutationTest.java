package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AuraGraft;
import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
import com.github.laxika.magicalvibes.cards.t.TempleOfTheFalseGod;
import com.github.laxika.magicalvibes.cards.w.WipeClean;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FatalMutation.class, ScornfulEgotist.class, TempleOfTheFalseGod.class, WipeClean.class, AuraGraft.class})
class FatalMutationTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys the enchanted creature when it is turned face up")
    void destroysEnchantedCreatureWhenTurnedFaceUp() {
        Permanent creature = addFaceDownCreature();
        attachFatalMutation(creature);
        creature.setRegenerationShield(1);

        gs.turnPermanentFaceUpWithoutPayingManaCost(gd, creature);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Scornful Egotist");
        harness.assertInGraveyard(player2, "Scornful Egotist");
        harness.assertInGraveyard(player1, "Fatal Mutation");
    }

    @Test
    @DisplayName("Does not trigger when another permanent is turned face up")
    void ignoresAnotherPermanentTurningFaceUp() {
        Permanent enchantedCreature = addFaceDownCreature();
        Permanent otherCreature = addFaceDownCreature();
        attachFatalMutation(enchantedCreature);

        gs.turnPermanentFaceUpWithoutPayingManaCost(gd, otherCreature);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(enchantedCreature, otherCreature);
        harness.assertOnBattlefield(player1, "Fatal Mutation");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new TempleOfTheFalseGod());
        harness.setHand(player1, List.of(new FatalMutation()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanting an already face-up creature does not destroy it")
    void enchantingFaceUpCreatureDoesNotDestroyIt() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ScornfulEgotist());

        attachFatalMutation(creature);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        harness.assertOnBattlefield(player1, "Fatal Mutation");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing Fatal Mutation after it triggers does not save the creature")
    void removingAuraDoesNotStopTrigger() {
        Permanent creature = addFaceDownCreature();
        attachFatalMutation(creature);
        Permanent aura = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.turnFaceUp(player2, 0);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player2, List.of(new WipeClean()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, aura.getId());

        harness.assertNotOnBattlefield(player1, "Fatal Mutation");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Scornful Egotist");
    }

    @Test
    @DisplayName("Moving Fatal Mutation after it triggers still destroys the creature that turned face up")
    void movingAuraDoesNotChangeCreatureDestroyed() {
        Permanent creature = addFaceDownCreature();
        Permanent destination = addFaceDownCreature();
        attachFatalMutation(creature);
        Permanent aura = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.turnFaceUp(player2, 0);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player1, List.of(new AuraGraft()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, aura.getId());
        harness.handlePermanentChosen(player1, destination.getId());
        assertThat(aura.getAttachedTo()).isEqualTo(destination.getId());
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(destination).doesNotContain(creature);
        harness.assertInGraveyard(player2, "Scornful Egotist");
        harness.assertOnBattlefield(player1, "Fatal Mutation");
    }

    private Permanent addFaceDownCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ScornfulEgotist());
        creature.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        return creature;
    }

    private void attachFatalMutation(Permanent creature) {
        harness.setHand(player1, List.of(new FatalMutation()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
