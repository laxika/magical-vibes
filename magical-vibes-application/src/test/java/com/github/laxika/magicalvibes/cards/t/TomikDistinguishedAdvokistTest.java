package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Craterize;
import com.github.laxika.magicalvibes.cards.c.CrucibleOfWorlds;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Groundskeeper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TomikDistinguishedAdvokist.class, Craterize.class, CrucibleOfWorlds.class,
        Forest.class, Groundskeeper.class})
class TomikDistinguishedAdvokistTest extends BaseCardTest {

    @Test
    @DisplayName("Opponents cannot target lands with spells")
    void opponentsCannotTargetLandsWithSpells() {
        harness.addToBattlefield(player1, new TomikDistinguishedAdvokist());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player2, List.of(new Craterize()));
        harness.addMana(player2, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Forest");

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be the target");
    }

    @Test
    @DisplayName("Tomik's controller can target lands with spells")
    void controllerCanTargetLandsWithSpells() {
        harness.addToBattlefield(player1, new TomikDistinguishedAdvokist());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new Craterize()));
        harness.addMana(player1, ManaColor.RED, 4);

        UUID targetId = harness.getPermanentId(player2, "Forest");
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Opponents cannot target land cards in graveyards with abilities")
    void opponentsCannotTargetLandCardsInGraveyardsWithAbilities() {
        harness.addToBattlefield(player1, new TomikDistinguishedAdvokist());
        harness.addToBattlefield(player2, new Groundskeeper());
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        Card forest = new Forest();
        harness.setGraveyard(player2, List.of(forest));

        assertThatThrownBy(() -> harness.activateAbility(
                player2, 0, 0, null, forest.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Land cards in graveyards");
    }

    @Test
    @DisplayName("Tomik's controller can target land cards in graveyards with abilities")
    void controllerCanTargetLandCardsInGraveyardsWithAbilities() {
        harness.addToBattlefield(player1, new Groundskeeper());
        harness.addToBattlefield(player1, new TomikDistinguishedAdvokist());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Card forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));

        harness.activateAbility(player1, 0, 0, null, forest.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Opponents cannot play lands from graveyards")
    void opponentsCannotPlayLandsFromGraveyards() {
        harness.addToBattlefield(player1, new TomikDistinguishedAdvokist());
        harness.addToBattlefield(player2, new CrucibleOfWorlds());
        harness.setGraveyard(player2, List.of(new Forest()));
        harness.setHand(player2, List.of());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.playGraveyardLand(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable from graveyard");
    }

    @Test
    @DisplayName("Tomik's controller can play lands from their graveyard")
    void controllerCanPlayLandsFromGraveyard() {
        harness.addToBattlefield(player1, new TomikDistinguishedAdvokist());
        harness.addToBattlefield(player1, new CrucibleOfWorlds());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playGraveyardLand(player1, 0);

        harness.assertOnBattlefield(player1, "Forest");
    }
}
