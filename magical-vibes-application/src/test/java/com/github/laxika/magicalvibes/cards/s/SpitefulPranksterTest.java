package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpitefulPrankster.class, GrizzlyBears.class, Shock.class})
class SpitefulPranksterTest extends BaseCardTest {

    @Test
    void hasFirstStrikeDuringItsControllersTurnOnly() {
        Permanent prankster = harness.addToBattlefieldAndReturn(player1, new SpitefulPrankster());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, prankster, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, prankster, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void dealsDamageToTargetPlayerWhenAnotherCreatureDies() {
        harness.addToBattlefield(player1, new SpitefulPrankster());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, harness.getPermanentId(player1, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void doesNotTriggerWhenItDiesItself() {
        harness.addToBattlefield(player1, new SpitefulPrankster());
        harness.setLife(player2, 20);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, harness.getPermanentId(player1, "Spiteful Prankster"));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }
}
