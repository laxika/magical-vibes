package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MortuaryMire.class, GrizzlyBears.class, Shock.class})
class MortuaryMireTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new MortuaryMire()));

        harness.playLand(player1, 0);

        Permanent mire = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(mire.isTapped()).isTrue();
    }

    @Test
    @DisplayName("May put a target creature card from the graveyard on top of the library")
    void putsTargetCreatureOnTopOfLibrary() {
        Card creature = new GrizzlyBears();
        Card nonCreature = new Shock();
        harness.setGraveyard(player1, List.of(creature, nonCreature));
        harness.setHand(player1, List.of(new MortuaryMire()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(creature);
    }

    @Test
    @DisplayName("May decline putting a creature card on top of the library")
    void mayDeclineCreatureSelection() {
        Card creature = new GrizzlyBears();
        Card libraryCard = new Shock();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new MortuaryMire()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
    }

    @Test
    @DisplayName("Tapping adds one black mana")
    void tappingAddsBlackMana() {
        Permanent mire = harness.addToBattlefieldAndReturn(player1, new MortuaryMire());
        mire.untap();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(mire.isTapped()).isTrue();
    }
}
