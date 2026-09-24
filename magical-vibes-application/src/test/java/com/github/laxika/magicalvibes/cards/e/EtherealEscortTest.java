package com.github.laxika.magicalvibes.cards.e;

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

@CardUsed({EtherealEscort.class, GrizzlyBears.class})
class EtherealEscortTest extends BaseCardTest {

    @Test
    void etbPerpetuallyGrantsLifelinkToAChosenCreatureCard() {
        GrizzlyBears bear = new GrizzlyBears();
        harness.setHand(player1, List.of(new EtherealEscort(), bear));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PerpetualPowerToughnessChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent enteredBear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(bear.getId()))
                .findFirst().orElseThrow();
        assertThat(enteredBear.hasKeyword(Keyword.LIFELINK)).isTrue();
    }

    @Test
    void attackTriggerPerpetuallyGrantsLifelinkToAChosenCreatureCard() {
        GrizzlyBears bear = new GrizzlyBears();
        Permanent escort = harness.addToBattlefieldAndReturn(player1, new EtherealEscort());
        harness.setHand(player1, List.of(bear));
        escort.setSummoningSick(false);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PerpetualPowerToughnessChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent enteredBear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(bear.getId()))
                .findFirst().orElseThrow();
        assertThat(enteredBear.hasKeyword(Keyword.LIFELINK)).isTrue();
    }
}
