package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DragonWhelp;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.UginEyeOfTheStorms;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({MoltenExhale.class, DragonWhelp.class, GrizzlyBears.class, UginEyeOfTheStorms.class})
class MoltenExhaleTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to a target creature when a Dragon is beheld")
    void dealsDamageToCreatureWithBeheldDragon() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new DragonWhelp());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MoltenExhale()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstantWithBehold(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"),
                List.of(dragon.getId()), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("A Dragon card in hand allows the spell to be cast at instant speed")
    void dragonCardInHandAllowsFlashTiming() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MoltenExhale(), new DragonWhelp()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        harness.castInstantWithBehold(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"),
                List.of(), List.of(1));

        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Casting at instant speed requires beholding a Dragon")
    void instantSpeedCastRequiresBeholdingDragon() {
        harness.addToBattlefield(player1, new DragonWhelp());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MoltenExhale()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must behold a dragon");
    }

    @Test
    @DisplayName("Without a Dragon, the sorcery keeps normal timing")
    void noDragonKeepsSorceryTiming() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MoltenExhale()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player1, 0,
                harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Normal sorcery casting does not require a Dragon")
    void castsWithoutDragonDuringMainPhase() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MoltenExhale()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Molten Exhale");
    }

    @Test
    @DisplayName("Having a Dragon does not require beholding it at sorcery timing")
    void canDeclineBeholdDuringMainPhase() {
        harness.addToBattlefield(player1, new DragonWhelp());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MoltenExhale()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Dragon Whelp");
    }

    @Test
    @DisplayName("A tapped Dragon can be beheld on an opponent's turn and remains in play")
    void tappedDragonAllowsCastingOnOpponentsTurn() {
        Permanent dragon = harness.addToBattlefieldAndReturn(player1, new DragonWhelp());
        dragon.setTapped(true);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MoltenExhale()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        harness.castInstantWithBehold(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"),
                List.of(dragon.getId()), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Dragon Whelp");
        assertThat(dragon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Deals exactly 4 damage to a planeswalker")
    void dealsFourDamageToPlaneswalker() {
        Permanent ugin = harness.enterBattlefieldAndReturn(player2, new UginEyeOfTheStorms());
        harness.setHand(player1, List.of(new MoltenExhale()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, ugin.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Ugin, Eye of the Storms");
        assertThat(ugin.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new MoltenExhale()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
