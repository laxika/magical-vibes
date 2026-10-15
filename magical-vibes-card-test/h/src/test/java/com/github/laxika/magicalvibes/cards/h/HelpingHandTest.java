package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HelpingHand.class, GrizzlyBears.class, HillGiant.class, HolyDay.class, TrainedArmodon.class})
class HelpingHandTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature with mana value 3 or less tapped")
    void returnsEligibleCreatureTapped() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new HelpingHand()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId())
                        && permanent.isTapped());
    }

    @Test
    @DisplayName("Cannot target a creature with mana value greater than 3")
    void cannotTargetCreatureWithManaValueGreaterThanThree() {
        Card creature = new HillGiant();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new HelpingHand()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature card in a graveyard")
    void cannotTargetNoncreatureCard() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new HelpingHand()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new HelpingHand()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    @Test
    @DisplayName("Returns only the chosen creature at the mana value three boundary")
    void returnsOnlyChosenCreatureWithManaValueThree() {
        Card creature = new TrainedArmodon();
        Card otherCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature, otherCreature));
        harness.setHand(player1, List.of(new HelpingHand()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(creature.getId());
                    assertThat(permanent.isTapped()).isTrue();
                });
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(otherCreature).doesNotContain(creature);
        harness.assertInGraveyard(player1, "Helping Hand");
    }

    @Test
    @DisplayName("Does not return another creature when the target leaves the graveyard")
    void doesNotChooseReplacementForMissingTarget() {
        Card creature = new TrainedArmodon();
        Card otherCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature, otherCreature));
        harness.setHand(player1, List.of(new HelpingHand()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castSorcery(player1, 0, creature.getId());
        harness.setGraveyard(player1, List.of(otherCreature));
        harness.setHand(player1, List.of(creature));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInHand(player1, "Trained Armodon");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Helping Hand");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot cast without choosing a graveyard target")
    void requiresTarget() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new HelpingHand()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
