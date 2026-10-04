package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AltarsReap;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GideonAllyOfZendikar;
import com.github.laxika.magicalvibes.cards.s.ScourFromExistence;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BrutalExpulsion.class, GrizzlyBears.class, HillGiant.class, Island.class, Shock.class,
        GideonAllyOfZendikar.class, ScourFromExistence.class, AltarsReap.class})
class BrutalExpulsionTest extends BaseCardTest {

    @Test
    void returnsTargetCreatureToItsOwnersHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(new int[]{0}, List.of(target.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void returnsTargetSpellToItsOwnersHand() {
        Shock shock = new Shock();
        harness.setHand(player2, List.of(shock));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new BrutalExpulsion()));
        addBrutalExpulsionMana(player1);

        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, player1.getId());
        UUID shockId = gd.stack.getFirst().getCard().getId();

        harness.forceActivePlayer(player1);
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0}, shockId, List.of());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Shock");
        harness.assertNotInGraveyard(player2, "Shock");
    }

    @Test
    void damageModeKillsAndExilesTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(new int[]{1}, List.of(target.getId()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void bothModesResolveAgainstTheirTargets() {
        Permanent creatureToReturn = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent creatureToDamage = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        cast(new int[]{0, 1}, List.of(creatureToReturn.getId(), creatureToDamage.getId()));

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(creatureToDamage.getMarkedDamage()).isEqualTo(2);
        assertThat(creatureToDamage.isExileInsteadOfDieThisTurn()).isTrue();
    }

    @Test
    void bounceModeRejectsAnIslandTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> cast(new int[]{0}, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageModeRejectsAnIslandTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Island());

        assertThatThrownBy(() -> cast(new int[]{1}, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothModesMayTargetTheSameCreatureAndReturnItBeforeDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(new int[]{0, 1}, List.of(target.getId(), target.getId()));

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(target.getMarkedDamage()).isZero();
    }

    @Test
    void secondModeExilesCreatureThatDiesFromLaterDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        cast(new int[]{1}, List.of(target.getId()));
        harness.assertOnBattlefield(player2, "Hill Giant");

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertNotInGraveyard(player2, "Hill Giant");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Hill Giant"));
    }

    @Test
    void damageModeRemovesPlaneswalkerLoyaltyAndExilesItAtZero() {
        Permanent target = harness.enterBattlefieldAndReturn(player2, new GideonAllyOfZendikar());

        cast(new int[]{1}, List.of(target.getId()));
        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Gideon, Ally of Zendikar");
        cast(new int[]{1}, List.of(target.getId()));

        harness.assertNotOnBattlefield(player2, "Gideon, Ally of Zendikar");
        harness.assertNotInGraveyard(player2, "Gideon, Ally of Zendikar");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Gideon, Ally of Zendikar"));
    }

    @Test
    void bounceModeRejectsANoncreaturePlaneswalker() {
        Permanent target = harness.enterBattlefieldAndReturn(player2, new GideonAllyOfZendikar());

        assertThatThrownBy(() -> cast(new int[]{0}, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void damageModeRejectsAPlayer() {
        assertThatThrownBy(() -> cast(new int[]{1}, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bounceStillResolvesWhenDamageTargetLeavesTheBattlefield() {
        Permanent bounceTarget = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent damageTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BrutalExpulsion()));
        addBrutalExpulsionMana(player1);
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                bounceTarget.getId(), List.of(damageTarget.getId()));

        harness.setHand(player2, List.of(new ScourFromExistence()));
        harness.addMana(player2, ManaColor.COLORLESS, 7);
        harness.castAndResolveInstant(player2, 0, damageTarget.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Hill Giant");
        assertThat(bounceTarget.getMarkedDamage()).isZero();
        harness.assertNotInGraveyard(player2, "Hill Giant");
    }

    @Test
    void damageStillResolvesWhenBounceTargetLeavesTheBattlefield() {
        Permanent bounceTarget = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent damageTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new BrutalExpulsion()));
        addBrutalExpulsionMana(player1);
        harness.castModalInstantWithModes(player1, 0, 1, 2, new int[]{0, 1},
                bounceTarget.getId(), List.of(damageTarget.getId()));

        harness.setHand(player2, List.of(new ScourFromExistence()));
        harness.addMana(player2, ManaColor.COLORLESS, 7);
        harness.castAndResolveInstant(player2, 0, bounceTarget.getId());
        harness.passBothPriorities();

        harness.assertNotInHand(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void preventedDamageStillExilesTheTargetWhenItIsSacrificed() {
        Permanent target = harness.enterBattlefieldAndReturn(player1, new GideonAllyOfZendikar());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        cast(new int[]{1}, List.of(target.getId()));

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        harness.setHand(player1, List.of(new AltarsReap()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstantWithSacrifice(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Gideon, Ally of Zendikar");
        harness.assertNotInGraveyard(player1, "Gideon, Ally of Zendikar");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Gideon, Ally of Zendikar"));
    }

    @Test
    void returnsANoncreaturePermanentSpellFromTheStack() {
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new GideonAllyOfZendikar(), "{2}{W}{W}");
        UUID spellId = gd.stack.getLast().getCard().getId();

        cast(new int[]{0}, List.of(spellId));

        harness.assertInHand(player2, "Gideon, Ally of Zendikar");
        harness.assertNotOnBattlefield(player2, "Gideon, Ally of Zendikar");
        harness.assertNotInGraveyard(player2, "Gideon, Ally of Zendikar");
    }

    @Test
    void exileReplacementExpiresAfterTheTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        cast(new int[]{1}, List.of(target.getId()));

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new AltarsReap()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstantWithSacrifice(player1, 0, null, target.getId());

        harness.assertInGraveyard(player1, "Hill Giant");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private void cast(int[] modes, List<UUID> targetIds) {
        harness.setHand(player1, List.of(new BrutalExpulsion()));
        addBrutalExpulsionMana(player1);
        if (java.util.Arrays.stream(modes).anyMatch(mode -> mode == 0)) {
            harness.castModalInstantWithModes(player1, 0, 1, 2, modes,
                    targetIds.getFirst(), targetIds.subList(1, targetIds.size()));
        } else {
            harness.castModalInstantWithModes(player1, 0, 1, 2, modes, targetIds);
        }
        harness.passBothPriorities();
    }

    private void addBrutalExpulsionMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }
}
