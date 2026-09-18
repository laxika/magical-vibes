package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TemptWithReflections.class, GrizzlyBears.class})
class TemptWithReflectionsTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a copy for the controller and rewards an accepting opponent")
    void acceptingOpponentCreatesCopiesForBothPlayers() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        castTemptWithReflections(harness.getPermanentId(player1, "Grizzly Bears"));
        harness.handleMayAbilityChosen(player2, true);

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(3);
        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(2);
        assertThat(findPermanents(player1, "Grizzly Bears")).filteredOn(p -> p.getCard().isToken())
                .hasSize(2);
        assertThat(findPermanents(player2, "Grizzly Bears")).filteredOn(p -> p.getCard().isToken())
                .hasSize(1);
    }

    @Test
    @DisplayName("A declining opponent does not receive or grant an additional copy")
    void decliningOpponentDoesNotCreateAdditionalCopies() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        castTemptWithReflections(harness.getPermanentId(player1, "Grizzly Bears"));
        harness.handleMayAbilityChosen(player2, false);

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2);
        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new TemptWithReflections()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castTemptWithReflections(UUID targetId) {
        harness.setHand(player1, List.of(new TemptWithReflections()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
