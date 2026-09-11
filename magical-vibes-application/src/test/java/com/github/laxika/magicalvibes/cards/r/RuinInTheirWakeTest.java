package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.Wastes;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RuinInTheirWake.class, Forest.class, Wastes.class})
class RuinInTheirWakeTest extends BaseCardTest {

    @Test
    @DisplayName("Without Wastes, the revealed basic land goes into hand")
    void withoutWastesPutsBasicLandIntoHand() {
        Card forest = new Forest();
        castRuin(forest);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With a Wastes, the revealed basic land may enter the battlefield tapped")
    void withWastesMayPutBasicLandOntoBattlefieldTapped() {
        harness.addToBattlefield(player1, new Wastes());
        Card forest = new Forest();
        castRuin(forest);

        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == forest && permanent.isTapped());
    }

    @Test
    @DisplayName("Declining the Wastes battlefield option leaves the basic land in hand")
    void decliningBattlefieldOptionPutsBasicLandIntoHand() {
        harness.addToBattlefield(player1, new Wastes());
        Card forest = new Forest();
        castRuin(forest);

        harness.handleCardChosen(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == forest);
    }

    private void castRuin(Card forest) {
        harness.setLibrary(player1, List.of(forest));
        harness.setHand(player1, List.of(new RuinInTheirWake()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
    }
}
