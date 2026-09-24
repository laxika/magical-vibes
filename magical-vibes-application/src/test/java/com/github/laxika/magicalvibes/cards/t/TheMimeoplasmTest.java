package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheMimeoplasm.class, GrizzlyBears.class, HillGiant.class})
class TheMimeoplasmTest extends BaseCardTest {

    @Test
    void copiesOneChosenCreatureAndAddsCountersForTheOtherPower() {
        GrizzlyBears copiedCreature = new GrizzlyBears();
        HillGiant otherCreature = new HillGiant();
        harness.setGraveyard(player1, List.of(copiedCreature, otherCreature));
        harness.setHand(player1, List.of(new TheMimeoplasm()));
        addMimeoplasmMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiGraveyardChoice initialChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(initialChoice).isNotNull();
        assertThat(initialChoice.minCount()).isEqualTo(2);
        assertThat(initialChoice.maxCount()).isEqualTo(2);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(copiedCreature.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(copiedCreature.getId(), otherCreature.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(copiedCreature.getId()));

        Permanent mimeoplasm = findPermanent(player1, "Grizzly Bears");
        assertThat(mimeoplasm.getCard().getPower()).isEqualTo(2);
        assertThat(mimeoplasm.getCard().getToughness()).isEqualTo(2);
        assertThat(mimeoplasm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(entry -> entry.card())
                .containsExactlyInAnyOrder(copiedCreature, otherCreature);
    }

    @Test
    void decliningCopyLeavesTheZeroZeroCreatureToStateBasedActions() {
        GrizzlyBears creature = new GrizzlyBears();
        HillGiant otherCreature = new HillGiant();
        TheMimeoplasm mimeoplasm = new TheMimeoplasm();
        harness.setGraveyard(player1, List.of(creature, otherCreature));
        harness.setHand(player1, List.of(mimeoplasm));
        addMimeoplasmMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard().getId().equals(mimeoplasm.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature, otherCreature, mimeoplasm);
        assertThat(gd.exiledCards).isEmpty();
    }

    private void addMimeoplasmMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
