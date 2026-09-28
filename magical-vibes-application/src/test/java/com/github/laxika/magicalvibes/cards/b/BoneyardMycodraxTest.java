package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BoneyardMycodrax.class, GrizzlyBears.class, Mountain.class})
class BoneyardMycodraxTest extends BaseCardTest {

    @Test
    @DisplayName("Boneyard Mycodrax's power and toughness count other creature cards in its controller's graveyard")
    void powerAndToughnessCountOtherCreatureCardsInOwnGraveyard() {
        Permanent mycodrax = addCreatureReady(player1, new BoneyardMycodrax());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Mountain()));

        assertThat(gqs.getEffectivePower(gd, mycodrax)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, mycodrax)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boneyard Mycodrax's scavenge uses its graveyard power")
    void scavengeUsesGraveyardPower() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(
                new BoneyardMycodrax(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertNotInGraveyard(player1, "Boneyard Mycodrax");
    }
}
