package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BroodButcher.class, GrizzlyBears.class, HillGiant.class, Forest.class})
class BroodButcherTest extends BaseCardTest {

    @Test
    @DisplayName("When Brood Butcher enters, it creates an Eldrazi Scion token")
    void enteringCreatesEldraziScion() {
        castBroodButcher();

        assertThat(findPermanents(player1, "Eldrazi Scion")).hasSize(1);
    }

    @Test
    @DisplayName("An Eldrazi Scion can be sacrificed to add colorless mana")
    void scionCanBeSacrificedForColorlessMana() {
        castBroodButcher();

        Permanent scion = findPermanents(player1, "Eldrazi Scion").getFirst();
        int scionIndex = gd.playerBattlefields.get(player1.getId()).indexOf(scion);
        harness.activateAbility(player1, scionIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
    }

    @Test
    @DisplayName("Sacrificing a creature gives a target creature -2/-2")
    void sacrificesCreatureForMinusTwoMinusTwo() {
        Permanent butcher = addCreatureReady(player1, new BroodButcher());
        Permanent sacrificialCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new HillGiant());
        addAbilityMana();

        int butcherIndex = gd.playerBattlefields.get(player1.getId()).indexOf(butcher);
        harness.activateAbility(player1, butcherIndex, 0, null, target.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, sacrificialCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrificialCreature);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The -2/-2 wears off at end of turn")
    void minusTwoMinusTwoWearsOffAtEndOfTurn() {
        Permanent butcher = addCreatureReady(player1, new BroodButcher());
        Permanent target = addCreatureReady(player2, new HillGiant());
        Permanent sacrificialCreature = addCreatureReady(player1, new GrizzlyBears());
        addAbilityMana();

        int butcherIndex = gd.playerBattlefields.get(player1.getId()).indexOf(butcher);
        harness.activateAbility(player1, butcherIndex, 0, null, target.getId());
        harness.handlePermanentChosen(player1, sacrificialCreature.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("The ability cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent butcher = addCreatureReady(player1, new BroodButcher());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        addAbilityMana();

        int butcherIndex = gd.playerBattlefields.get(player1.getId()).indexOf(butcher);
        assertThatThrownBy(() -> harness.activateAbility(player1, butcherIndex, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Brood Butcher can sacrifice itself and its ability still resolves")
    void canSacrificeItself() {
        Permanent butcher = harness.addToBattlefieldAndReturn(player1, new BroodButcher());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BroodButcher());
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, butcher.getId());
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(butcher);
        harness.assertInGraveyard(player1, "Brood Butcher");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Scion is a 1/1 Eldrazi Scion and its mana ability resolves immediately")
    void scionHasExpectedCharacteristicsAndImmediateManaAbility() {
        castBroodButcher();
        Permanent scion = findPermanent(player1, "Eldrazi Scion");

        assertThat(gqs.getEffectivePower(gd, scion)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, scion)).isEqualTo(1);
        assertThat(scion.getCard().getColors()).isEmpty();
        assertThat(scion.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ELDRAZI, CardSubtype.SCION);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(scion), 0, null, null);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
    }

    @Test
    @DisplayName("The target can be sacrificed to pay the cost, leaving the ability with no legal target")
    void canSacrificeTheTarget() {
        castBroodButcher();
        Permanent butcher = findPermanent(player1, "Brood Butcher");
        Permanent scion = findPermanent(player1, "Eldrazi Scion");
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, scion.getId());
        harness.handlePermanentChosen(player1, scion.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
        assertThat(gqs.getEffectivePower(gd, butcher)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, butcher)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Sacrificing the Scion as a cost does not activate its mana ability")
    void scionSacrificedAsCostDoesNotProduceMana() {
        castBroodButcher();
        Permanent scion = findPermanent(player1, "Eldrazi Scion");
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BroodButcher());
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handlePermanentChosen(player1, scion.getId());

        assertThat(findPermanents(player1, "Eldrazi Scion")).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Brood Butcher can target itself and repeated activations can kill it")
    void canTargetItselfAndActivationsStack() {
        castBroodButcher();
        Permanent butcher = findPermanent(player1, "Brood Butcher");
        Permanent scion = findPermanent(player1, "Eldrazi Scion");
        addAbilityMana();

        harness.activateAbility(player1, 0, 0, null, butcher.getId());
        harness.handlePermanentChosen(player1, scion.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, butcher)).isEqualTo(1);

        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new BroodButcher());
        addAbilityMana();
        harness.activateAbility(player1, 0, 0, null, butcher.getId());
        harness.handlePermanentChosen(player1, sacrifice.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Brood Butcher")).hasSize(2);
    }

    private void castBroodButcher() {
        harness.setHand(player1, List.of(new BroodButcher()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private void addAbilityMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
