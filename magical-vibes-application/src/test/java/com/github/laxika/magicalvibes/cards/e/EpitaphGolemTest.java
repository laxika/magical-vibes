package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EpitaphGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Puts target card from the controller's graveyard on the bottom of their library")
    void tucksTargetToBottomOfLibrary() {
        int golemIdx = addGolem();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Card tucked = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(tucked)));
        harness.setLibrary(player1, new ArrayList<>(List.of(new HillGiant(), new GrizzlyBears())));

        harness.activateAbilityWithGraveyardTargets(player1, golemIdx, 0, List.of(tucked.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(3);
        assertThat(library.get(library.size() - 1).getId()).isEqualTo(tucked.getId());
    }

    @Test
    @DisplayName("Cannot target a card in an opponent's graveyard")
    void rejectsOpponentGraveyardTarget() {
        int golemIdx = addGolem();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Card opponentCard = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(opponentCard)));

        UUID targetId = opponentCard.getId();
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, golemIdx, 0, List.of(targetId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without paying the generic cost")
    void rejectsActivationWithoutMana() {
        int golemIdx = addGolem();

        Card tucked = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(tucked)));

        UUID targetId = tucked.getId();
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, golemIdx, 0, List.of(targetId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability does not require tapping, so it can be activated twice in a turn")
    void activatesTwiceWithoutTapping() {
        int golemIdx = addGolem();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        Card first = new GrizzlyBears();
        Card second = new HillGiant();
        harness.setGraveyard(player1, new ArrayList<>(List.of(first, second)));
        harness.setLibrary(player1, new ArrayList<>(List.of(new GrizzlyBears())));

        harness.activateAbilityWithGraveyardTargets(player1, golemIdx, 0, List.of(first.getId()));
        harness.passBothPriorities();
        harness.activateAbilityWithGraveyardTargets(player1, golemIdx, 0, List.of(second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(3);
        assertThat(library.get(1).getId()).isEqualTo(first.getId());
        assertThat(library.get(2).getId()).isEqualTo(second.getId());
    }

    private int addGolem() {
        harness.addToBattlefield(player1, new EpitaphGolem());
        Permanent golem = findPermanent(player1, "Epitaph Golem");
        golem.setSummoningSick(false);
        return gd.playerBattlefields.get(player1.getId()).indexOf(golem);
    }
}
