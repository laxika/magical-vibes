package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MarkOfTheOniTest extends BaseCardTest {

    private Card demon(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSubtypes(List.of(CardSubtype.DEMON));
        card.setPower(4);
        card.setToughness(4);
        return card;
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent castOn(Permanent target) {
        harness.setHand(player1, List.of(new MarkOfTheOni()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        return findPermanent(player1, "Mark of the Oni");
    }

    @Test
    @DisplayName("Resolving Mark of the Oni steals the enchanted creature")
    void stealsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        Permanent aura = castOn(creature);

        assertThat(aura.isAttached()).isTrue();
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Sacrifices itself at end step when controller has no Demons")
    void sacrificesWithoutDemon() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        castOn(creature);

        advanceToEndStep();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mark of the Oni");
        // Control reverts to the owner once the Aura is gone.
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Does not trigger while the controller has a Demon")
    void noTriggerWithDemon() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        castOn(creature);
        harness.addToBattlefield(player1, demon("Pit Demon"));

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Mark of the Oni");
    }

    @Test
    @DisplayName("An enchanted Demon you now control keeps the Aura around")
    void enchantedDemonCounts() {
        Permanent enemyDemon = addCreatureReady(player2, demon("Oni Overlord"));
        castOn(enemyDemon);

        advanceToEndStep();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Mark of the Oni");
    }

    @Test
    @DisplayName("An opponent's Demon does not prevent the sacrifice")
    void opponentDemonDoesNotHelp() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        castOn(creature);
        harness.addToBattlefield(player2, demon("Enemy Demon"));

        advanceToEndStep();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mark of the Oni");
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.f.FountainOfYouth());
        harness.setHand(player1, List.of(new MarkOfTheOni()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
}
