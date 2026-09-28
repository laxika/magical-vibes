package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BygoneMarvels.class, GrizzlyBears.class, Shock.class})
class BygoneMarvelsTest extends BaseCardTest {

    @Test
    void returnsPermanentCardAndExilesItselfWithoutDescendEight() {
        Card target = new GrizzlyBears();
        BygoneMarvels bygoneMarvels = new BygoneMarvels();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(bygoneMarvels));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, target.getId());
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bygoneMarvels);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copiesTwiceWhenDescendEightIsMet() {
        Card target = new GrizzlyBears();
        Card firstCopyTarget = new GrizzlyBears();
        Card secondCopyTarget = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(
                target,
                firstCopyTarget, secondCopyTarget, new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        BygoneMarvels bygoneMarvels = new BygoneMarvels();
        harness.setHand(player1, List.of(bygoneMarvels));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, target.getId());
        assertThat(gd.stack).hasSize(3);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, firstCopyTarget.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, secondCopyTarget.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.gameLog).filteredOn(log -> log.plainText().contains("A copy of Bygone Marvels"))
                .hasSize(2);
        assertThat(gd.playerHands.get(player1.getId()))
                .contains(target, firstCopyTarget, secondCopyTarget);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bygoneMarvels);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetNonPermanentCard() {
        Card target = new Shock();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new BygoneMarvels()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
