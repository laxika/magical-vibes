package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.p.PricklyBoggart;
import com.github.laxika.magicalvibes.cards.p.ProwessOfTheFair;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RhysTheExiled.class, ElvishWarrior.class, PricklyBoggart.class, ProwessOfTheFair.class})
class RhysTheExiledTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking gains 1 life when Rhys is the only Elf")
    void gainsOneLifeWhenOnlyElf() {
        addCreatureReady(player1, new RhysTheExiled());
        int startLife = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startLife + 1);
    }

    @Test
    @DisplayName("Attacking gains 1 life for each Elf controlled, including non-attackers")
    void gainsLifePerElf() {
        addCreatureReady(player1, new RhysTheExiled());
        addCreatureReady(player1, new ElvishWarrior());
        addCreatureReady(player1, new ElvishWarrior());
        int startLife = gd.playerLifeTotals.get(player1.getId());

        // Only Rhys attacks; the other Elves stay back but still count.
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startLife + 3);
    }

    @Test
    @DisplayName("Non-Elf creatures do not add life")
    void nonElvesDoNotCount() {
        addCreatureReady(player1, new RhysTheExiled());
        addCreatureReady(player1, new PricklyBoggart());
        int startLife = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startLife + 1);
    }

    @Test
    @DisplayName("Activating the ability sacrifices an Elf and grants a regeneration shield")
    void activatingGrantsRegenerationShield() {
        Permanent rhys = addCreatureReady(player1, new RhysTheExiled());
        harness.addToBattlefield(player1, new ElvishWarrior());
        harness.addMana(player1, ManaColor.BLACK, 1);
        UUID elfId = harness.getPermanentId(player1, "Elvish Warrior");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, elfId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Elvish Warrior");
        assertThat(rhys.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate the regeneration ability without {B}")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new RhysTheExiled());
        harness.addToBattlefield(player1, new ElvishWarrior());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The number of Elves is counted when the attack trigger resolves")
    void countsElvesAtResolution() {
        addCreatureReady(player1, new RhysTheExiled());
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new ElvishWarrior());
        harness.addMana(player1, ManaColor.BLACK, 1);
        int startLife = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, elf.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Elvish Warrior");
        harness.assertLife(player1, startLife + 1);
    }

    @Test
    @DisplayName("Another Elf attacking does not trigger Rhys")
    void anotherElfAttackingDoesNotTriggerRhys() {
        addCreatureReady(player1, new RhysTheExiled());
        addCreatureReady(player1, new ElvishWarrior());
        int startLife = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        harness.assertLife(player1, startLife);
    }

    @Test
    @DisplayName("Opponent's Elves do not contribute to the attack trigger")
    void opposingElvesDoNotCount() {
        addCreatureReady(player1, new RhysTheExiled());
        harness.addToBattlefield(player2, new ElvishWarrior());
        int startLife = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.assertLife(player1, startLife + 1);
    }

    @Test
    @DisplayName("Noncreature Elf permanents contribute to the attack trigger")
    void noncreatureElvesCount() {
        addCreatureReady(player1, new RhysTheExiled());
        harness.addToBattlefield(player1, new ProwessOfTheFair());
        int startLife = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.assertLife(player1, startLife + 2);
    }

    @Test
    @DisplayName("A noncreature Elf can pay the regeneration cost")
    void canSacrificeNoncreatureElf() {
        Permanent rhys = addCreatureReady(player1, new RhysTheExiled());
        Permanent prowess = harness.addToBattlefieldAndReturn(player1, new ProwessOfTheFair());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, prowess.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Prowess of the Fair");
        assertThat(rhys.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Rhys can sacrifice itself but regeneration cannot return it")
    void canSacrificeRhysItself() {
        Permanent rhys = addCreatureReady(player1, new RhysTheExiled());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, rhys.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rhys the Exiled");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }
}
