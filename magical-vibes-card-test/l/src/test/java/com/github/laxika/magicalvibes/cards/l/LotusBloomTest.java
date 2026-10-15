package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.p.PithingNeedle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LotusBloom.class, PithingNeedle.class})
class LotusBloomTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Lotus Bloom with three time counters")
    void suspendExilesWithThreeTimeCounters() {
        LotusBloom card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The last suspend counter offers a free cast")
    void lastCounterOffersFreeCast() {
        LotusBloom card = suspendCard();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lotus Bloom");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Tapping and sacrificing Lotus Bloom adds three mana of the chosen color")
    void sacrificeAddsThreeManaOfChosenColor() {
        LotusBloom lotusBloom = new LotusBloom();
        harness.addToBattlefield(player1, lotusBloom);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Lotus Bloom");
        harness.assertInGraveyard(player1, "Lotus Bloom");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the suspend cast leaves Lotus Bloom in exile without another offer")
    void decliningCastLeavesCardInExile() {
        LotusBloom card = suspendCard();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        harness.assertNotOnBattlefield(player1, "Lotus Bloom");

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's upkeep does not remove Lotus Bloom's time counters")
    void opponentsUpkeepDoesNotRemoveCounter() {
        LotusBloom card = suspendCard();

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    @DisplayName("Lotus Bloom cannot be suspended during upkeep without flash permission")
    void cannotSuspendDuringUpkeep() {
        LotusBloom card = new LotusBloom();
        harness.setHand(player1, List.of(card));
        advanceToUpkeep(player1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Lotus Bloom");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("A tapped Lotus Bloom cannot pay its activation cost")
    void tappedLotusBloomCannotActivate() {
        harness.addToBattlefieldAndReturn(player1, new LotusBloom()).tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Lotus Bloom");
        harness.assertNotInGraveyard(player1, "Lotus Bloom");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Lotus Bloom's mana ability works during upkeep and does not use the stack")
    void manaAbilityWorksAtInstantSpeedWithoutStack() {
        harness.addToBattlefield(player1, new LotusBloom());
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Lotus Bloom");
    }

    @Test
    @DisplayName("Pithing Needle naming Lotus Bloom does not prevent the suspend special action")
    void pithingNeedleDoesNotPreventSuspend() {
        harness.setHand(player1, List.of(new PithingNeedle()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Lotus Bloom");
        harness.ensurePriority(player1);

        LotusBloom card = suspendCard();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
        assertThat(gd.stack).isEmpty();
    }

    private LotusBloom suspendCard() {
        LotusBloom card = new LotusBloom();
        harness.setHand(player1, List.of(card));
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
