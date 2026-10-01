package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MinasTirithGarrison.class, EliteVanguard.class, GrizzlyBears.class})
class MinasTirithGarrisonTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of cards in its controller's hand and toughness is 5")
    void powerTracksControllerHandSize() {
        Permanent garrison = harness.addToBattlefieldAndReturn(player1, new MinasTirithGarrison());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, garrison)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, garrison)).isEqualTo(5);

        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, garrison)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking lets you tap Humans to draw for each one")
    void attacksTapHumansAndDraw() {
        Permanent garrison = addReady(new MinasTirithGarrison());
        Permanent human = addReady(new EliteVanguard());
        Permanent nonHuman = addReady(new GrizzlyBears());
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(human.getId()));

        assertThat(garrison.isTapped()).isTrue();
        assertThat(human.isTapped()).isTrue();
        assertThat(nonHuman.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Attacking may tap no Humans")
    void mayTapNoHumans() {
        Permanent human = addReady(new EliteVanguard());
        addReady(new MinasTirithGarrison());
        harness.setHand(player1, List.of());

        declareAttackers(List.of(1));
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(human.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private Permanent addReady(Card card) {
        return addCreatureReady(player1, card);
    }
}
