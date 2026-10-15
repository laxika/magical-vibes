package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DeathWind;
import com.github.laxika.magicalvibes.cards.d.DivineDeflection;
import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.t.TamiyoTheMoonSage;
import com.github.laxika.magicalvibes.cards.v.Vorstclaw;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({PillarOfFlame.class, GrizzlyBears.class, SerraAngel.class, Vorstclaw.class,
        MoorlandInquisitor.class, DeathWind.class, DivineDeflection.class, TamiyoTheMoonSage.class})
class PillarOfFlameTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to target player")
    void dealsTwoDamageToPlayer() {
        harness.setHand(player1, List.of(new PillarOfFlame()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Creature killed by Pillar of Flame is exiled instead of dying")
    void killedCreatureIsExiled() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new PillarOfFlame()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Creature with toughness greater than 2 survives and is not exiled")
    void toughCreatureSurvives() {
        harness.addToBattlefield(player2, new SerraAngel());
        harness.setHand(player1, List.of(new PillarOfFlame()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID targetId = harness.getPermanentId(player2, "Serra Angel");
        harness.castAndResolveSorcery(player1, 0, targetId);

        harness.assertOnBattlefield(player2, "Serra Angel");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .noneMatch(c -> c.getName().equals("Serra Angel"));
    }

    @Test
    @DisplayName("A damaged creature is exiled when its toughness becomes zero later that turn")
    void laterDeathIsReplacedWithExile() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Vorstclaw());
        harness.setHand(player1, List.of(new PillarOfFlame(), new DeathWind()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.assertOnBattlefield(player2, "Vorstclaw");
        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castInstant(player1, 0, 7, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Vorstclaw");
        harness.assertNotInGraveyard(player2, "Vorstclaw");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getId().equals(creature.getCard().getId()));
    }

    @Test
    @DisplayName("Fully prevented damage does not cause a later death to be replaced")
    void preventedDamageDoesNotMarkCreatureForExile() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new PillarOfFlame(), new DeathWind()));
        harness.setHand(player2, List.of(new DivineDeflection()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castSorcery(player1, 0, creature.getId());
        harness.passPriority(player1);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castInstant(player2, 0, 2, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Moorland Inquisitor");
        assertThat(creature.getMarkedDamage()).isZero();
        harness.assertLife(player1, 18);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, 2, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Moorland Inquisitor");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("The exile replacement expires at cleanup")
    void creatureDiesNormallyOnNextTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new Vorstclaw());
        harness.setHand(player1, List.of(new PillarOfFlame(), new DeathWind()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, creature.getId());

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(creature.getMarkedDamage()).isZero();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.castInstant(player1, 0, 7, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Vorstclaw");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Damage removes planeswalker loyalty without exiling a noncreature planeswalker")
    void planeswalkerGoesToGraveyardWhenLoyaltyRunsOut() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new TamiyoTheMoonSage());
        harness.setHand(player1, List.of(new PillarOfFlame(), new PillarOfFlame()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveSorcery(player1, 0, planeswalker.getId());
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.castAndResolveSorcery(player1, 0, planeswalker.getId());

        harness.assertNotOnBattlefield(player2, "Tamiyo, the Moon Sage");
        harness.assertInGraveyard(player2, "Tamiyo, the Moon Sage");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }
}
