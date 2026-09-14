package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonlairSpider.class, GrizzlyBears.class})
class DragonlairSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent's spell creates a 1/1 green Insect token")
    void opponentSpellCreatesInsect() {
        harness.addToBattlefield(player1, new DragonlairSpider());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent insect = findPermanent(player1, "Insect");
        assertThat(insect).isNotNull();
        assertThat(gqs.getEffectivePower(gd, insect)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, insect)).isEqualTo(1);
    }

    @Test
    @DisplayName("Your own spell does not create an Insect token")
    void ownSpellDoesNotCreateInsect() {
        harness.addToBattlefield(player1, new DragonlairSpider());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Insect")).isZero();
    }
}
