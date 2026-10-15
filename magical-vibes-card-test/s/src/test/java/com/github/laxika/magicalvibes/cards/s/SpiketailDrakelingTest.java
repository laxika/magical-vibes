package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.p.PrismaticLens;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiketailDrakeling.class, PrismaticLens.class})
class SpiketailDrakelingTest extends BaseCardTest {

    @Test
    void sacrificeCountersSpellWhenItsControllerCannotPay() {
        harness.addToBattlefield(player1, new SpiketailDrakeling());

        PrismaticLens lens = new PrismaticLens();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, lens, "{2}");
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, lens.getId());
        harness.assertInGraveyard(player1, "Spiketail Drakeling");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Spiketail Drakeling");
        harness.assertInGraveyard(player2, "Prismatic Lens");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetPermanent() {
        harness.addToBattlefield(player1, new SpiketailDrakeling());
        var lens = harness.addToBattlefieldAndReturn(player2, new PrismaticLens());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, lens.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithoutSpellTarget() {
        harness.addToBattlefield(player1, new SpiketailDrakeling());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void spellSurvivesWhenItsControllerPaysTwoMana() {
        harness.addToBattlefield(player1, new SpiketailDrakeling());

        PrismaticLens lens = new PrismaticLens();
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, lens, "{2}");
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, lens.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Prismatic Lens");
        harness.assertInGraveyard(player1, "Spiketail Drakeling");
    }

    @Test
    void spellIsCounteredWhenItsControllerDeclinesToPay() {
        harness.addToBattlefield(player1, new SpiketailDrakeling());

        PrismaticLens lens = new PrismaticLens();
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, lens, "{2}");
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, lens.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spiketail Drakeling");
        harness.assertInGraveyard(player2, "Prismatic Lens");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canSacrificeWhileTappedAndSummoningSick() {
        var drakeling = harness.addToBattlefieldAndReturn(player1, new SpiketailDrakeling());
        drakeling.tap();
        drakeling.setSummoningSick(true);
        PrismaticLens lens = new PrismaticLens();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, lens, "{2}");
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, lens.getId());
        harness.assertNotOnBattlefield(player1, "Spiketail Drakeling");
        harness.assertInGraveyard(player1, "Spiketail Drakeling");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Prismatic Lens");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canCounterOwnSpell() {
        harness.addToBattlefield(player1, new SpiketailDrakeling());
        PrismaticLens lens = new PrismaticLens();
        harness.castFromHand(player1, lens, "{2}");

        harness.activateAbility(player1, 0, null, lens.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spiketail Drakeling");
        harness.assertInGraveyard(player1, "Prismatic Lens");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void controllerWithUntappedManaSourcesIsOfferedPaymentDuringResolution() {
        harness.addToBattlefield(player1, new SpiketailDrakeling());
        harness.addToBattlefield(player2, new PrismaticLens());
        harness.addToBattlefield(player2, new PrismaticLens());
        PrismaticLens spell = new PrismaticLens();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, spell, "{2}");
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, spell.getId());
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Prismatic Lens");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
    }
}
