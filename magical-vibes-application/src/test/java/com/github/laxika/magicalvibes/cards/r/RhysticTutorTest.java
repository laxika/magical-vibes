package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.p.PlagueFiend;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RhysticTutor.class, RebelInformer.class, PlagueFiend.class})
class RhysticTutorTest extends BaseCardTest {

    @Test
    void searchesLibraryWhenNoPlayerPays() {
        harness.setLibrary(player1, List.of(new RebelInformer(), new PlagueFiend()));
        castTutor();

        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof RebelInformer);
        assertThat(gd.playerDecks.get(player1.getId()))
                .hasSize(1)
                .allMatch(card -> card instanceof PlagueFiend);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void anyPlayerCanPayToPreventTheSearch() {
        harness.setLibrary(player1, List.of(new RebelInformer()));
        castTutor();

        harness.handleMayAbilityChosen(player1, false);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card instanceof RebelInformer);
    }

    @Test
    void controllerCanPayToPreventTheSearch() {
        harness.setLibrary(player1, List.of(new RebelInformer()));
        castTutor();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(card -> card instanceof RebelInformer);
    }

    private void castTutor() {
        harness.castFromHand(player1, new RhysticTutor(), "{2}{B}");
        harness.passBothPriorities();
    }
}
