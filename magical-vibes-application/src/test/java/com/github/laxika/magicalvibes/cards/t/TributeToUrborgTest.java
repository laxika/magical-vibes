package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TributeToUrborg.class, AirElemental.class, FountainOfYouth.class,
        GiantGrowth.class, GrizzlyBears.class, Shock.class})
class TributeToUrborgTest extends BaseCardTest {

    @Test
    void givesTargetCreatureMinusTwoMinusTwoWithoutKicker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        cast(false, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    void kickedPenaltyScalesWithInstantAndSorceryCardsInGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setGraveyard(player1, List.of(new Shock(), new GiantGrowth(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new TributeToUrborg()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castKickedInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(-4);
        assertThat(target.getToughnessModifier()).isEqualTo(-4);
    }

    @Test
    void cannotTargetNoncreaturePermanent() {
        Card artifact = new FountainOfYouth();
        harness.addToBattlefield(player2, artifact);
        harness.setHand(player1, List.of(new TributeToUrborg()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid target");
    }

    private void cast(boolean kicked, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new TributeToUrborg()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        if (kicked) {
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.castKickedInstant(player1, 0, targetId);
        } else {
            harness.castInstant(player1, 0, targetId);
        }
        harness.passBothPriorities();
    }
}
