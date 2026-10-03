package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BuriedRuin.class, AngelsFeather.class, Shock.class})
class BuriedRuinTest extends BaseCardTest {

    @Test
    @DisplayName("Taps for colorless mana")
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new BuriedRuin());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Returns targeted artifact card from graveyard to hand and sacrifices itself")
    void returnsArtifactFromGraveyardToHand() {
        harness.addToBattlefield(player1, new BuriedRuin());
        Card feather = new AngelsFeather();
        harness.setGraveyard(player1, List.of(feather));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, feather.getId(), Zone.GRAVEYARD);
        harness.assertInGraveyard(player1, "Buried Ruin");
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(feather.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(feather.getId()));
    }

    @Test
    @DisplayName("Cannot target a nonartifact card in the graveyard")
    void cannotTargetNonArtifact() {
        harness.addToBattlefield(player1, new BuriedRuin());
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 1, null, shock.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot return an artifact from an opponent's graveyard")
    void cannotTargetOpponentsArtifact() {
        harness.addToBattlefield(player1, new BuriedRuin());
        Card feather = new AngelsFeather();
        harness.setGraveyard(player2, List.of(feather));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 1, null, feather.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Buried Ruin");
        harness.assertInGraveyard(player2, "Angel's Feather");
    }

    @Test
    @DisplayName("Cannot activate the return ability without two mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new BuriedRuin());
        Card feather = new AngelsFeather();
        harness.setGraveyard(player1, List.of(feather));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 1, null, feather.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Buried Ruin");
        assertThat(findPermanent(player1, "Buried Ruin").isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Angel's Feather");
    }

    @Test
    @DisplayName("A tapped Buried Ruin cannot activate its return ability")
    void cannotActivateReturnAbilityAfterTappingForMana() {
        harness.addToBattlefield(player1, new BuriedRuin());
        Card feather = new AngelsFeather();
        harness.setGraveyard(player1, List.of(feather));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(findPermanent(player1, "Buried Ruin").isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, 1, null, feather.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Buried Ruin");
        harness.assertInGraveyard(player1, "Angel's Feather");
    }

    @Test
    @DisplayName("Can return an artifact during the opponent's turn")
    void canActivateDuringOpponentsTurn() {
        harness.addToBattlefield(player1, new BuriedRuin());
        Card feather = new AngelsFeather();
        harness.setGraveyard(player1, List.of(feather));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateAbility(player1, 0, 1, null, feather.getId(), Zone.GRAVEYARD);

        harness.assertNotOnBattlefield(player1, "Buried Ruin");
        harness.assertInGraveyard(player1, "Buried Ruin");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).noneMatch(c -> c.getId().equals(feather.getId()));
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        harness.assertInHand(player1, "Angel's Feather");
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(feather.getId()));
    }

    @Test
    @DisplayName("Does not return another artifact when the target leaves the graveyard")
    void targetLeavingGraveyardDoesNotRefundCostsOrReturnAnotherCard() {
        harness.addToBattlefield(player1, new BuriedRuin());
        Card target = new AngelsFeather();
        Card otherArtifact = new AngelsFeather();
        harness.setGraveyard(player1, List.of(target, otherArtifact));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, target.getId(), Zone.GRAVEYARD);
        gd.playerGraveyards.get(player1.getId()).remove(target);
        harness.setExile(player1, List.of(target));

        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Buried Ruin");
        harness.assertInGraveyard(player1, "Buried Ruin");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(target.getId()) || c.getId().equals(otherArtifact.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(otherArtifact).doesNotContain(target);
        assertThat(gd.exiledCards).anyMatch(entry -> entry.card().getId().equals(target.getId()));
    }
}
