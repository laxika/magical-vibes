package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.ArborElf;
import com.github.laxika.magicalvibes.cards.d.Dispel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BojukaBog.class, ArborElf.class, Dispel.class})
class BojukaBogTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new BojukaBog()));

        harness.playLand(player1, 0);

        Permanent bog = findPermanent(player1, "Bojuka Bog");
        assertThat(bog.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Exiles the chosen player's graveyard")
    void exilesChosenPlayersGraveyard() {
        harness.setGraveyard(player1, List.of(new Dispel()));
        harness.setGraveyard(player2, List.of(new ArborElf(), new Dispel()));
        harness.setHand(player1, List.of(new BojukaBog()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Arbor Elf", "Dispel");
    }

    @Test
    @DisplayName("Can exile its controller's graveyard")
    void canTargetController() {
        Dispel ownCard = new Dispel();
        ArborElf opponentCard = new ArborElf();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setHand(player1, List.of(new BojukaBog()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(ownCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can target a player with an empty graveyard")
    void canTargetEmptyGraveyard() {
        Dispel ownCard = new Dispel();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of());
        harness.setHand(player1, List.of(new BojukaBog()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Exiles cards present at resolution, not only at target selection")
    void exilesCardsAddedBeforeResolution() {
        ArborElf originalCard = new ArborElf();
        Dispel addedCard = new Dispel();
        harness.setGraveyard(player2, List.of(originalCard));
        harness.setHand(player1, List.of(new BojukaBog()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(originalCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.setGraveyard(player2, List.of(originalCard, addedCard));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(originalCard, addedCard);
    }

    @Test
    @DisplayName("Tapping adds one black mana")
    void tappingAddsBlackMana() {
        Permanent bog = harness.addToBattlefieldAndReturn(player1, new BojukaBog());
        bog.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(bog.isTapped()).isTrue();
    }
}
