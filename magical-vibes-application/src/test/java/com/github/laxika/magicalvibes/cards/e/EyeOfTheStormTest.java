package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EyeOfTheStorm.class, Divination.class, Opt.class})
class EyeOfTheStormTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a cast instant or sorcery and offers its copy")
    void exilesCastSpellAndOffersCopy() {
        UUID eyeId = addEye();
        Divination divination = new Divination();
        harness.setHand(player1, List.of(divination));
        addDivinationMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(eyeId).stream().map(Card::getId))
                .containsExactly(divination.getId());
        PendingInteraction.EyeOfTheStormCastChoice choice =
                (PendingInteraction.EyeOfTheStormCastChoice) gd.interaction.activeInteraction();
        assertThat(choice.validCopyIds()).hasSize(1);

        harness.handleMultipleCardsChosen(player1, choice.validCopyIds());

        assertThat(gd.stack).anyMatch(entry -> entry.isCopy()
                && entry.getCard().getName().equals("Divination"));
    }

    @Test
    @DisplayName("Copies all tracked spells in the order chosen by the caster")
    void copiesAllTrackedSpellsInChosenOrder() {
        UUID eyeId = addEye();
        Divination divination = new Divination();
        harness.setHand(player1, List.of(divination));
        addDivinationMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        PendingInteraction.EyeOfTheStormCastChoice firstChoice =
                (PendingInteraction.EyeOfTheStormCastChoice) gd.interaction.activeInteraction();
        harness.handleMultipleCardsChosen(player1, firstChoice.validCopyIds());
        harness.passBothPriorities();

        Opt opt = new Opt();
        harness.setHand(player1, List.of(opt));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getCardsExiledByPermanent(eyeId).stream().map(Card::getName))
                .containsExactly("Divination", "Opt");
        PendingInteraction.EyeOfTheStormCastChoice secondChoice =
                (PendingInteraction.EyeOfTheStormCastChoice) gd.interaction.activeInteraction();
        List<UUID> chosenInReverseOrder = List.of(
                secondChoice.validCopyIds().get(1), secondChoice.validCopyIds().getFirst());

        harness.handleMultipleCardsChosen(player1, chosenInReverseOrder);

        assertThat(gd.stack.stream().filter(StackEntry::isCopy).map(entry -> entry.getCard().getName()))
                .containsExactly("Opt", "Divination");
        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType().name().equals("TRIGGERED_ABILITY")
                && entry.getCard().getName().equals("Eye of the Storm"));
    }

    private UUID addEye() {
        harness.addToBattlefield(player1, new EyeOfTheStorm());
        return harness.getPermanentId(player1, "Eye of the Storm");
    }

    private void addDivinationMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
