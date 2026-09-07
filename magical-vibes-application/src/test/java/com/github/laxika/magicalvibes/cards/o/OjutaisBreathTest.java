package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.action.ReboundAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OjutaisBreath.class, GrizzlyBears.class, Forest.class})
class OjutaisBreathTest extends BaseCardTest {

    @Test
    void tapsTargetCreatureAndSkipsItsNextUntap() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        OjutaisBreath card = new OjutaisBreath();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getSkipUntapCount()).isEqualTo(1);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.delayedActions).anyMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void reboundOffersAFreeCastAtNextUpkeep() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        OjutaisBreath card = new OjutaisBreath();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castInstant(player1, 0, bear.getId());
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ExileCastSpellTarget.class);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isTrue();
        assertThat(bear.getSkipUntapCount()).isEqualTo(1);
        assertThat(gd.findExiledCard(card.getId())).isNull();
        harness.assertInGraveyard(player1, "Ojutai's Breath");
        assertThat(gd.delayedActions).noneMatch(action -> action instanceof ReboundAtNextUpkeep);
    }

    @Test
    void cannotTargetANoncreaturePermanent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new OjutaisBreath()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
