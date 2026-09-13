package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.k.KorHaven;
import com.github.laxika.magicalvibes.cards.s.SneakyHomunculus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RathsEdge.class, KorHaven.class, SneakyHomunculus.class})
class RathsEdgeTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability adds one colorless mana")
    void tapAddsColorlessMana() {
        harness.addToBattlefield(player1, new RathsEdge());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Rath's Edge");
    }

    @Test
    @DisplayName("Sacrificing another land deals 1 damage to target player")
    void sacrificesAnotherLandAndDamagesPlayer() {
        harness.addToBattlefield(player1, new RathsEdge());
        var korHaven = harness.addToBattlefieldAndReturn(player1, new KorHaven());
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, korHaven.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertOnBattlefield(player1, "Rath's Edge");
        harness.assertInGraveyard(player1, "Kor Haven");
    }

    @Test
    @DisplayName("Sacrificing Rath's Edge itself deals 1 damage to target creature")
    void sacrificesItselfAndDamagesCreature() {
        harness.addToBattlefield(player1, new RathsEdge());
        harness.addToBattlefield(player2, new SneakyHomunculus());
        UUID targetId = harness.getPermanentId(player2, "Sneaky Homunculus");

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Rath's Edge");
        harness.assertInGraveyard(player2, "Sneaky Homunculus");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new RathsEdge());
        harness.addToBattlefield(player2, new KorHaven());
        UUID targetId = harness.getPermanentId(player2, "Kor Haven");
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature, planeswalker, battle, or player");
    }

    @Test
    @DisplayName("Sacrifice cost offers only lands, including Rath's Edge itself")
    void sacrificeCostOffersOnlyLands() {
        var rathsEdge = harness.addToBattlefieldAndReturn(player1, new RathsEdge());
        var korHaven = harness.addToBattlefieldAndReturn(player1, new KorHaven());
        var creature = harness.addToBattlefieldAndReturn(player1, new SneakyHomunculus());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, player2.getId());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds())
                .containsExactlyInAnyOrder(rathsEdge.getId(), korHaven.getId())
                .doesNotContain(creature.getId());

        harness.handlePermanentChosen(player1, korHaven.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        harness.assertOnBattlefield(player1, "Rath's Edge");
        harness.assertInGraveyard(player1, "Kor Haven");
    }
}
