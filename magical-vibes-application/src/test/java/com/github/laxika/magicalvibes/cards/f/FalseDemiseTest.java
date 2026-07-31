package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FalseDemiseTest extends BaseCardTest {

    @Test
    @DisplayName("When your own enchanted creature dies, it returns to the battlefield under your control")
    void returnsOwnCreatureUnderYourControl() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Card creatureCard = creature.getCard();

        castFalseDemise(player1, creature);
        killCreature(player1, creature);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creatureCard.getId()));
        assertThat(gd.stolenCreatures).doesNotContainKey(creatureCard.getId());
    }

    @Test
    @DisplayName("When an opponent's enchanted creature dies, it returns under the Aura controller's control")
    void returnsOpponentCreatureUnderYourControl() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Card creatureCard = creature.getCard();

        castFalseDemise(player1, creature);
        killCreature(player1, creature);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(creatureCard.getId()));

        // The control change has to stick, so the returned permanent is tracked as stolen from its owner.
        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(creatureCard.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(gd.stolenCreatures).containsEntry(returned.getId(), player2.getId());
    }

    @Test
    @DisplayName("False Demise goes to its owner's graveyard when the enchanted creature dies")
    void auraGoesToGraveyardOnDeath() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        castFalseDemise(player1, creature);
        killCreature(player1, creature);

        harness.assertInGraveyard(player1, "False Demise");
        harness.assertNotOnBattlefield(player1, "False Demise");
    }

    @Test
    @DisplayName("False Demise cannot enchant a non-creature permanent")
    void cannotEnchantNonCreature() {
        Permanent nonCreature = new Permanent(new FalseDemise());
        gd.playerBattlefields.get(player2.getId()).add(nonCreature);

        harness.setHand(player1, List.of(new FalseDemise()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castFalseDemise(Player controller, Permanent target) {
        harness.setHand(controller, List.of(new FalseDemise()));
        harness.addMana(controller, ManaColor.BLUE, 3);

        harness.castEnchantment(controller, 0, target.getId());
        harness.passBothPriorities();
    }

    private void killCreature(Player caster, Permanent creature) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new DoomBlade()));
        harness.addMana(caster, ManaColor.BLACK, 2);
        harness.castInstant(caster, 0, creature.getId());
        harness.passBothPriorities(); // resolve Doom Blade — creature dies, trigger goes on stack
        harness.passBothPriorities(); // resolve the return trigger
    }
}
