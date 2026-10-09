package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SarkhanTheMasterless;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DomriAnarchOfBolas.class, Cancel.class, GrizzlyBears.class, SerraAngel.class, SarkhanTheMasterless.class})
class DomriAnarchOfBolasTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control get +1/+0")
    void boostsOwnCreatures() {
        addReadyDomri(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent ownBear = findPermanent(player1, "Grizzly Bears");
        Permanent opposingBear = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, ownBear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBear)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingBear)).isEqualTo(2);
    }

    @Test
    @DisplayName("+1 adds a chosen red or green mana and makes creature spells uncounterable this turn")
    void plusOneAddsManaAndMakesCreatureSpellsUncounterableThisTurn() {
        Permanent domri = addReadyDomri(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        assertThat(domri.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);

        GrizzlyBears firstBears = new GrizzlyBears();
        GrizzlyBears secondBears = new GrizzlyBears();
        harness.setHand(player1, List.of(firstBears, secondBears));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setHand(player2, List.of(new Cancel(), new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, firstBears.getId());
        resolveAllTriggers();

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, secondBears.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player2.getId()).stream()
                .filter(card -> card.getName().equals("Cancel"))
                .count()).isEqualTo(2);
    }

    @Test
    @DisplayName("-2 makes a creature you control fight a creature an opponent controls")
    void minusTwoFights() {
        Permanent domri = addReadyDomri(player1);
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID angelId = harness.getPermanentId(player1, "Serra Angel");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(angelId, bearsId));
        harness.passBothPriorities();

        assertThat(domri.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(findPermanent(player1, "Serra Angel").getMarkedDamage()).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("-2 rejects an opponent creature as its first target")
    void minusTwoFirstTargetMustBeControlled() {
        addReadyDomri(player1);
        harness.addToBattlefield(player1, new SerraAngel());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID angelId = harness.getPermanentId(player1, "Serra Angel");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() ->
                harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(bearsId, angelId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("+1 uses the stack and can add red mana")
    void plusOneUsesStackAndAddsRedMana() {
        Permanent domri = addReadyDomri(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(domri.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);

        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("+1 does not prevent countering noncreature spells")
    void plusOneDoesNotProtectNoncreatureSpells() {
        addReadyDomri(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        DomriAnarchOfBolas secondDomri = new DomriAnarchOfBolas();
        harness.setHand(player1, List.of(secondDomri));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castPlaneswalker(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, secondDomri.getId());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Domri, Anarch of Bolas")).isEqualTo(1);
        harness.assertInGraveyard(player1, "Domri, Anarch of Bolas");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Creature spells remain uncounterable after Domri leaves the battlefield")
    void protectionPersistsAfterDomriLeaves() {
        Permanent domri = addReadyDomri(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, domri));

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    @DisplayName("Creature spells can be countered again on the next turn")
    void protectionExpiresAtEndOfTurn() {
        harness.setHand(player1, java.util.List.of());
        harness.setHand(player2, java.util.List.of());
        addReadyDomri(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Fight damage includes Domri's static power boost")
    void fightUsesBoostedPower() {
        addReadyDomri(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(bears.getId(), angel.getId()));
        resolveAllTriggers();

        assertThat(angel.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Serra Angel");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Spending Domri's last loyalty removes the boost before the fight")
    void fightWithoutBoostWhenDomriDiesToLoyaltyCost() {
        Permanent domri = addReadyDomri(player1);
        domri.setCounterCount(CounterType.LOYALTY, 2);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new SerraAngel());

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(bears.getId(), angel.getId()));
        harness.assertNotOnBattlefield(player1, "Domri, Anarch of Bolas");
        resolveAllTriggers();

        assertThat(angel.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Serra Angel");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Domri, Anarch of Bolas");
    }

    @Test
    @DisplayName("No fight occurs when the first target leaves the battlefield")
    void fightDoesNothingWhenFirstTargetLeaves() {
        addReadyDomri(player1);
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(angel.getId(), bears.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, angel));
        resolveAllTriggers();

        assertThat(bears.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("No fight occurs when the second target leaves the battlefield")
    void fightDoesNothingWhenSecondTargetLeaves() {
        addReadyDomri(player1);
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(angel.getId(), bears.getId()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bears));
        resolveAllTriggers();

        assertThat(angel.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Serra Angel");
    }

    @Test
    @DisplayName("-2 rejects a second creature you control")
    void minusTwoSecondTargetMustNotBeControlled() {
        addReadyDomri(player1);
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(angel.getId(), bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Domri boosts himself when Sarkhan makes him a creature")
    void boostsDomriHimselfWhenAnimated() {
        Permanent domri = addReadyDomri(player1);
        Permanent sarkhan = harness.addToBattlefieldAndReturn(player1, new SarkhanTheMasterless());
        sarkhan.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 1, 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, domri)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, domri)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, sarkhan)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, sarkhan)).isEqualTo(4);
    }

    private Permanent addReadyDomri(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new DomriAnarchOfBolas());
        perm.setCounterCount(CounterType.LOYALTY, 3);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
