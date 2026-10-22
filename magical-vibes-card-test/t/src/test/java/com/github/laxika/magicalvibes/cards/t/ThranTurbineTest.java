package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AbzanGuide;
import com.github.laxika.magicalvibes.cards.a.AlbinoTroll;
import com.github.laxika.magicalvibes.cards.a.AuguryRaven;
import com.github.laxika.magicalvibes.cards.c.ChimericStaff;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.m.ManaLeak;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThranTurbine.class, ChimericStaff.class, AlbinoTroll.class, Disenchant.class})
class ThranTurbineTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger adds two ability-only colorless mana when accepted")
    void acceptedTriggerAddsAbilityOnlyMana() {
        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            Permanent turbine = harness.addToBattlefieldAndReturn(player1, new ThranTurbine());

            advanceToUpkeep(player1);
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyMana(ManaColor.COLORLESS)).isEqualTo(2);

            harness.setHand(player1, List.of(new Disenchant()));
            harness.addMana(player1, ManaColor.WHITE, 1);
            assertThatThrownBy(() -> harness.castInstant(player1, 0, turbine.getId()))
                    .isInstanceOf(IllegalStateException.class);
            assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyMana(ManaColor.COLORLESS)).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Declining the upkeep trigger adds no mana")
    void declinedTriggerAddsNoMana() {
        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            harness.addToBattlefield(player1, new ThranTurbine());

            advanceToUpkeep(player1);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyManaTotal()).isZero();
        });
    }

    @Test
    @DisplayName("Ability-only mana cannot cast a spell but can pay an activated ability")
    void manaCannotCastSpellButPaysAbility() {
        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            harness.addToBattlefield(player1, new ThranTurbine());
            Permanent staff = harness.addToBattlefieldAndReturn(player1, new ChimericStaff());

            advanceToUpkeep(player1);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);

            harness.activateAbility(player1, 1, 0, 2, null);
            harness.passBothPriorities();

            assertThat(gqs.getEffectivePower(gd, staff)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, staff)).isEqualTo(2);
            assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyManaTotal()).isZero();
        });
    }

    @Test
    @DisplayName("Upkeep trigger fires only during its controller's upkeep")
    void triggerOnlyFiresDuringControllerUpkeep() {
        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            harness.addToBattlefield(player1, new ThranTurbine());

            advanceToUpkeep(player2);
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isNull();
            assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyManaTotal()).isZero();
        });
    }

    @Test
    @DisplayName("Unused Turbine mana empties before the draw step")
    void unusedManaEmptiesAtEndOfUpkeep() {
        harness.addToBattlefield(player1, new ThranTurbine());

        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyManaTotal()).isEqualTo(2);
        });
        // The starting player skips the draw step of the first turn (CR 103.8a), so the next step that
        // exists after upkeep is the precombat main phase.
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyManaTotal()).isZero();
    }

    @Test
    @CardUsed({ThranTurbine.class, AuguryRaven.class})
    @DisplayName("Turbine mana can pay a foretell cost, which is a special action rather than a spell")
    void manaCanPayForetellCost() {
        harness.addToBattlefield(player1, new ThranTurbine());
        AuguryRaven raven = new AuguryRaven();

        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            acceptTurbineMana();
            harness.setHand(player1, List.of(raven));

            harness.foretell(player1, 0);

            assertThat(gd.findExiledCard(raven.getId()).faceDown()).isTrue();
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        });
    }

    @Test
    @CardUsed({ThranTurbine.class, AbzanGuide.class})
    @DisplayName("Turbine mana can pay a morph cost to turn a creature face up")
    void manaCanPayMorphCost() {
        harness.setHand(player1, List.of(new AbzanGuide()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        Permanent guide = findPermanent(player1, "Abzan Guide");
        harness.addToBattlefield(player1, new ThranTurbine());

        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            acceptTurbineMana();
            harness.addMana(player1, ManaColor.WHITE, 1);
            harness.addMana(player1, ManaColor.BLACK, 1);
            harness.addMana(player1, ManaColor.GREEN, 1);

            harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(guide));

            assertThat(guide.isFaceDown()).isFalse();
            assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        });
    }

    @Test
    @DisplayName("Turbine mana can pay the generic portion of an echo cost")
    void manaCanPayEchoCost() {
        harness.setHand(player1, List.of(new AlbinoTroll()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new ThranTurbine());

        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyManaTotal()).isEqualTo(2);

            harness.passBothPriorities();
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.addMana(player1, ManaColor.GREEN, 1);
            harness.handleMayAbilityChosen(player1, true);

            harness.assertOnBattlefield(player1, "Albino Troll");
            assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyManaTotal()).isEqualTo(1);
        });
    }

    @Test
    @CardUsed({ThranTurbine.class, Shock.class, ManaLeak.class})
    @DisplayName("Turbine mana can pay Mana Leak's {3}, which is paid while Mana Leak resolves rather than to cast a spell")
    void manaCanPayManaLeakCost() {
        harness.addToBattlefield(player1, new ThranTurbine());

        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            acceptTurbineMana();
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);
            harness.castInstant(player1, 0, player2.getId());
            Card shock = gd.stack.getFirst().getCard();

            harness.setHand(player2, List.of(new ManaLeak()));
            harness.addMana(player2, ManaColor.BLUE, 1);
            harness.addMana(player2, ManaColor.COLORLESS, 1);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, shock.getId());
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, true);
            harness.passBothPriorities();

            harness.assertLife(player2, 18);
            assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        });
    }

    @Test
    @CardUsed({ThranTurbine.class, Shock.class, ManaLeak.class})
    @DisplayName("Turbine mana alone can't pay more than its two mana toward Mana Leak's {3}")
    void manaLeakCountersWhenTurbineManaFallsShort() {
        harness.addToBattlefield(player1, new ThranTurbine());

        harness.withAutoStop(TurnStep.UPKEEP, () -> {
            acceptTurbineMana();
            harness.setHand(player1, List.of(new Shock()));
            harness.addMana(player1, ManaColor.RED, 1);
            harness.castInstant(player1, 0, player2.getId());
            Card shock = gd.stack.getFirst().getCard();

            harness.setHand(player2, List.of(new ManaLeak()));
            harness.addMana(player2, ManaColor.BLUE, 1);
            harness.addMana(player2, ManaColor.COLORLESS, 1);
            harness.passPriority(player1);
            harness.castInstant(player2, 0, shock.getId());
            harness.passBothPriorities();

            harness.assertInGraveyard(player1, "Shock");
            harness.assertLife(player2, 20);
            assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyMana(ManaColor.COLORLESS)).isEqualTo(2);
        });
    }

    private void acceptTurbineMana() {
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerManaPools.get(player1.getId()).getAbilityOnlyMana(ManaColor.COLORLESS)).isEqualTo(2);
    }
}
