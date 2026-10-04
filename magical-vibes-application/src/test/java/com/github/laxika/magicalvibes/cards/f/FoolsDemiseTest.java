package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.k.KrosanGrip;
import com.github.laxika.magicalvibes.cards.m.MomentaryBlink;
import com.github.laxika.magicalvibes.cards.m.MakeshiftMannequin;
import com.github.laxika.magicalvibes.cards.s.StranglingSoot;
import com.github.laxika.magicalvibes.cards.w.WordOfSeizing;
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

@CardUsed({FoolsDemise.class, AshcoatBear.class, StranglingSoot.class, KrosanGrip.class,
        MomentaryBlink.class, MakeshiftMannequin.class, WordOfSeizing.class})
class FoolsDemiseTest extends BaseCardTest {

    @Test
    @DisplayName("When the enchanted creature dies, it returns under the Aura controller's control")
    void returnsCreatureUnderAuraControllersControl() {
        Permanent creature = addCreatureReady(player2, new AshcoatBear());
        Card creatureCard = creature.getCard();

        castFoolsDemise(player1, creature);
        killCreature(player1, creature);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creatureCard.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(creatureCard.getId()));
        harness.assertInHand(player1, "Fool's Demise");
        harness.assertNotInGraveyard(player1, "Fool's Demise");
    }

    @Test
    @DisplayName("When Fool's Demise is put into a graveyard from the battlefield, it returns to its owner's hand")
    void returnsToHandWhenAuraIsDestroyed() {
        Permanent creature = addCreatureReady(player1, new AshcoatBear());
        castFoolsDemise(player1, creature);
        Permanent aura = findPermanent(player1, "Fool's Demise");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new KrosanGrip()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.castInstant(player2, 0, aura.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Fool's Demise");
        harness.assertNotInGraveyard(player1, "Fool's Demise");
        harness.assertNotOnBattlefield(player1, "Fool's Demise");
    }

    @Test
    @DisplayName("A different creature's death does not trigger Fool's Demise")
    void doesNotTriggerForDifferentCreature() {
        Permanent enchantedCreature = addCreatureReady(player1, new AshcoatBear());
        Permanent otherCreature = addCreatureReady(player1, new AshcoatBear());
        Card otherCreatureCard = otherCreature.getCard();

        castFoolsDemise(player1, enchantedCreature);
        killCreature(player1, otherCreature);

        harness.assertOnBattlefield(player1, "Fool's Demise");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(enchantedCreature.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(otherCreatureCard.getId()));
    }

    @Test
    @DisplayName("Exiling the enchanted creature does not trigger Fool's Demise's death ability")
    void doesNotTriggerWhenEnchantedCreatureIsExiled() {
        Permanent creature = addCreatureReady(player1, new AshcoatBear());
        Card creatureCard = creature.getCard();

        castFoolsDemise(player1, creature);
        blinkCreature(player1, creature);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creatureCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creatureCard.getId()));
        harness.assertInHand(player1, "Fool's Demise");
    }

    @Test
    @DisplayName("Fool's Demise cannot enchant a non-creature permanent")
    void cannotEnchantNonCreature() {
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player2, new FoolsDemise());

        harness.setHand(player1, List.of(new FoolsDemise()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A returned opponent's creature still goes to its owner's graveyard when it dies again")
    void returnedCreatureRetainsItsOwner() {
        Permanent creature = addCreatureReady(player2, new AshcoatBear());
        Card creatureCard = creature.getCard();
        castFoolsDemise(player1, creature);
        killCreature(player1, creature);

        Permanent returnedCreature = findPermanent(player1, "Ashcoat Bear");
        killCreature(player1, returnedCreature);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creatureCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creatureCard);
        harness.assertNotOnBattlefield(player1, "Ashcoat Bear");
    }

    @Test
    @DisplayName("The death trigger cannot return a creature that left the graveyard and died again")
    void doesNotReturnCreatureFromALaterGraveyardVisit() {
        Permanent creature = addCreatureReady(player1, new AshcoatBear());
        Card creatureCard = creature.getCard();
        castFoolsDemise(player1, creature);
        harness.setHand(player1, List.of(new StranglingSoot()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.assertInGraveyard(player1, "Ashcoat Bear");

        harness.setHand(player1, List.of(new MakeshiftMannequin()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castAndResolveInstant(player1, 0, creatureCard.getId());
        Permanent returnedCreature = findPermanent(player1, "Ashcoat Bear");
        harness.setHand(player1, List.of(new StranglingSoot()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, returnedCreature.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Ashcoat Bear");
        harness.assertNotOnBattlefield(player1, "Ashcoat Bear");
        harness.assertInHand(player1, "Fool's Demise");
    }

    @Test
    @DisplayName("A stolen Aura's return trigger is controlled by its last controller, but returns to its owner")
    void stolenAuraTriggerBelongsToItsController() {
        Permanent creature = addCreatureReady(player1, new AshcoatBear());
        castFoolsDemise(player1, creature);
        Permanent aura = findPermanent(player1, "Fool's Demise");
        Card auraCard = aura.getCard();
        harness.setHand(player2, List.of(new WordOfSeizing()));
        harness.addMana(player2, ManaColor.RED, 5);
        harness.castAndResolveInstant(player2, 0, aura.getId());
        harness.assertOnBattlefield(player2, "Fool's Demise");

        harness.setHand(player2, List.of(new KrosanGrip()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player2, 0, aura.getId());

        assertThat(gd.stack).anySatisfy(entry -> {
            assertThat(entry.getCard().getId()).isEqualTo(auraCard.getId());
            assertThat(entry.getControllerId()).isEqualTo(player2.getId());
        });
        resolveAllTriggers();
        harness.assertInHand(player1, "Fool's Demise");
        harness.assertNotInHand(player2, "Fool's Demise");
    }

    @Test
    @DisplayName("The Aura returns only itself, leaving other copies in the graveyard")
    void returnsOnlyTheAuraThatDied() {
        Card otherAura = new FoolsDemise();
        harness.setGraveyard(player1, List.of(otherAura));
        Permanent creature = addCreatureReady(player1, new AshcoatBear());
        castFoolsDemise(player1, creature);
        Card attachedAura = findPermanent(player1, "Fool's Demise").getCard();

        killCreature(player1, creature);

        assertThat(gd.playerHands.get(player1.getId())).contains(attachedAura).doesNotContain(otherAura);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(otherAura).doesNotContain(attachedAura);
    }

    @Test
    @DisplayName("An Aura whose target leaves before resolution stays in the graveyard")
    void doesNotReturnAuraThatNeverEnteredBattlefield() {
        Permanent creature = addCreatureReady(player1, new AshcoatBear());
        harness.setHand(player1, List.of(new FoolsDemise(), new MomentaryBlink()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Fool's Demise");
        harness.assertNotInHand(player1, "Fool's Demise");
        harness.assertNotOnBattlefield(player1, "Fool's Demise");
        harness.assertOnBattlefield(player1, "Ashcoat Bear");
    }

    private void castFoolsDemise(Player controller, Permanent target) {
        harness.setHand(controller, List.of(new FoolsDemise()));
        harness.addMana(controller, ManaColor.BLUE, 5);
        harness.castEnchantment(controller, 0, target.getId());
        harness.passBothPriorities();
    }

    private void killCreature(Player caster, Permanent creature) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new StranglingSoot()));
        harness.addMana(caster, ManaColor.BLACK, 3);
        harness.castInstant(caster, 0, creature.getId());
        resolveAllTriggers();
    }

    private void blinkCreature(Player caster, Permanent creature) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(caster, List.of(new MomentaryBlink()));
        harness.addMana(caster, ManaColor.WHITE, 1);
        harness.addMana(caster, ManaColor.COLORLESS, 1);
        harness.castInstant(caster, 0, creature.getId());
        resolveAllTriggers();
    }
}
