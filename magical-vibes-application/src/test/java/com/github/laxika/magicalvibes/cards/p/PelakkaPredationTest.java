package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PelakkaPredation.class, PelakkaCaverns.class, HillGiant.class, GrizzlyBears.class, Forest.class})
class PelakkaPredationTest extends BaseCardTest {

    @Test
    void choosesAndDiscardsAnOpponentCardWithManaValueAtLeastThree() {
        HillGiant expensiveCard = new HillGiant();
        GrizzlyBears cheapCard = new GrizzlyBears();
        Forest land = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(cheapCard, expensiveCard, land)));
        harness.setHand(player1, List.of(new PelakkaPredation()));
        addSpellMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.validIndices()).containsExactly(1);

        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(expensiveCard);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(cheapCard, land);
    }

    @Test
    void excludesCardsWithManaValueBelowThree() {
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        harness.setHand(player1, List.of(new PelakkaPredation()));
        addSpellMana();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    void cannotTargetTheController() {
        harness.setHand(player1, List.of(new PelakkaPredation()));
        addSpellMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void landFaceEntersTappedAndProducesBlackMana() {
        harness.setHand(player1, List.of(new PelakkaPredation()));

        gs.playCard(gd, player1, 0, 1, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.getCard()).isInstanceOf(PelakkaCaverns.class);
        assertThat(land.isTapped()).isTrue();

        land.untap();
        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool mana = gd.playerManaPools.get(player1.getId());
        assertThat(mana.get(ManaColor.BLACK)).isEqualTo(1);
    }

    private void addSpellMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
