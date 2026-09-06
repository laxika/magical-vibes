package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WelkinTern;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonTempest.class, DragonEgg.class, GrizzlyBears.class, WelkinTern.class})
class DragonTempestTest extends BaseCardTest {

    @Test
    @DisplayName("A flying creature entering gains haste until end of turn")
    void flyingCreatureGainsHaste() {
        harness.addToBattlefield(player1, new DragonTempest());
        harness.setHand(player1, List.of(new WelkinTern()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent tern = findPermanent(player1, "Welkin Tern");
        assertThat(tern.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(tern.hasKeyword(Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("A nonflying creature does not gain haste")
    void nonflyingCreatureDoesNotGainHaste() {
        harness.addToBattlefield(player1, new DragonTempest());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Dragon entering deals damage equal to the number of Dragons controlled")
    void dragonEntryDealsDamageBasedOnDragonCount() {
        harness.addToBattlefield(player1, new DragonTempest());
        harness.addToBattlefield(player1, new DragonEgg());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new DragonEgg()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(victim.getId()));
    }
}
