package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CabalPatriarch.class, Forest.class, GiantGrowth.class, GrizzlyBears.class, HillGiant.class})
class CabalPatriarchTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature gives a target creature -2/-2")
    void sacrificesCreatureForMinusTwoMinusTwo() {
        harness.addToBattlefield(player1, new CabalPatriarch());
        Permanent sacrificialCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        addMana();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, sacrificialCreature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrificialCreature);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exiling a creature card from the graveyard gives a target creature -2/-2")
    void exilesCreatureCardForMinusTwoMinusTwo() {
        harness.addToBattlefield(player1, new CabalPatriarch());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        GrizzlyBears creatureCard = new GrizzlyBears();
        GiantGrowth noncreatureCard = new GiantGrowth();
        harness.setGraveyard(player1, List.of(creatureCard, noncreatureCard));
        addMana();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(noncreatureCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creatureCard);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The -2/-2 wears off at end of turn")
    void minusTwoMinusTwoWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new CabalPatriarch());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        addMana();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    @DisplayName("The sacrifice ability cannot target a noncreature permanent")
    void sacrificeAbilityCannotTargetNoncreature() {
        harness.addToBattlefield(player1, new CabalPatriarch());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        harness.addToBattlefield(player1, new CabalPatriarch());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot exile a noncreature card to pay the second ability")
    void cannotExileNoncreatureCard() {
        harness.addToBattlefield(player1, new CabalPatriarch());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setGraveyard(player1, List.of(new GiantGrowth()));
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cabal Patriarch can sacrifice itself and its ability still resolves")
    void canSacrificeItself() {
        CabalPatriarch patriarchCard = new CabalPatriarch();
        Permanent patriarch = harness.addToBattlefieldAndReturn(player1, patriarchCard);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        addMana();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handlePermanentChosen(player1, patriarch.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(patriarch);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(patriarchCard);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("A sacrificed creature can immediately pay for the graveyard ability")
    void canExileTheCreatureJustSacrificed() {
        harness.addToBattlefield(player1, new CabalPatriarch());
        GrizzlyBears creatureCard = new GrizzlyBears();
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, creatureCard);
        HillGiant targetCard = new HillGiant();
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);
        addMana();
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handlePermanentChosen(player1, sacrifice.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creatureCard);
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(creatureCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creatureCard);
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(targetCard);
    }

    @Test
    @DisplayName("The graveyard ability can kill a creature you control with zero toughness")
    void canTargetAndKillOwnCreature() {
        harness.addToBattlefield(player1, new CabalPatriarch());
        GrizzlyBears targetCard = new GrizzlyBears();
        Permanent target = harness.addToBattlefieldAndReturn(player1, targetCard);
        harness.setGraveyard(player1, List.of(new HillGiant()));
        addMana();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(targetCard);
    }

    @Test
    @DisplayName("The target can be sacrificed as the cost and stays sacrificed when resolution fails")
    void canSacrificeTheTarget() {
        harness.addToBattlefield(player1, new CabalPatriarch());
        GrizzlyBears targetCard = new GrizzlyBears();
        Permanent target = harness.addToBattlefieldAndReturn(player1, targetCard);
        addMana();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(targetCard);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(targetCard);
    }

    @Test
    @DisplayName("A creature in an opponent's graveyard cannot pay the exile cost")
    void cannotExileFromOpponentsGraveyard() {
        harness.addToBattlefield(player1, new CabalPatriarch());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        GrizzlyBears creatureCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(creatureCard));
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creatureCard);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Both abilities require black mana even when three generic mana are available")
    void requiresBlackMana(int abilityIndex) {
        harness.addToBattlefield(player1, new CabalPatriarch());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Both abilities can be activated during an opponent's turn without tapping")
    void canActivateOnOpponentsTurn(int abilityIndex) {
        Permanent patriarch = harness.addToBattlefieldAndReturn(player1, new CabalPatriarch());
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, abilityIndex, null, target.getId());
        if (abilityIndex == 0) {
            harness.handlePermanentChosen(player1, sacrifice.getId());
        } else {
            harness.handleGraveyardCardChosen(player1, 0);
        }
        harness.passBothPriorities();

        assertThat(patriarch.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    private void addMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 3);
    }
}
