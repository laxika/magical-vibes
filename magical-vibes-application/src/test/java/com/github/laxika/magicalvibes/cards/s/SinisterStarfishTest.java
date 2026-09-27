package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SinisterStarfish.class, GrizzlyBears.class})
class SinisterStarfishTest extends BaseCardTest {

    @Test
    void surveilAccepted() {
        Permanent starfish = addReadyStarfish();
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(starfish.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    void surveilDeclinedLeavesTopCardOnLibrary() {
        addReadyStarfish();
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(topCard);
    }

    @Test
    void summoningSickStarfishCannotActivate() {
        harness.addToBattlefieldAndReturn(player1, new SinisterStarfish()).setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyStarfish() {
        Permanent starfish = harness.addToBattlefieldAndReturn(player1, new SinisterStarfish());
        starfish.setSummoningSick(false);
        return starfish;
    }
}
