package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BlackLotus;
import com.github.laxika.magicalvibes.cards.b.Braingeyser;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.r.Regrowth;
import com.github.laxika.magicalvibes.cards.s.ShivanDragon;
import com.github.laxika.magicalvibes.cards.t.Terror;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GarthOneEye.class, Disenchant.class, Braingeyser.class, Terror.class,
        ShivanDragon.class, Regrowth.class, BlackLotus.class})
class GarthOneEyeTest extends BaseCardTest {

    @Test
    void choosesAnUnchosenCardAndOffersItsCopyForNormalCost() {
        Permanent garth = addCreatureReady(player1, new GarthOneEye());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactlyInAnyOrder(
                "Disenchant", "Braingeyser", "Terror", "Shivan Dragon", "Regrowth", "Black Lotus");

        harness.handleListChoice(player1, "Shivan Dragon");
        assertThat(garth.getChosenModeLabels()).containsExactly("Shivan Dragon");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Shivan Dragon");
    }

    @Test
    void doesNotOfferANameThatWasAlreadyChosen() {
        Permanent garth = addCreatureReady(player1, new GarthOneEye());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Black Lotus");
        harness.handleMayAbilityChosen(player1, false);

        garth.untap();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactlyInAnyOrder(
                "Disenchant", "Braingeyser", "Terror", "Shivan Dragon", "Regrowth");
        assertThat(choice.options()).doesNotContain("Black Lotus");
    }

    @Test
    void blackLotusCopyCanBeSacrificedForThreeMana() {
        addCreatureReady(player1, new GarthOneEye());
        chooseCopy("Black Lotus");
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Black Lotus").getCard().isToken()).isTrue();
        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        harness.assertNotOnBattlefield(player1, "Black Lotus");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
        harness.assertNotInGraveyard(player1, "Black Lotus");
    }

    @Test
    void shivanDragonCopyRetainsItsFirebreathingAbility() {
        addCreatureReady(player1, new GarthOneEye());
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        chooseCopy("Shivan Dragon");
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent dragon = findPermanent(player1, "Shivan Dragon");
        assertThat(dragon.getCard().isToken()).isTrue();
        int initialPower = gqs.getEffectivePower(gd, dragon);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, dragon)).isEqualTo(initialPower + 1);
    }

    @Test
    void braingeyserAllowsChoosingANonzeroXWhenPayingItsCost() {
        addCreatureReady(player1, new GarthOneEye());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        chooseCopy("Braingeyser");
        harness.handleMayAbilityChosen(player1, true);

        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, player2.getId());
        }
        assertThat(gd.interaction.activeInteraction()).isInstanceOfAny(
                PendingInteraction.XValueChoice.class, PendingInteraction.AlternateCastXValueChoice.class);
    }

    @Test
    void disenchantCopyDestroysAnOpponentsArtifact() {
        addCreatureReady(player1, new GarthOneEye());
        Permanent lotus = harness.addToBattlefieldAndReturn(player2, new BlackLotus());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        chooseCopy("Disenchant");
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, lotus.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Black Lotus");
        harness.assertInGraveyard(player2, "Black Lotus");
        harness.assertNotInGraveyard(player1, "Disenchant");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void terrorCopyDestroysALegalCreature() {
        addCreatureReady(player1, new GarthOneEye());
        Permanent dragon = harness.addToBattlefieldAndReturn(player2, new ShivanDragon());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        chooseCopy("Terror");
        harness.handleMayAbilityChosen(player1, true);
        PendingInteraction.PermanentChoice targets =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targets.validPermanentIds()).contains(dragon.getId())
                .doesNotContain(findPermanent(player1, "Garth One-Eye").getId());
        harness.handlePermanentChosen(player1, dragon.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Shivan Dragon");
        harness.assertInGraveyard(player2, "Shivan Dragon");
        harness.assertNotInGraveyard(player1, "Terror");
    }

    @Test
    void regrowthCopyReturnsACardFromItsControllersGraveyard() {
        addCreatureReady(player1, new GarthOneEye());
        BlackLotus lotus = new BlackLotus();
        harness.setGraveyard(player1, List.of(lotus));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        chooseCopy("Regrowth");
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, lotus.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Black Lotus");
        harness.assertNotInGraveyard(player1, "Black Lotus");
        harness.assertNotInGraveyard(player1, "Regrowth");
    }

    @Test
    void decliningAllSixNamesExhaustsTheAbility() {
        Permanent garth = addCreatureReady(player1, new GarthOneEye());
        for (String name : List.of("Disenchant", "Braingeyser", "Terror",
                "Shivan Dragon", "Regrowth", "Black Lotus")) {
            garth.untap();
            chooseCopy(name);
            harness.handleMayAbilityChosen(player1, false);
        }
        garth.untap();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(garth);
    }

    @Test
    void cannotCastShivanDragonWithoutPayingItsManaCost() {
        addCreatureReady(player1, new GarthOneEye());
        chooseCopy("Shivan Dragon");
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "Shivan Dragon");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void summoningSickGarthCannotActivate() {
        harness.addToBattlefield(player1, new GarthOneEye());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void regrowthWithoutAGraveyardTargetStillConsumesItsName() {
        Permanent garth = addCreatureReady(player1, new GarthOneEye());
        harness.setGraveyard(player1, List.of());
        chooseCopy("Regrowth");
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        garth.untap();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).doesNotContain("Regrowth");
    }

    @Test
    void anotherGarthDoesNotShareChosenNames() {
        addCreatureReady(player1, new GarthOneEye());
        addCreatureReady(player2, new GarthOneEye());
        chooseCopy("Black Lotus");
        harness.handleMayAbilityChosen(player1, false);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactlyInAnyOrder(
                "Disenchant", "Braingeyser", "Terror", "Shivan Dragon", "Regrowth", "Black Lotus");
    }

    @Test
    void canCastAPermanentCopyOnTheOpponentsTurn() {
        addCreatureReady(player1, new GarthOneEye());
        harness.forceActivePlayer(player2);
        chooseCopy("Black Lotus");
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Black Lotus");
        assertThat(findPermanent(player1, "Black Lotus").getCard().isToken()).isTrue();
    }

    private void chooseCopy(String name) {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, name);
    }
}
