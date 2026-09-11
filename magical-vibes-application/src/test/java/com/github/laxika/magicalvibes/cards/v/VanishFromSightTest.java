package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VanishFromSight.class, GrizzlyBears.class, Island.class})
class VanishFromSightTest extends BaseCardTest {

    @Test
    @DisplayName("Puts the target on top, then surveils 1")
    void putsTargetOnTopThenSurveils() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card targetTop = new Island();
        Card targetBottom = new Island();
        Card surveilledCard = new GrizzlyBears();
        Card keptCard = new Island();
        harness.setLibrary(player2, List.of(targetTop, targetBottom));
        harness.setLibrary(player1, List.of(surveilledCard, keptCard));

        castVanishFromSight(target);
        harness.handleListChoice(player2, "Top");
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target.getCard(), targetTop, targetBottom);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(keptCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(surveilledCard);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Vanish from Sight");
    }

    @Test
    @DisplayName("Puts the target on the bottom, then surveils 1")
    void putsTargetOnBottomThenSurveils() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card targetTop = new Island();
        Card targetBottom = new Island();
        Card surveilledCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(targetTop, targetBottom));
        harness.setLibrary(player1, List.of(surveilledCard));

        castVanishFromSight(target);
        harness.handleListChoice(player2, "Bottom");
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(targetTop, targetBottom, target.getCard());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(surveilledCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(surveilledCard);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new VanishFromSight()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castVanishFromSight(Permanent target) {
        harness.setHand(player1, List.of(new VanishFromSight()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
    }
}
