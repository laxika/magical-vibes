package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PurplePentapus.class})
class PurplePentapusTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield surveils 1")
    void entersWithSurveil() {
        Card topCard = new PurplePentapus();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new PurplePentapus()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("The graveyard ability returns this card tapped")
    void returnsFromGraveyardTapped() {
        PurplePentapus pentapus = new PurplePentapus();
        harness.setGraveyard(player1, List.of(pentapus));
        Permanent creature = addCreatureReady(player1, new PurplePentapus());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(pentapus.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(pentapus);
    }

    @Test
    @DisplayName("The graveyard ability requires an untapped creature to tap")
    void requiresUntappedCreature() {
        PurplePentapus pentapus = new PurplePentapus();
        harness.setGraveyard(player1, List.of(pentapus));
        Permanent creature = addCreatureReady(player1, new PurplePentapus());
        creature.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Surveil may leave the top card in the library")
    void canKeepTopCard() {
        Card topCard = new PurplePentapus();
        Card nextCard = new PurplePentapus();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setHand(player1, List.of(new PurplePentapus()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    @DisplayName("Returning from the graveyard surveils and leaves another copy there")
    void returnSurveilsAndOnlyReturnsSource() {
        PurplePentapus pentapus = new PurplePentapus();
        PurplePentapus otherCopy = new PurplePentapus();
        Card topCard = new PurplePentapus();
        Card nextCard = new PurplePentapus();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setGraveyard(player1, List.of(pentapus, otherCopy));
        Permanent creature = addCreatureReady(player1, new PurplePentapus());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(pentapus, otherCopy);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherCopy, topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard().getId()).isEqualTo(pentapus.getId());
                    assertThat(permanent.isTapped()).isTrue();
                });
    }

    @Test
    @DisplayName("A summoning-sick creature can pay the tap cost")
    void canTapSummoningSickCreature() {
        PurplePentapus pentapus = new PurplePentapus();
        harness.setGraveyard(player1, List.of(pentapus));
        harness.setLibrary(player1, List.of());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new PurplePentapus());
        creature.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(pentapus);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(pentapus.getId())
                        && permanent.isTapped());
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the tap cost")
    void cannotTapOpponentsCreature() {
        PurplePentapus pentapus = new PurplePentapus();
        harness.setGraveyard(player1, List.of(pentapus));
        Permanent opponentCreature = addCreatureReady(player2, new PurplePentapus());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(pentapus);
    }

    @Test
    @DisplayName("An older activation cannot return the card after it returns and dies again")
    void olderActivationCannotReturnNewGraveyardObject() {
        PurplePentapus pentapus = new PurplePentapus();
        harness.setGraveyard(player1, List.of(pentapus));
        harness.setLibrary(player1, List.of());
        addCreatureReady(player1, new PurplePentapus());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0);
        addCreatureReady(player1, new PurplePentapus());
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(pentapus.getId()))
                .findFirst().orElseThrow();
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().destroyPermanentToGraveyard(gd, returned));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(pentapus);

        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(pentapus);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(pentapus.getId()));
    }
}
