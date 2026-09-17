package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
import com.github.laxika.magicalvibes.cards.t.TempleOfTheFalseGod;
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

@CardUsed({FatalMutation.class, ScornfulEgotist.class, TempleOfTheFalseGod.class})
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
