package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LilianaHereticalHealerTest extends BaseCardTest {

    @Test
    @DisplayName("Another nontoken creature you control dying transforms Liliana and creates a Zombie")
    void allyCreatureDeathTransformsAndCreatesZombie() {
        addLiliana(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        shockPermanent(player1, bears);

        harness.assertNotOnBattlefield(player1, "Liliana, Heretical Healer");
        harness.assertOnBattlefield(player1, "Liliana, Defiant Necromancer");

        Permanent walker = findPermanent(player1, "Liliana, Defiant Necromancer");
        assertThat(walker.isTransformed()).isTrue();
        assertThat(walker.getCounterCount(CounterType.LOYALTY)).isPositive();

        GameData gd = harness.getGameData();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("An opponent's creature dying leaves Liliana untransformed")
    void opponentCreatureDeathDoesNotTransform() {
        addLiliana(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        shockPermanent(player1, bears);

        harness.assertOnBattlefield(player1, "Liliana, Heretical Healer");
        harness.assertNotOnBattlefield(player1, "Liliana, Defiant Necromancer");
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken());
    }

    private void addLiliana(Player player) {
        harness.addToBattlefield(player, new LilianaHereticalHealer());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }

    private void shockPermanent(Player caster, Permanent target) {
        harness.forceActivePlayer(caster);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
