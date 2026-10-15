package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.p.PipBoy3000;
import com.github.laxika.magicalvibes.cards.w.WeatheredRunestone;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Vault101BirthdayParty.class, GrizzlyBears.class, PipBoy3000.class, WeatheredRunestone.class, Pacifism.class})
class Vault101BirthdayPartyTest extends BaseCardTest {

    @Test
    void chapterICreatesHumanSoldierAndFood() {
        addSagaWithLore(0);

        triggerChapter();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Human Soldier")).hasSize(1);
        assertThat(findPermanents(player1, "Human Soldier").getFirst().getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.SOLDIER);
        assertThat(findPermanents(player1, "Food")).hasSize(1);
    }

    @Test
    void chapterIIPutsEquipmentFromHandOntoBattlefieldAndMayAttachIt() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        PipBoy3000 equipment = new PipBoy3000();
        harness.setHand(player1, List.of(equipment));
        addSagaWithLore(1);

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class)
                .validCardIds()).containsExactly(equipment.getId());
        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));
        harness.handleMayAbilityChosen(player1, true);

        Permanent enteredEquipment = findPermanent(player1, "Pip-Boy 3000");
        assertThat(enteredEquipment).isNotNull();
        assertThat(enteredEquipment.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void chapterIIIPutsAuraFromGraveyardOntoBattlefieldAttachedToCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Pacifism aura = new Pacifism();
        harness.setGraveyard(player1, List.of(aura));
        addSagaWithLore(2);

        triggerChapter();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(creature.getId());
        harness.handlePermanentChosen(player1, creature.getId());

        Permanent enteredAura = findPermanent(player1, "Pacifism");
        assertThat(enteredAura).isNotNull();
        assertThat(enteredAura.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void chapterIIMayDeclinePuttingAnyCardOntoBattlefield() {
        PipBoy3000 equipment = new PipBoy3000();
        harness.setHand(player1, List.of(equipment));
        addSagaWithLore(1);

        triggerChapter();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).contains(equipment);
        harness.assertNotOnBattlefield(player1, "Pip-Boy 3000");
    }

    @Test
    void chapterIIIEquipmentMayEnterWithoutBeingAttached() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        PipBoy3000 equipment = new PipBoy3000();
        harness.setGraveyard(player1, List.of(equipment));
        addSagaWithLore(2);

        triggerChapter();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanent(player1, "Pip-Boy 3000").getAttachedTo()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(equipment);
        harness.assertInGraveyard(player1, "Vault 101: Birthday Party");
    }

    @Test
    void auraFromHandStaysInHandWhenNothingCanBeEnchanted() {
        Pacifism aura = new Pacifism();
        harness.setHand(player1, List.of(aura));
        addSagaWithLore(1);

        triggerChapter();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(aura);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(aura);
        harness.assertNotOnBattlefield(player1, "Pacifism");
    }

    @Test
    void auraFromHandCanEnchantAnOpponentsCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Pacifism aura = new Pacifism();
        harness.setHand(player1, List.of(aura));
        addSagaWithLore(1);

        triggerChapter();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(aura.getId()));
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(findPermanent(player1, "Pacifism").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(aura);
    }

    @Test
    void weatheredRunestoneDoesNotPreventEquipmentEnteringFromHand() {
        harness.addToBattlefield(player2, new WeatheredRunestone());
        PipBoy3000 equipment = new PipBoy3000();
        harness.setHand(player1, List.of(equipment));
        addSagaWithLore(1);

        triggerChapter();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Pip-Boy 3000");
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(equipment);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(equipment);
    }

    private void addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new Vault101BirthdayParty());
        saga.setCounterCount(CounterType.LORE, loreCounters);
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
