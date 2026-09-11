package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.e.ElaborateFirecannon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AltanakTheThriceCalled.class, ElaborateFirecannon.class, Forest.class, Shock.class})
class AltanakTheThriceCalledTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when an opponent's spell targets Altanak")
    void drawsWhenOpponentSpellTargetsAltanak() {
        harness.addToBattlefield(player1, new AltanakTheThriceCalled());
        UUID altanakId = harness.getPermanentId(player1, "Altanak, the Thrice-Called");
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, altanakId);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Draws a card when an opponent's ability targets Altanak")
    void drawsWhenOpponentAbilityTargetsAltanak() {
        harness.addToBattlefield(player1, new AltanakTheThriceCalled());
        Permanent firecannon = new Permanent(new ElaborateFirecannon());
        firecannon.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(firecannon);
        UUID altanakId = harness.getPermanentId(player1, "Altanak, the Thrice-Called");
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.activateAbility(player2, 0, null, altanakId);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Returns a targeted land tapped and discards Altanak from hand")
    void returnsTargetedLandTappedFromHand() {
        AltanakTheThriceCalled altanak = new AltanakTheThriceCalled();
        harness.setHand(player1, List.of(altanak));
        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateHandAbilityWithGraveyardTargets(player1, 0, List.of(forest.getId()));
        harness.passBothPriorities();

        Permanent returnedForest = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(forest.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(returnedForest.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(altanak);
    }

    @Test
    @DisplayName("Rejects a nonland card as the hand ability's target")
    void rejectsNonlandGraveyardTarget() {
        AltanakTheThriceCalled altanak = new AltanakTheThriceCalled();
        harness.setHand(player1, List.of(altanak));
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateHandAbilityWithGraveyardTargets(
                player1, 0, List.of(shock.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(altanak);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }
}
