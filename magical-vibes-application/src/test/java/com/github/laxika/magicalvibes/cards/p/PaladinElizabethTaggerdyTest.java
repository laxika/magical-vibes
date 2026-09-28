package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PaladinElizabethTaggerdy.class, GrizzlyBears.class, HillGiant.class, Forest.class})
class PaladinElizabethTaggerdyTest extends BaseCardTest {

    @Test
    @DisplayName("Battalion draws a card and offers a creature within its power tapped and attacking")
    void battalionDrawsAndPutsCreatureTappedAndAttacking() {
        addCreatureReady(player1, new PaladinElizabethTaggerdy());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        HillGiant tooExpensive = new HillGiant();
        GrizzlyBears valid = new GrizzlyBears();
        Forest drawn = new Forest();
        harness.setHand(player1, List.of(tooExpensive, valid));
        harness.setLibrary(player1, List.of(drawn));

        declareAttackers(player1, List.of(0, 1, 2));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.HandChoice choice =
                (PendingInteraction.HandChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(1);
        harness.handleCardChosen(player1, 1);

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(valid.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(entered.isTapped()).isTrue();
        assertThat(entered.isAttackedThisTurn()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).contains(tooExpensive);
    }

    @Test
    @DisplayName("Battalion does not trigger without two other attackers")
    void battalionDoesNotTriggerWithOnlyOneOtherAttacker() {
        addCreatureReady(player1, new PaladinElizabethTaggerdy());
        addCreatureReady(player1, new GrizzlyBears());
        Forest drawn = new Forest();
        harness.setLibrary(player1, List.of(drawn));

        declareAttackers(player1, List.of(0, 1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(drawn);
    }
}
