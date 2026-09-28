package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BuckyBarnesEagerAlly;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NighthawkDarkDefender.class, BuckyBarnesEagerAlly.class, GrizzlyBears.class})
class NighthawkDarkDefenderTest extends BaseCardTest {

    @Test
    void ownEntryBoostsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new NighthawkDarkDefender()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void anotherHeroEntryBoostsTargetCreature() {
        harness.addToBattlefield(player1, new NighthawkDarkDefender());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BuckyBarnesEagerAlly()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void nonHeroEntryDoesNotTrigger() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new NighthawkDarkDefender());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentHeroEntryDoesNotTrigger() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new NighthawkDarkDefender());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new BuckyBarnesEagerAlly()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
