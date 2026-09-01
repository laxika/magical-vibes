package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ChitteringHost;
import com.github.laxika.magicalvibes.cards.d.DeathSpark;
import com.github.laxika.magicalvibes.cards.e.Exile;
import com.github.laxika.magicalvibes.cards.g.GrafRats;
import com.github.laxika.magicalvibes.cards.m.MidnightScavengers;
import com.github.laxika.magicalvibes.cards.s.SwampMosquito;
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

@CardUsed({
        FalseDemise.class,
        DeathSpark.class,
        Exile.class,
        SwampMosquito.class,
        GrafRats.class,
        MidnightScavengers.class,
        ChitteringHost.class
})
class FalseDemiseTest extends BaseCardTest {

    @Test
    @DisplayName("When your own enchanted creature dies, it returns to the battlefield under your control")
    void returnsOwnCreatureUnderYourControl() {
        Permanent creature = addCreatureReady(player1, new SwampMosquito());
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
        Permanent creature = addCreatureReady(player2, new SwampMosquito());
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
        Permanent creature = addCreatureReady(player2, new SwampMosquito());

        castFalseDemise(player1, creature);
        killCreature(player1, creature);

        harness.assertInGraveyard(player1, "False Demise");
        harness.assertNotOnBattlefield(player1, "False Demise");
    }

    @Test
    @DisplayName("A different creature's death does not trigger False Demise")
    void doesNotTriggerForDifferentCreature() {
        Permanent enchantedCreature = addCreatureReady(player1, new SwampMosquito());
        Permanent otherCreature = addCreatureReady(player1, new SwampMosquito());
        Card enchantedCreatureCard = enchantedCreature.getCard();
        Card otherCreatureCard = otherCreature.getCard();

        castFalseDemise(player1, enchantedCreature);
        killCreature(player1, otherCreature);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(enchantedCreatureCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(otherCreatureCard.getId()));
        harness.assertOnBattlefield(player1, "False Demise");
    }

    @Test
    @DisplayName("Exiling the enchanted creature does not trigger False Demise")
    void doesNotTriggerWhenEnchantedCreatureIsExiled() {
        Permanent creature = addCreatureReady(player1, new SwampMosquito());
        Card creatureCard = creature.getCard();

        castFalseDemise(player1, creature);

        creature.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Exile()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(creatureCard.getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creatureCard.getId()));
        harness.assertInGraveyard(player1, "False Demise");
    }

    @Test
    @DisplayName("When an enchanted melded creature dies, both component cards return")
    void returnsBothMeldComponents() {
        harness.addToBattlefieldAndReturn(player1, new GrafRats());
        harness.addToBattlefieldAndReturn(player1, new MidnightScavengers());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        resolveAllTriggers();

        Permanent host = findPermanent(player1, "Chittering Host");
        Card grafRatsCard = host.getMeldComponentCards().get(0);
        Card scavengersCard = host.getMeldComponentCards().get(1);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        castFalseDemise(player1, host);

        host.setMarkedDamage(100);
        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(grafRatsCard.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(scavengersCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(grafRatsCard.getId())
                        || c.getId().equals(scavengersCard.getId()));
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
        harness.setHand(caster, List.of(new DeathSpark()));
        harness.addMana(caster, ManaColor.RED, 2);
        harness.castInstant(caster, 0, creature.getId());
        resolveAllTriggers();
    }
}
