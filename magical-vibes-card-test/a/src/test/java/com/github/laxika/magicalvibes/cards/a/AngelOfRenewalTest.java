package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CliffsideLookout;
import com.github.laxika.magicalvibes.cards.s.ScourFromExistence;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelOfRenewal.class, GrizzlyBears.class, CliffsideLookout.class, ScourFromExistence.class})
class AngelOfRenewalTest extends BaseCardTest {

    @Test
    void entersAndGainsLifeForEachCreatureYouControl() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AngelOfRenewal()));
        harness.setLife(player1, 20);
        addManaForAngel();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
    }

    @Test
    void countsTheAngelThatEntered() {
        harness.setHand(player1, List.of(new AngelOfRenewal()));
        harness.setLife(player1, 20);
        addManaForAngel();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(21);
    }

    @Test
    void countsCreaturesAtResolutionRatherThanWhenItEnters() {
        harness.setHand(player1, List.of(new AngelOfRenewal()));
        harness.setLife(player1, 20);
        addManaForAngel();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);

        harness.addToBattlefield(player1, new CliffsideLookout());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    void gainsNoLifeWhenTheOnlyCreatureLeavesBeforeTheTriggerResolves() {
        harness.setHand(player1, List.of(new AngelOfRenewal()));
        harness.setHand(player2, List.of(new ScourFromExistence()));
        harness.setLife(player1, 20);
        addManaForAngel();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        var angel = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addMana(player2, ManaColor.COLORLESS, 7);
        harness.castAndResolveInstant(player2, 0, angel.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    private void addManaForAngel() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
