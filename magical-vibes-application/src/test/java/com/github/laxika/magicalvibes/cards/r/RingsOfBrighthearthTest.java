package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.h.HordeOfNotions;
import com.github.laxika.magicalvibes.cards.m.MoongloveExtract;
import com.github.laxika.magicalvibes.cards.s.Smokebraider;
import com.github.laxika.magicalvibes.cards.s.StreetWraith;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RingsOfBrighthearth.class, ProdigalPyromancer.class, LlanowarElves.class,
        MoongloveExtract.class, HordeOfNotions.class, Smokebraider.class, StreetWraith.class})
class RingsOfBrighthearthTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2} copies the activated ability — target takes damage twice")
    void payingCopiesAbility() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new RingsOfBrighthearth());
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.addMana(player1, ManaColor.RED, 2);

        int pyroIndex = harness.getGameData().playerBattlefields.get(player1.getId()).size() - 1;
        harness.activateAbility(player1, pyroIndex, null, player2.getId());

        // Rings' trigger resolves first, offering "pay {2} to copy".
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);   // pay {2}, copy created
        harness.handleMayAbilityChosen(player1, false);  // keep the copy's original target

        // Resolve the copy, then the original ability.
        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Declining to pay leaves the ability uncopied — target takes damage once")
    void decliningDoesNotCopy() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new RingsOfBrighthearth());
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.addMana(player1, ManaColor.RED, 2);

        int pyroIndex = harness.getGameData().playerBattlefields.get(player1.getId()).size() - 1;
        harness.activateAbility(player1, pyroIndex, null, player2.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);  // decline to pay

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The copy may be given a new target")
    void copyMayChooseNewTarget() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new RingsOfBrighthearth());
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addToBattlefield(player2, new LlanowarElves());

        int pyroIndex = harness.getGameData().playerBattlefields.get(player1.getId()).size() - 1;
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        harness.activateAbility(player1, pyroIndex, null, player2.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);   // pay {2}
        harness.handleMayAbilityChosen(player1, true);   // choose new targets for the copy
        harness.handlePermanentChosen(player1, elvesId); // copy now hits the Elves

        harness.passBothPriorities();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        // Copy killed the 1/1; the original ability still hit player2 for 1.
        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Mana abilities do not trigger Rings of Brighthearth")
    void manaAbilityDoesNotTrigger() {
        harness.addToBattlefield(player1, new RingsOfBrighthearth());
        Permanent elves = addCreatureReady(player1, new LlanowarElves());

        int elvesIndex = harness.getGameData().playerBattlefields.get(player1.getId()).indexOf(elves);
        harness.tapPermanent(player1, elvesIndex);

        GameData gd = harness.getGameData();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's activated ability does not trigger Rings")
    void opponentActivationDoesNotTrigger() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new RingsOfBrighthearth());
        harness.addToBattlefield(player2, new MoongloveExtract());

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A sacrificed source's ability can be copied without paying its costs again")
    void copiesAbilityOfSacrificedSource() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new RingsOfBrighthearth());
        harness.addToBattlefield(player1, new MoongloveExtract());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.assertInGraveyard(player1, "Moonglove Extract");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each Rings permits one separately paid copy of the same activation")
    void multipleRingsCopyIndependently() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new RingsOfBrighthearth());
        harness.addToBattlefield(player1, new RingsOfBrighthearth());
        harness.addToBattlefield(player1, new MoongloveExtract());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 2, null, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cycling triggers Rings and its copy draws without paying life or discarding again")
    void copiesCyclingAbility() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new RingsOfBrighthearth());
        harness.setHand(player1, List.of(new StreetWraith()));
        harness.setLibrary(player1, List.of(new MoongloveExtract(), new MoongloveExtract()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Street Wraith");
        harness.assertLife(player1, 18);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The copied ability may target a different card in the graveyard")
    void copyMayChooseNewGraveyardTarget() {
        harness.addToBattlefield(player1, new RingsOfBrighthearth());
        harness.addToBattlefield(player1, new HordeOfNotions());
        Smokebraider originalTarget = new Smokebraider();
        Smokebraider copyTarget = new Smokebraider();
        harness.setGraveyard(player1, List.of(originalTarget, copyTarget));
        for (ManaColor color : List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK,
                ManaColor.RED, ManaColor.GREEN)) {
            harness.addMana(player1, color, 1);
        }
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, 0, null, originalTarget.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, copyTarget.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == originalTarget)
                .anyMatch(p -> p.getCard() == copyTarget);
    }
}
