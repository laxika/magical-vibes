package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GarrukPrimalHunter;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrittleBlast.class, DoomBlade.class, GrizzlyBears.class,
        GarrukPrimalHunter.class, TurnToFrog.class, Unsummon.class})
class BrittleBlastTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 5 damage and exiles opposing creatures that would die")
    void grantsPerpetualExileInsteadOfDying() {
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BrittleBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, firstTarget.getId());

        GameData gameData = harness.getGameData();
        assertThat(gameData.exiledCards).anyMatch(entry -> entry.card().getId().equals(firstTarget.getCard().getId()));
        assertThat(gameData.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(firstTarget.getCard().getId()));

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, secondTarget.getId());

        assertThat(gameData.exiledCards).anyMatch(entry -> entry.card().getId().equals(secondTarget.getCard().getId()));
        assertThat(gameData.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(secondTarget.getCard().getId()));
    }

    @Test
    @DisplayName("Does not grant the replacement effect to the caster's creatures")
    void doesNotGrantToOwnCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new BrittleBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getCard().getId()));
        assertThat(gameData.exiledCards)
                .noneMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new BrittleBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void dealsExactlyFiveDamageToPlaneswalker() {
        Permanent target = harness.enterBattlefieldAndReturn(player2, new GarrukPrimalHunter());
        target.setCounterCount(CounterType.LOYALTY, 6);
        harness.setHand(player1, List.of(new BrittleBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    void exilesPlaneswalkerWithLethalDamage() {
        Permanent target = harness.enterBattlefieldAndReturn(player2, new GarrukPrimalHunter());
        target.setCounterCount(CounterType.LOYALTY, 3);
        harness.setHand(player1, List.of(new BrittleBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(target.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(target.getCard().getId()));
    }

    @Test
    void grantsReplacementToUntargetedOpposingPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.enterBattlefieldAndReturn(player2, new GarrukPrimalHunter());
        harness.setHand(player1, List.of(new BrittleBlast()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, target.getId());
        other.setCounterCount(CounterType.LOYALTY, 0);
        harness.runStateBasedActions();

        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(other.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(other.getCard().getId()));
    }

    @Test
    void illegalTargetPreventsPerpetualGrantToOtherCreatures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BrittleBlast(), new DoomBlade()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, other.getId());

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(other.getCard().getId()));
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(other.getCard().getId()));
    }

    @Test
    void perpetualGrantSurvivesReturningToHandAndBeingRecast() {
        harness.setHand(player2, List.of());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BrittleBlast(), new Unsummon(), new DoomBlade()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, other.getId());
        harness.assertInHand(player2, "Grizzly Bears");

        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(other.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(other.getCard().getId()));
    }

    @Test
    void losingAbilitiesRemovesGrantedExileReplacementUntilEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BrittleBlast(), new TurnToFrog(), new DoomBlade()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());
        harness.castAndResolveInstant(player1, 0, other.getId());
        harness.castAndResolveInstant(player1, 0, other.getId());

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(other.getCard().getId()));
        assertThat(gd.exiledCards).noneMatch(entry -> entry.card().getId().equals(other.getCard().getId()));
    }
}
