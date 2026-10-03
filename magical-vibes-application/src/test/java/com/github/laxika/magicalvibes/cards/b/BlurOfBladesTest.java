package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlurOfBlades.class, GrizzlyBears.class, FugitiveWizard.class, Solemnity.class})
class BlurOfBladesTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a -1/-1 counter on target creature and deals 2 damage to its controller")
    void putsCounterAndDamagesController() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlurOfBlades()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        // Grizzly Bears (2/2) with one -1/-1 counter → 1/1, survives
        Permanent target = findPermanent(player2, "Grizzly Bears");
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(target.getEffectivePower()).isEqualTo(1);
        assertThat(target.getEffectiveToughness()).isEqualTo(1);
        // Controller takes 2 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals 2 damage to controller even when the creature dies from the counter")
    void damagesControllerWhenCreatureDies() {
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setHand(player1, List.of(new BlurOfBlades()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Fugitive Wizard");
        harness.castAndResolveInstant(player1, 0, targetId);

        // Fugitive Wizard (1/1) with one -1/-1 counter → 0/0, dies to SBA
        harness.assertNotOnBattlefield(player2, "Fugitive Wizard");
        harness.assertInGraveyard(player2, "Fugitive Wizard");
        // Controller still takes 2 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Fizzles when target creature is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlurOfBlades()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        harness.assertInGraveyard(player1, "Blur of Blades");
        // No damage to controller when spell fizzles
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Can target your own creature and damages you rather than your opponent")
    void damagesOwnCreaturesController() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BlurOfBlades()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent target = findPermanent(player1, "Grizzly Bears");
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Still deals damage when Solemnity prevents the counter")
    void damagesControllerWhenCounterCannotBePlaced() {
        harness.addToBattlefield(player1, new Solemnity());
        harness.addToBattlefield(player2, new FugitiveWizard());
        harness.setHand(player1, List.of(new BlurOfBlades()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        UUID targetId = harness.getPermanentId(player2, "Fugitive Wizard");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent target = findPermanent(player2, "Fugitive Wizard");
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertNotInGraveyard(player2, "Fugitive Wizard");
        harness.assertLife(player2, 18);
    }
}
