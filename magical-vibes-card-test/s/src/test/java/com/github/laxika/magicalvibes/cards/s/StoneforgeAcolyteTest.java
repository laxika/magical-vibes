package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BoneSaw;
import com.github.laxika.magicalvibes.cards.e.EldraziMimic;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StoneforgeAcolyte.class, BoneSaw.class, EldraziMimic.class})
class StoneforgeAcolyteTest extends BaseCardTest {

    @Test
    @DisplayName("Cohort finds an Equipment among the top four and lets you order the rest")
    void cohortFindsEquipmentAndOrdersRest() {
        Card first = new StoneforgeAcolyte();
        Card equipment = new BoneSaw();
        Card third = new StoneforgeAcolyte();
        Card fourth = new StoneforgeAcolyte();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, equipment, third, fourth));
        Permanent acolyte = addCreatureReady(player1, new StoneforgeAcolyte());
        Permanent ally = addCreatureReady(player1, new StoneforgeAcolyte());

        harness.activateAbility(player1, battlefieldIndex(acolyte), 0, null, null);
        harness.passBothPriorities();

        assertThat(acolyte.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(equipment);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(equipment);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        List<Card> remaining = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(
                        remaining.indexOf(fourth),
                        remaining.indexOf(third),
                        remaining.indexOf(first))));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth, third, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cohort cannot be activated without another untapped Ally")
    void cannotActivateWithoutAnotherUntappedAlly() {
        Permanent acolyte = addCreatureReady(player1, new StoneforgeAcolyte());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(acolyte), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
    }

    @Test
    @DisplayName("Cohort puts all four cards on the bottom when no Equipment is found")
    void putsAllCardsOnBottomWhenNoEquipmentIsFound() {
        Card first = new StoneforgeAcolyte();
        Card second = new StoneforgeAcolyte();
        Card third = new StoneforgeAcolyte();
        Card fourth = new StoneforgeAcolyte();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, second, third, fourth));
        Permanent acolyte = addCreatureReady(player1, new StoneforgeAcolyte());
        Permanent ally = addCreatureReady(player1, new StoneforgeAcolyte());

        harness.activateAbility(player1, battlefieldIndex(acolyte), 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        List<Card> remaining = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(
                        remaining.indexOf(fourth),
                        remaining.indexOf(third),
                        remaining.indexOf(second),
                        remaining.indexOf(first))));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth, third, second, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cohort may decline Equipment and puts the looked-at cards below the untouched library")
    void mayDeclineEquipment() {
        Card first = new StoneforgeAcolyte();
        Card equipment = new BoneSaw();
        Card third = new StoneforgeAcolyte();
        Card fourth = new StoneforgeAcolyte();
        Card untouched = new BoneSaw();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(first, equipment, third, fourth, untouched));
        Permanent acolyte = addCreatureReady(player1, new StoneforgeAcolyte());
        addCreatureReady(player1, new StoneforgeAcolyte());

        harness.activateAbility(player1, battlefieldIndex(acolyte), 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(equipment);
        harness.handleCardChosen(player1, -1);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, fourth, third, equipment, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cohort looks at all available cards in a library shorter than four")
    void handlesShortLibrary() {
        Card remaining = new StoneforgeAcolyte();
        Card equipment = new BoneSaw();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(remaining, equipment));
        Permanent acolyte = addCreatureReady(player1, new StoneforgeAcolyte());
        addCreatureReady(player1, new StoneforgeAcolyte());

        harness.activateAbility(player1, battlefieldIndex(acolyte), 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(equipment);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cohort resolves with an empty library and still pays both tap costs")
    void handlesEmptyLibrary() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        Permanent acolyte = addCreatureReady(player1, new StoneforgeAcolyte());
        Permanent ally = addCreatureReady(player1, new StoneforgeAcolyte());

        harness.activateAbility(player1, battlefieldIndex(acolyte), 0, null, null);
        harness.passBothPriorities();

        assertThat(acolyte.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cohort may tap a summoning-sick Ally for its additional cost")
    void mayTapSummoningSickAlly() {
        Card equipment = new BoneSaw();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(equipment));
        Permanent acolyte = addCreatureReady(player1, new StoneforgeAcolyte());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new StoneforgeAcolyte());

        harness.activateAbility(player1, battlefieldIndex(acolyte), 0, null, null);
        assertThat(acolyte.isTapped()).isTrue();
        assertThat(ally.isTapped()).isTrue();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(equipment);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A summoning-sick Acolyte cannot activate its tap ability")
    void cannotActivateWithSummoningSickness() {
        Permanent acolyte = harness.addToBattlefieldAndReturn(player1, new StoneforgeAcolyte());
        Permanent ally = addCreatureReady(player1, new StoneforgeAcolyte());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(acolyte), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(acolyte.isTapped()).isFalse();
        assertThat(ally.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cohort cannot use a tapped Ally or an opponent's untapped Ally")
    void cannotUseTappedOrOpposingAlly() {
        Permanent acolyte = addCreatureReady(player1, new StoneforgeAcolyte());
        Permanent tappedAlly = addCreatureReady(player1, new StoneforgeAcolyte());
        tappedAlly.tap();
        Permanent opposingAlly = addCreatureReady(player2, new StoneforgeAcolyte());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(acolyte), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
        assertThat(acolyte.isTapped()).isFalse();
        assertThat(opposingAlly.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cohort cannot tap an untapped non-Ally creature")
    void cannotUseNonAllyCreature() {
        Permanent acolyte = addCreatureReady(player1, new StoneforgeAcolyte());
        Permanent nonAlly = addCreatureReady(player1, new EldraziMimic());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(acolyte), 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No untapped matching creature to tap");
        assertThat(acolyte.isTapped()).isFalse();
        assertThat(nonAlly.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cohort takes only one Equipment even when multiple are among the top four")
    void choosesOnlyOneEquipment() {
        Card firstEquipment = new BoneSaw();
        Card secondEquipment = new BoneSaw();
        Card third = new StoneforgeAcolyte();
        Card fourth = new StoneforgeAcolyte();
        Card untouched = new StoneforgeAcolyte();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(firstEquipment, secondEquipment, third, fourth, untouched));
        Permanent acolyte = addCreatureReady(player1, new StoneforgeAcolyte());
        addCreatureReady(player1, new StoneforgeAcolyte());

        harness.activateAbility(player1, battlefieldIndex(acolyte), 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(firstEquipment, secondEquipment);
        harness.handleCardChosen(player1, 1);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondEquipment);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched, fourth, firstEquipment, third);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
