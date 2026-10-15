package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
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

@CardUsed({SupplyDrop.class, ArgothianSprite.class})
class SupplyDropTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives a creature I control +2/+2 until end of turn")
    void etbBoostsCreatureIControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        harness.setHand(player1, List.of(new SupplyDrop()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.getToughnessModifier()).isEqualTo(2);
        assertThat(bears.getEffectivePower()).isEqualTo(4);
        assertThat(bears.getEffectiveToughness()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isZero();
        assertThat(bears.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("ETB rejects a creature controlled by an opponent")
    void etbRejectsOpponentsCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new ArgothianSprite());
        harness.setHand(player1, List.of(new SupplyDrop()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();
        assertThat(ownCreature.getPowerModifier()).isEqualTo(2);
        assertThat(opposingCreature.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("The activated ability sacrifices Supply Drop and draws a card")
    void activatedAbilitySacrificesAndDraws() {
        Permanent supplyDrop = harness.addToBattlefieldAndReturn(player1, new SupplyDrop());
        harness.setLibrary(player1, List.of(new ArgothianSprite()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(supplyDrop);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(supplyDrop.getCard());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof ArgothianSprite);
    }

    @Test
    @DisplayName("Supply Drop can be cast without any creatures to target")
    void canBeCastWithoutCreatures() {
        harness.setHand(player1, List.of(new SupplyDrop()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Supply Drop");
    }

    @Test
    @DisplayName("Flash allows casting during an opponent's combat")
    void canBeCastDuringOpponentsCombat() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.setHand(player1, List.of(new SupplyDrop()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.ensurePriority(player1);

        harness.castArtifact(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Supply Drop");
        assertThat(bears.getPowerModifier()).isEqualTo(2);
        assertThat(bears.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing Supply Drop in response does not stop its ETB boost")
    void etbStillResolvesAfterSourceIsSacrificed() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new ArgothianSprite());
        harness.setHand(player1, List.of(new SupplyDrop()));
        harness.setLibrary(player1, List.of(new ArgothianSprite()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.activateAbility(player1, 1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Supply Drop");
        assertThat(creature.getPowerModifier()).isZero();
        harness.passBothPriorities();
        harness.assertInHand(player1, "Argothian Sprite");
        assertThat(creature.getPowerModifier()).isZero();
        harness.passBothPriorities();
        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("A tapped Supply Drop cannot pay its activation cost")
    void tappedArtifactCannotActivate() {
        Permanent supplyDrop = harness.addToBattlefieldAndReturn(player1, new SupplyDrop());
        supplyDrop.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(supplyDrop);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(supplyDrop.getCard());
    }

    @Test
    @DisplayName("The activated ability requires four mana")
    void insufficientManaCannotActivate() {
        Permanent supplyDrop = harness.addToBattlefieldAndReturn(player1, new SupplyDrop());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(supplyDrop);
        assertThat(supplyDrop.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(supplyDrop.getCard());
    }

}
