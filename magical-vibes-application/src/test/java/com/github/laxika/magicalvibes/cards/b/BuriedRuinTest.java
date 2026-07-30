package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

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
}
