package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AncestralBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
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

@CardUsed({Vault101BirthdayParty.class, GrizzlyBears.class, AncestralBlade.class, Pacifism.class})
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
        AncestralBlade equipment = new AncestralBlade();
        harness.setHand(player1, List.of(equipment));
        addSagaWithLore(1);

        triggerChapter();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.SearchLibraryAndOrGraveyardChoice.class)
                .validCardIds()).containsExactly(equipment.getId());
        harness.handleMultipleCardsChosen(player1, List.of(equipment.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent enteredEquipment = findPermanentByCardId(equipment.getId());
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

        Permanent enteredAura = findPermanentByCardId(aura.getId());
        assertThat(enteredAura).isNotNull();
        assertThat(enteredAura.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new Vault101BirthdayParty());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent findPermanentByCardId(java.util.UUID cardId) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(cardId))
                .findFirst()
                .orElse(null);
    }
}
