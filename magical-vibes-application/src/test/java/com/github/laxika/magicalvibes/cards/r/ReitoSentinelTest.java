package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReitoSentinel.class, GrizzlyBears.class, Shock.class})
class ReitoSentinelTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills three cards from the chosen player's library")
    void etbMillsTargetPlayer() {
        harness.setLibrary(player2, new ArrayList<>(List.of(
                new Shock(), new Shock(), new Shock(), new Shock())));
        harness.setHand(player1, List.of(new ReitoSentinel()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Puts a target card from any graveyard on the bottom of its owner's library")
    void putsTargetGraveyardCardOnOwnerLibraryBottom() {
        Card target = new GrizzlyBears();
        Card existingTop = new Shock();
        Card existingBottom = new Shock();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of(existingTop, existingBottom));
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new ReitoSentinel());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(sentinel.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(existingTop, existingBottom, target);
    }

    @Test
    @DisplayName("Rejects a target that is not a card in a graveyard")
    void rejectsNonGraveyardTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new ReitoSentinel());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, target.getCard().getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }
}
