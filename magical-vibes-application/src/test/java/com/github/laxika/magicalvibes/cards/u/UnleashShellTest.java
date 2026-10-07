package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.cards.t.TeferiTemporalPilgrim;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UnleashShell.class, ArgothianSprite.class, TeferiTemporalPilgrim.class})
class UnleashShellTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage to a creature and 2 damage to its controller")
    void dealsDamageToCreatureAndController() {
        harness.addToBattlefield(player2, new ArgothianSprite());
        castUnleashShell();

        UUID targetId = harness.getPermanentId(player2, "Argothian Sprite");
        harness.castAndResolveInstant(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Argothian Sprite");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Deals 5 damage to a planeswalker and 2 damage to its controller")
    void dealsDamageToPlaneswalkerAndController() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new TeferiTemporalPilgrim());
        planeswalker.setCounterCount(CounterType.LOYALTY, 7);
        castUnleashShell();

        harness.castAndResolveInstant(player1, 0, planeswalker.getId());

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Fizzles when the target leaves before resolution")
    void fizzlesWhenTargetLeaves() {
        harness.addToBattlefield(player2, new ArgothianSprite());
        castUnleashShell();

        UUID targetId = harness.getPermanentId(player2, "Argothian Sprite");
        harness.castInstant(player1, 0, targetId);
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Damages its caster when targeting their own creature")
    void damagesControllerOfOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        castUnleashShell();
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertInGraveyard(player1, "Argothian Sprite");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Still damages the controller when the planeswalker loses all loyalty")
    void damagesControllerOfLethallyDamagedPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new TeferiTemporalPilgrim());
        planeswalker.setCounterCount(CounterType.LOYALTY, 4);
        castUnleashShell();

        harness.castAndResolveInstant(player1, 0, planeswalker.getId());

        harness.assertNotOnBattlefield(player2, "Teferi, Temporal Pilgrim");
        harness.assertInGraveyard(player2, "Teferi, Temporal Pilgrim");
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Marks exactly five damage on a surviving creature")
    void damagesSurvivingCreatureAndController() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        castUnleashShell();

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertOnBattlefield(player2, "Argothian Sprite");
        assertThat(creature.getMarkedDamage()).isEqualTo(5);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Cannot target a player directly")
    void cannotTargetPlayer() {
        castUnleashShell();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);
    }

    private void castUnleashShell() {
        harness.setHand(player1, List.of(new UnleashShell()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);
    }
}
