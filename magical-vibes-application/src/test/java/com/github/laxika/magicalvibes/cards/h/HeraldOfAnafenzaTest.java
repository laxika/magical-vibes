package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HeraldOfAnafenzaTest extends BaseCardTest {

    @Test
    @DisplayName("Outlast puts a +1/+1 counter on Herald of Anafenza and creates a Warrior token")
    void outlastPutsCounterAndCreatesWarriorToken() {
        Permanent herald = addHeraldReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(herald.isTapped()).isTrue();
        assertThat(herald.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        Permanent token = findPermanent(player1, "Warrior");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.WARRIOR);
    }

    @Test
    @DisplayName("Outlast cannot be activated outside sorcery speed")
    void outlastRequiresSorcerySpeed() {
        addHeraldReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    @Test
    @DisplayName("Only the Herald whose outlast ability was activated creates a Warrior token")
    void onlyActivatedHeraldCreatesToken() {
        addHeraldReady(player1);
        Permanent activatedHerald = addHeraldReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(activatedHerald.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(p -> p.getCard().getName().equals("Warrior"))
                .hasSize(1);
    }

    private Permanent addHeraldReady(Player player) {
        return addCreatureReady(player, new HeraldOfAnafenza());
    }
}
