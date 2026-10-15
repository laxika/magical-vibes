package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EvilPresence;
import com.github.laxika.magicalvibes.cards.s.StrongholdAssassin;
import com.github.laxika.magicalvibes.cards.v.VolrathsStronghold;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({TidalWarrior.class, VolrathsStronghold.class, StrongholdAssassin.class, EvilPresence.class})
class TidalWarriorTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability targets a land")
    void activatingAbilityTargetsLand() {
        addCreatureReady(player1, new TidalWarrior());
        Permanent stronghold = harness.addToBattlefieldAndReturn(player1, new VolrathsStronghold());

        harness.activateAbility(player1, 0, null, stronghold.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(stronghold.getId());
    }

    @Test
    @DisplayName("Activating the ability can target an opponent's land")
    void activatingAbilityCanTargetOpponentsLand() {
        addCreatureReady(player1, new TidalWarrior());
        Permanent stronghold = harness.addToBattlefieldAndReturn(player2, new VolrathsStronghold());

        harness.activateAbility(player1, 0, null, stronghold.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(stronghold.getId());
    }

    @Test
    @DisplayName("Resolving the ability makes the target land an Island")
    void landBecomesIsland() {
        addCreatureReady(player1, new TidalWarrior());
        Permanent stronghold = harness.addToBattlefieldAndReturn(player1, new VolrathsStronghold());

        harness.activateAbility(player1, 0, null, stronghold.getId());
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, stronghold)).containsExactly(CardSubtype.ISLAND);
        assertThat(gqs.hasLostPrintedAbilities(gd, stronghold)).isTrue();
        assertThat(gqs.getOverriddenLandManaColor(gd, stronghold)).isEqualTo(ManaColor.BLUE);
    }

    @Test
    @DisplayName("The granted Island type wears off at end of turn")
    void islandTypeWearsOff() {
        addCreatureReady(player1, new TidalWarrior());
        Permanent stronghold = harness.addToBattlefieldAndReturn(player1, new VolrathsStronghold());

        harness.activateAbility(player1, 0, null, stronghold.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, stronghold)).isEmpty();
        assertThat(gqs.hasLostPrintedAbilities(gd, stronghold)).isFalse();
        assertThat(gqs.getOverriddenLandManaColor(gd, stronghold)).isNull();
    }

    @Test
    @DisplayName("The ability cannot target a creature")
    void cannotTargetCreature() {
        addCreatureReady(player1, new TidalWarrior());
        Permanent assassin = addCreatureReady(player1, new StrongholdAssassin());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, assassin.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Activating taps the Warrior before resolution")
    void activationPaysTapCost() {
        Permanent warrior = addCreatureReady(player1, new TidalWarrior());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new VolrathsStronghold());

        harness.activateAbility(player1, 0, null, land.getId());

        assertThat(warrior.isTapped()).isTrue();
        assertThat(gqs.effectiveBasicLandTypes(gd, land)).isEmpty();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A summoning sick Warrior cannot pay the tap cost")
    void summoningSicknessPreventsActivation() {
        harness.addToBattlefield(player1, new TidalWarrior());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new VolrathsStronghold());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The affected Stronghold produces blue instead of colorless mana")
    void affectedLandProducesBlueMana() {
        addCreatureReady(player1, new TidalWarrior());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new VolrathsStronghold());

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();
        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("The ability resolves even if the Warrior leaves the battlefield")
    void abilitySurvivesSourceLeavingBattlefield() {
        Permanent warrior = addCreatureReady(player1, new TidalWarrior());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new VolrathsStronghold());

        harness.activateAbility(player1, 0, null, land.getId());
        gd.playerBattlefields.get(player1.getId()).remove(warrior);
        gd.playerGraveyards.get(player1.getId()).add(warrior.getCard());
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, land)).containsExactly(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("A later land type setting Aura replaces the Island effect")
    void laterAuraOverridesIsland() {
        addCreatureReady(player1, new TidalWarrior());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new VolrathsStronghold());

        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new EvilPresence()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, land)).containsExactly(CardSubtype.SWAMP);
        harness.tapPermanent(player1, 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }
}
