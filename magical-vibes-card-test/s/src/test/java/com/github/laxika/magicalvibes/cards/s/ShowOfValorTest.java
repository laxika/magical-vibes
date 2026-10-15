package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.cards.t.TimberpackWolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShowOfValor.class, TimberpackWolf.class, TormodsCrypt.class})
class ShowOfValorTest extends BaseCardTest {

    @Test
    @DisplayName("Show of Valor can boost an opponent's creature without boosting yours")
    void canTargetOpponentCreature() {
        harness.addToBattlefield(player1, new TimberpackWolf());
        harness.addToBattlefield(player2, new TimberpackWolf());
        harness.setHand(player1, List.of(new ShowOfValor()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Timberpack Wolf");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent target = gd.playerBattlefields.get(player2.getId()).getFirst();
        Permanent other = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(target.getEffectivePower()).isEqualTo(4);
        assertThat(target.getEffectiveToughness()).isEqualTo(6);
        assertThat(other.getEffectivePower()).isEqualTo(2);
        assertThat(other.getEffectiveToughness()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Show of Valor");
    }

    @Test
    @DisplayName("Two Show of Valor boosts add together and both expire at cleanup")
    void multipleBoostsStackAndExpire() {
        harness.addToBattlefield(player1, new TimberpackWolf());
        harness.setHand(player1, List.of(new ShowOfValor(), new ShowOfValor()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        UUID targetId = harness.getPermanentId(player1, "Timberpack Wolf");
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent target = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(target.getEffectivePower()).isEqualTo(6);
        assertThat(target.getEffectiveToughness()).isEqualTo(10);

        harness.forceStep(TurnStep.END_STEP);
        assertThat(target.getEffectivePower()).isEqualTo(6);
        assertThat(target.getEffectiveToughness()).isEqualTo(10);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(2);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Resolving Show of Valor gives +2/+4 to target creature")
    void resolvesAndBoostsTarget() {
        harness.addToBattlefield(player1, new TimberpackWolf());
        harness.setHand(player1, List.of(new ShowOfValor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearId = harness.getPermanentId(player1, "Timberpack Wolf");
        harness.castAndResolveInstant(player1, 0, bearId);

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Boost from Show of Valor wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new TimberpackWolf());
        harness.setHand(player1, List.of(new ShowOfValor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearId = harness.getPermanentId(player1, "Timberpack Wolf");
        harness.castAndResolveInstant(player1, 0, bearId);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Show of Valor")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new TimberpackWolf());
        harness.addToBattlefield(player1, new TormodsCrypt());
        harness.setHand(player1, List.of(new ShowOfValor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Tormod's Crypt");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
