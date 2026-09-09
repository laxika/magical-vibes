package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FatalBlow;
import com.github.laxika.magicalvibes.cards.r.RedwoodTreefolk;
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

@CardUsed({Abduction.class, FatalBlow.class, RedwoodTreefolk.class})
class AbductionTest extends BaseCardTest {

    // ===== Gaining control =====

    @Test
    @DisplayName("Resolving Abduction steals the enchanted creature")
    void stealsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new RedwoodTreefolk());

        castAbduction(player1, creature);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.stolenCreatures).containsEntry(creature.getId(), player2.getId());
    }

    // ===== ETB untap =====

    @Test
    @DisplayName("Abduction untaps the enchanted creature as it enters")
    void untapsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new RedwoodTreefolk());
        creature.tap();

        castAbduction(player1, creature);

        assertThat(creature.isTapped()).isFalse();
    }

    // ===== Death trigger: return under owner's control =====

    @Test
    @DisplayName("When the enchanted creature dies, it returns to the battlefield under its owner's control")
    void returnsToOwnerBattlefieldOnDeath() {
        Permanent creature = addCreatureReady(player2, new RedwoodTreefolk());
        Card creatureCard = creature.getCard();

        // Player 1 steals the creature with Abduction
        castAbduction(player1, creature);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));

        // The controller kills the stolen creature
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.permanentsDealtDamageThisTurn.add(creature.getId());
        harness.setHand(player1, List.of(new FatalBlow()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId()); // creature dies, trigger goes on stack
        harness.passBothPriorities(); // resolve the return trigger

        // The creature returns to its owner (player2), not the aura's controller (player1)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(creatureCard.getId()));
    }

    @Test
    @DisplayName("Abduction itself goes to its owner's graveyard when the enchanted creature dies")
    void auraGoesToGraveyardOnDeath() {
        Permanent creature = addCreatureReady(player2, new RedwoodTreefolk());

        castAbduction(player1, creature);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        gd.permanentsDealtDamageThisTurn.add(creature.getId());
        harness.setHand(player1, List.of(new FatalBlow()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve trigger

        harness.assertInGraveyard(player1, "Abduction");
        harness.assertNotOnBattlefield(player1, "Abduction");
    }

    @Test
    @DisplayName("When Abduction leaves the battlefield, the enchanted creature returns to its previous controller")
    void returnsControlWhenAuraLeavesBattlefield() {
        Permanent creature = addCreatureReady(player2, new RedwoodTreefolk());

        castAbduction(player1, creature);
        Permanent aura = findPermanent(player1, "Abduction");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, aura));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        harness.assertInGraveyard(player1, "Abduction");
    }

    @Test
    @DisplayName("When the enchanted creature is exiled, Abduction does not return it")
    void doesNotReturnExiledCreature() {
        Permanent creature = addCreatureReady(player2, new RedwoodTreefolk());
        Card creatureCard = creature.getCard();

        castAbduction(player1, creature);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, creature));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
        assertThat(gd.findExiledCard(creatureCard.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getId().equals(creatureCard.getId()));
    }

    // ===== Target validation =====

    @Test
    @DisplayName("Abduction cannot enchant a non-creature permanent")
    void cannotEnchantNonCreature() {
        // Put an enchantment (non-creature) permanent on the battlefield to target
        Permanent nonCreature = new Permanent(new Abduction());
        gd.playerBattlefields.get(player2.getId()).add(nonCreature);

        harness.setHand(player1, List.of(new Abduction()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Helpers =====

    private void castAbduction(Player controller, Permanent target) {
        harness.setHand(controller, List.of(new Abduction()));
        harness.addMana(controller, ManaColor.BLUE, 4);

        harness.castEnchantment(controller, 0, target.getId());
        harness.passBothPriorities(); // resolve the aura (attach + gain control)
        harness.passBothPriorities(); // resolve the ETB untap trigger
    }
}
