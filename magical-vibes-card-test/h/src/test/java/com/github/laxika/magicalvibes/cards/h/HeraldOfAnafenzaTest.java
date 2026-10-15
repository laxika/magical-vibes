package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DragonThroneOfTarkir;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeraldOfAnafenza.class, DragonThroneOfTarkir.class})
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
        assertThat(countPermanents(player1, "Warrior")).isEqualTo(1);
    }

    @Test
    @DisplayName("The Warrior trigger resolves before the outlast counter")
    void warriorResolvesBeforeCounter() {
        Permanent herald = addHeraldReady(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(herald.isTapped()).isTrue();
        assertThat(countPermanents(player1, "Warrior")).isZero();
        assertThat(herald.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Warrior")).isEqualTo(1);
        assertThat(herald.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passBothPriorities();
        assertThat(herald.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player2, "Warrior")).isZero();
    }

    @Test
    @DisplayName("Activating an ability granted by Dragon Throne does not trigger Herald")
    void grantedAbilityDoesNotCreateWarrior() {
        Permanent herald = addHeraldReady(player1);
        Permanent throne = new Permanent(new DragonThroneOfTarkir());
        gd.playerBattlefields.get(player1.getId()).add(throne);
        throne.setAttachedTo(herald.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(herald.isTapped()).isTrue();
        assertThat(countPermanents(player1, "Warrior")).isZero();
        assertThat(herald.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A summoning-sick Herald cannot pay the tap cost of outlast")
    void summoningSicknessPreventsOutlast() {
        Permanent herald = addHeraldReady(player1);
        herald.setSummoningSick(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(herald.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Warrior")).isZero();
    }

    private Permanent addHeraldReady(Player player) {
        return addCreatureReady(player, new HeraldOfAnafenza());
    }
}
