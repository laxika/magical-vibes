package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FieldCreeper;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.m.Manalith;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.o.OreskosSwiftclaw;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AethershieldArtificer.class, FieldCreeper.class, OreskosSwiftclaw.class,
        Murder.class, Manalith.class, LightningStrike.class})
class AethershieldArtificerTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
    }

    @Test
    @DisplayName("Boosts the targeted artifact creature by +2/+2")
    void boostsTargetArtifactCreature() {
        harness.addToBattlefield(player1, new AethershieldArtificer());
        Permanent thopter = harness.addToBattlefieldAndReturn(player1, new FieldCreeper());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new FieldCreeper());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, thopter.getId());
        harness.passBothPriorities();

        assertThat(thopter.getPowerModifier()).isEqualTo(2);
        assertThat(thopter.getToughnessModifier()).isEqualTo(2);
        assertThat(other.getPowerModifier()).isEqualTo(0);
        assertThat(other.getToughnessModifier()).isEqualTo(0);
        assertThat(harness.getGameQueryService().hasKeyword(gd, other, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Granted indestructible survives a destroy effect")
    void grantsIndestructible() {
        harness.addToBattlefield(player1, new AethershieldArtificer());
        Permanent thopter = harness.addToBattlefieldAndReturn(player1, new FieldCreeper());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, thopter.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, thopter.getId());

        harness.assertOnBattlefield(player1, "Field Creeper");
    }

    @Test
    @DisplayName("Cannot target a non-artifact creature")
    void cannotTargetNonArtifactCreature() {
        harness.addToBattlefield(player1, new AethershieldArtificer());
        harness.addToBattlefield(player1, new FieldCreeper());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new OreskosSwiftclaw());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an artifact creature an opponent controls")
    void cannotTargetOpponentArtifactCreature() {
        harness.addToBattlefield(player1, new AethershieldArtificer());
        harness.addToBattlefield(player1, new FieldCreeper());
        Permanent enemy = harness.addToBattlefieldAndReturn(player2, new FieldCreeper());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, enemy.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Does not trigger during the opponent's combat")
    void doesNotTriggerOnOpponentTurn() {
        harness.addToBattlefield(player1, new AethershieldArtificer());
        harness.addToBattlefieldAndReturn(player1, new FieldCreeper());

        advanceToCombat(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        harness.addToBattlefield(player1, new AethershieldArtificer());
        Permanent thopter = harness.addToBattlefieldAndReturn(player1, new FieldCreeper());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, thopter.getId());
        harness.passBothPriorities();
        assertThat(thopter.getPowerModifier()).isEqualTo(2);
        assertThat(harness.getGameQueryService().hasKeyword(gd, thopter, Keyword.INDESTRUCTIBLE)).isTrue();

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(thopter.getPowerModifier()).isEqualTo(0);
        assertThat(thopter.getToughnessModifier()).isEqualTo(0);
        assertThat(harness.getGameQueryService().hasKeyword(gd, thopter, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void cannotTargetNonCreatureArtifact() {
        harness.addToBattlefield(player1, new AethershieldArtificer());
        harness.addToBattlefield(player1, new FieldCreeper());
        Permanent manalith = harness.addToBattlefieldAndReturn(player1, new Manalith());

        advanceToCombat(player1);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, manalith.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void grantedIndestructibleSurvivesLethalDamage() {
        harness.addToBattlefield(player1, new AethershieldArtificer());
        Permanent creeper = harness.addToBattlefieldAndReturn(player1, new FieldCreeper());

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, creeper.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, creeper.getId());

        harness.assertOnBattlefield(player1, "Field Creeper");
        assertThat(creeper.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void noLegalTargetsDoesNotLeavePendingChoice() {
        harness.addToBattlefield(player1, new AethershieldArtificer());

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void removingSourceDoesNotStopTriggeredAbility() {
        Permanent artificer = harness.addToBattlefieldAndReturn(player1, new AethershieldArtificer());
        Permanent creeper = harness.addToBattlefieldAndReturn(player1, new FieldCreeper());
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, creeper.getId());
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, artificer.getId());
        harness.assertInGraveyard(player1, "Aethershield Artificer");
        harness.passBothPriorities();

        assertThat(creeper.getPowerModifier()).isEqualTo(2);
        assertThat(creeper.getToughnessModifier()).isEqualTo(2);
        assertThat(harness.getGameQueryService().hasKeyword(gd, creeper, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void targetCanBeDestroyedBeforeAbilityResolves() {
        harness.addToBattlefield(player1, new AethershieldArtificer());
        Permanent creeper = harness.addToBattlefieldAndReturn(player1, new FieldCreeper());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new FieldCreeper());
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        advanceToCombat(player1);
        harness.handlePermanentChosen(player1, creeper.getId());
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, creeper.getId());
        harness.assertInGraveyard(player1, "Field Creeper");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(other.getPowerModifier()).isEqualTo(0);
        assertThat(other.getToughnessModifier()).isEqualTo(0);
        assertThat(harness.getGameQueryService().hasKeyword(gd, other, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
