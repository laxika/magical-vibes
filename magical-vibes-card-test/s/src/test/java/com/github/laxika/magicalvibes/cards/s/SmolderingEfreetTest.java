package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.r.RealityShift;
import com.github.laxika.magicalvibes.cards.w.WildSlash;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SmolderingEfreet.class, Murder.class, WildSlash.class, RealityShift.class})
class SmolderingEfreetTest extends BaseCardTest {

    @Test
    @DisplayName("When Smoldering Efreet dies, it deals 2 damage to its controller")
    void dealsDamageToItsControllerWhenItDies() {
        Permanent efreet = harness.addToBattlefieldAndReturn(player1, new SmolderingEfreet());
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, efreet.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Smoldering Efreet does not deal damage while it remains on the battlefield")
    void doesNotDealDamageWithoutDying() {
        harness.addToBattlefield(player1, new SmolderingEfreet());
        harness.setLife(player1, 20);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Death damage waits for the trigger to resolve and damages the opponent controlling the Efreet")
    void damagesOpponentControllerAfterLethalDamage() {
        Permanent efreet = harness.addToBattlefieldAndReturn(player2, new SmolderingEfreet());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new WildSlash()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, efreet.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(efreet);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(efreet.getCard());
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Exiling Smoldering Efreet does not trigger its death ability")
    void doesNotDealDamageWhenExiled() {
        Permanent efreet = harness.addToBattlefieldAndReturn(player2, new SmolderingEfreet());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setLibrary(player2, List.of(new SmolderingEfreet()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new RealityShift()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveInstant(player1, 0, efreet.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(efreet);
        assertThat(gd.exiledCards).anySatisfy(entry -> assertThat(entry.card()).isEqualTo(efreet.getCard()));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
