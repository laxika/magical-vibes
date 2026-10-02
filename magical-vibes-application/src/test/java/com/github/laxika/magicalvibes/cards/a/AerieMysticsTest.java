package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ConstrictingTendrils;
import com.github.laxika.magicalvibes.cards.v.ValeronOutlander;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AerieMystics.class, ValeronOutlander.class, ConstrictingTendrils.class})
class AerieMysticsTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants shroud to creatures you control, not the opponent's")
    void grantsShroudToOwnCreatures() {
        Permanent mystics = addCreatureReady(player1, new AerieMystics());
        Permanent ally = addCreatureReady(player1, new ValeronOutlander());
        Permanent enemy = addCreatureReady(player2, new ValeronOutlander());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mystics.hasKeyword(Keyword.SHROUD)).isTrue();
        assertThat(ally.hasKeyword(Keyword.SHROUD)).isTrue();
        assertThat(enemy.hasKeyword(Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Shroud wears off at end of turn")
    void shroudWearsOffAtEndOfTurn() {
        Permanent mystics = addCreatureReady(player1, new AerieMystics());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mystics.hasKeyword(Keyword.SHROUD)).isTrue();

        harness.passUntil(TurnStep.UPKEEP);

        assertThat(mystics.hasKeyword(Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Ability requires {1}{G}{U}")
    void requiresEnoughMana() {
        addCreatureReady(player1, new AerieMystics());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void affectsCreaturesPresentAtResolutionOnly() {
        Permanent mystics = harness.addToBattlefieldAndReturn(player1, new AerieMystics());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThat(mystics.hasKeyword(Keyword.SHROUD)).isFalse();
        Permanent arrivingBeforeResolution = harness.addToBattlefieldAndReturn(player1, new ValeronOutlander());
        harness.passBothPriorities();
        Permanent arrivingAfterResolution = harness.addToBattlefieldAndReturn(player1, new ValeronOutlander());

        assertThat(mystics.hasKeyword(Keyword.SHROUD)).isTrue();
        assertThat(arrivingBeforeResolution.hasKeyword(Keyword.SHROUD)).isTrue();
        assertThat(arrivingAfterResolution.hasKeyword(Keyword.SHROUD)).isFalse();
    }

    @Test
    void canActivateWhileSummoningSickTappedAndAlreadyShrouded() {
        Permanent mystics = harness.addToBattlefieldAndReturn(player1, new AerieMystics());
        mystics.setTapped(true);
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new ValeronOutlander());
        assertThat(lateCreature.hasKeyword(Keyword.SHROUD)).isFalse();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(mystics.hasKeyword(Keyword.SHROUD)).isTrue();
        assertThat(lateCreature.hasKeyword(Keyword.SHROUD)).isTrue();
        assertThat(mystics.isTapped()).isTrue();
    }

    @Test
    void shroudPreventsBothPlayersFromTargetingProtectedCreature() {
        Permanent mystics = addCreatureReady(player1, new AerieMystics());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.setHand(player1, List.of(new ConstrictingTendrils()));
        harness.setHand(player2, List.of(new ConstrictingTendrils()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, mystics.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
        assertThatThrownBy(() -> harness.castInstant(player2, 0, mystics.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    void gainingShroudInResponseMakesTargetedSpellFailToResolve() {
        Permanent mystics = addCreatureReady(player1, new AerieMystics());
        int originalPower = mystics.getEffectivePower();
        harness.setHand(player1, List.of(new ConstrictingTendrils()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castInstant(player1, 0, mystics.getId());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(mystics.hasKeyword(Keyword.SHROUD)).isTrue();
        harness.passBothPriorities();

        assertThat(mystics.getEffectivePower()).isEqualTo(originalPower);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Constricting Tendrils");
    }
}
