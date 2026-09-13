package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TyrantsChoice.class, GrizzlyBears.class})
class TyrantsChoiceTest extends BaseCardTest {

    @Test
    void tortureWinsOnTieAndEachOpponentLosesFourLife() {
        cast();

        harness.handleListChoice(player1, ChoiceContext.TyrantsChoiceChoice.TORTURE);
        harness.handleListChoice(player2, ChoiceContext.TyrantsChoiceChoice.DEATH);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void deathMajorityMakesEachOpponentSacrificeAChosenCreature() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        cast();

        harness.handleListChoice(player1, ChoiceContext.TyrantsChoiceChoice.DEATH);
        harness.handleListChoice(player2, ChoiceContext.TyrantsChoiceChoice.DEATH);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactly(first.getId(), second.getId());

        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(second);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void cast() {
        harness.setHand(player1, List.of(new TyrantsChoice()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
