package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ThingInTheIceTest extends BaseCardTest {

    @Test
    @DisplayName("Awoken Horror can attack after transforming")
    void awokenHorrorCanAttackAfterTransforming() {
        addTransformedAwokenHorror();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        // Declaring is itself the assertion: an illegal attacker throws here. Combat then runs to
        // the damage step on its own, so the life loss is what proves the attack connected.
        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(lifeBefore);
    }

    /**
     * Puts Thing in the Ice onto the battlefield already flipped, swapping in the very back-face
     * card instance the engine's own transform path uses ({@code originalCard.getBackFaceCard()}).
     */
    private Permanent addTransformedAwokenHorror() {
        Permanent perm = harness.addToBattlefieldAndReturn(player1, new ThingInTheIce());
        Card backFace = perm.getOriginalCard().getBackFaceCard();
        perm.setCard(backFace);
        perm.setTransformed(true);
        perm.setSummoningSick(false);
        return perm;
    }
}
