package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PlanarCleansing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MinionsReturn.class, DoomBlade.class, GrizzlyBears.class, PlanarCleansing.class})
class MinionsReturnTest extends BaseCardTest {

    @Test
    @DisplayName("When the enchanted creature dies, it returns under the Aura controller's control")
    void returnsUnderAuraControllersControl() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Card creatureCard = creature.getCard();

        castMinionsReturn(player1, creature);
        killCreature(creature);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(creatureCard.getId()));
    }

    @Test
    @DisplayName("The Aura's own creature comes back under its controller too")
    void returnsOwnCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Card creatureCard = creature.getCard();

        castMinionsReturn(player1, creature);
        killCreature(creature);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
    }

    @Test
    @DisplayName("Minion's Return goes to the graveyard when the enchanted creature dies")
    void auraGoesToGraveyardOnDeath() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        castMinionsReturn(player1, creature);
        killCreature(creature);

        harness.assertInGraveyard(player1, "Minion's Return");
        harness.assertNotOnBattlefield(player1, "Minion's Return");
    }

    @Test
    @DisplayName("Minion's Return cannot enchant a non-creature permanent")
    void cannotEnchantNonCreature() {
        Permanent nonCreature = new Permanent(new MinionsReturn());
        gd.playerBattlefields.get(player2.getId()).add(nonCreature);

        harness.setHand(player1, List.of(new MinionsReturn()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castMinionsReturn(Player controller, Permanent target) {
        harness.setHand(controller, List.of(new MinionsReturn()));
        harness.addMana(controller, ManaColor.BLACK, 3);

        harness.castEnchantment(controller, 0, target.getId());
        harness.passBothPriorities();
    }

    private void killCreature(Permanent creature) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Flash allows casting the Aura during the opponent's turn")
    void castsDuringOpponentsTurn() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.ensurePriority(player1);

        castMinionsReturn(player1, creature);

        assertThat(findPermanent(player1, "Minion's Return").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("The returned creature is a fresh untapped permanent and dies normally a second time")
    void returnsFreshPermanentAndDoesNotReturnAgain() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.setTapped(true);
        Card creatureCard = creature.getCard();
        castMinionsReturn(player1, creature);

        killCreature(creature);

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getId()).isNotEqualTo(creature.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.isSummoningSick()).isTrue();
        assertThat(returned.getAttachedTo()).isNull();

        killCreature(returned);

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(creatureCard.getId()));
    }

    @Test
    @DisplayName("The creature returns when the Aura and creature are destroyed simultaneously")
    void returnsWhenAuraAndCreatureDieSimultaneously() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        castMinionsReturn(player1, creature);
        Permanent aura = findPermanent(player1, "Minion's Return");
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerBattlefields.get(player1.getId()).addFirst(aura);

        harness.castFromHand(player1, new PlanarCleansing(), "{3}{W}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Minion's Return");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }
}
