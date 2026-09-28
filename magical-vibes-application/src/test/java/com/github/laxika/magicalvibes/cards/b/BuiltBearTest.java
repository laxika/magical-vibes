package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BuiltBear.class, Forest.class, GrizzlyBears.class, Shock.class})
class BuiltBearTest extends BaseCardTest {

    @Test
    void entersAsBuiltBearAndDrawsACard() {
        Forest drawnCard = new Forest();
        harness.setHand(player1, List.of(new BuiltBear()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent bear = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.REACH)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.VIGILANCE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void tapsForManaOfChosenColor() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new BuiltBear());
        bear.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void wardCountersAnOpponentSpellUnlessTheyPayTwo() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new BuiltBear());
        bear.setSummoningSick(false);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, bear.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
    }
}
