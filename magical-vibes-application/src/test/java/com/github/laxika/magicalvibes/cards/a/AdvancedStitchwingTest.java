package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FieldCreeper;
import com.github.laxika.magicalvibes.cards.t.Terrarion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AdvancedStitchwing.class, FieldCreeper.class, Terrarion.class})
class AdvancedStitchwingTest extends BaseCardTest {

    @Test
    @DisplayName("Graveyard ability returns it tapped after discarding two cards")
    void returnsFromGraveyardTappedAfterDiscardingTwoCards() {
        AdvancedStitchwing stitchwing = new AdvancedStitchwing();
        harness.setGraveyard(player1, List.of(stitchwing));
        harness.setHand(player1, List.of(new FieldCreeper(), new Terrarion()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(stitchwing.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(stitchwing.getId()));
    }

    @Test
    @DisplayName("Cannot activate the graveyard ability with fewer than two cards in hand")
    void cannotActivateWithFewerThanTwoCards() {
        harness.setGraveyard(player1, List.of(new AdvancedStitchwing()));
        harness.setHand(player1, List.of(new FieldCreeper()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Discards are paid before resolution and only the activating copy returns")
    void paysDiscardsBeforeResolutionAndReturnsOnlySource() {
        AdvancedStitchwing source = new AdvancedStitchwing();
        AdvancedStitchwing other = new AdvancedStitchwing();
        FieldCreeper discardedCreature = new FieldCreeper();
        Terrarion discardedArtifact = new Terrarion();
        Terrarion retained = new Terrarion();
        harness.setGraveyard(player1, List.of(source, other));
        harness.setHand(player1, List.of(discardedCreature, retained, discardedArtifact));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 2);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(source, other, discardedCreature, discardedArtifact);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(permanent -> {
            assertThat(permanent.getCard().getId()).isEqualTo(source.getId());
            assertThat(permanent.isTapped()).isTrue();
        });
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(other, discardedCreature, discardedArtifact);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
    }

    @Test
    @DisplayName("Three colorless mana cannot pay the blue requirement")
    void cannotActivateWithoutBlueMana() {
        AdvancedStitchwing source = new AdvancedStitchwing();
        FieldCreeper creature = new FieldCreeper();
        Terrarion artifact = new Terrarion();
        harness.setGraveyard(player1, List.of(source));
        harness.setHand(player1, List.of(creature, artifact));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature, artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability can be activated twice but returns the source only once")
    void canActivateAgainInResponse() {
        AdvancedStitchwing source = new AdvancedStitchwing();
        harness.setGraveyard(player1, List.of(source));
        harness.setHand(player1, List.of(new FieldCreeper(), new Terrarion(),
                new FieldCreeper(), new Terrarion()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(permanent -> {
            assertThat(permanent.getCard().getId()).isEqualTo(source.getId());
            assertThat(permanent.isTapped()).isTrue();
        });
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4)
                .noneMatch(card -> card.getId().equals(source.getId()));
    }
}
