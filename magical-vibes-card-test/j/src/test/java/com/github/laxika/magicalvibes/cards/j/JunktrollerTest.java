package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.c.Char;
import com.github.laxika.magicalvibes.cards.g.GrayscaledGharial;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Junktroller.class, GrayscaledGharial.class, BorosRecruit.class, Char.class})
class JunktrollerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a target card from an opponent's graveyard on the bottom of its owner's library")
    void putsTargetCardFromOpponentsGraveyardOnOwnersLibraryBottom() {
        Permanent junktroller = addCreatureReady(player1, new Junktroller());
        Card target = new GrayscaledGharial();
        Card oldTop = new BorosRecruit();
        harness.setGraveyard(player2, List.of(target));
        harness.setLibrary(player2, List.of(oldTop));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(oldTop, target);
        assertThat(junktroller.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Puts a target card from its controller's graveyard on the bottom of its owner's library")
    void putsTargetCardFromItsControllersGraveyardOnOwnersLibraryBottom() {
        Permanent junktroller = addCreatureReady(player1, new Junktroller());
        Card target = new Char();
        Card oldTop = new GrayscaledGharial();
        harness.setGraveyard(player1, List.of(target));
        harness.setLibrary(player1, List.of(oldTop));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(oldTop, target);
        assertThat(junktroller.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does nothing if the target card leaves the graveyard before resolution")
    void doesNothingIfTargetLeavesGraveyard() {
        addCreatureReady(player1, new Junktroller());
        Card target = new GrayscaledGharial();
        harness.setGraveyard(player2, List.of(target));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Cannot activate while tapped")
    void cannotActivateWhileTapped() {
        Permanent junktroller = addCreatureReady(player1, new Junktroller());
        junktroller.tap();
        Card target = new Char();
        harness.setGraveyard(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent junktroller = addCreatureReady(player1, new Junktroller());
        junktroller.setSummoningSick(true);
        Card target = new Char();
        harness.setGraveyard(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(junktroller.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Resolves after Junktroller leaves the battlefield and moves only the chosen card")
    void resolvesWithoutSourceIntoEmptyLibrary() {
        addCreatureReady(player1, new Junktroller());
        Card target = new Char();
        Card other = new GrayscaledGharial();
        harness.setGraveyard(player2, List.of(other, target));
        harness.setLibrary(player2, List.of());

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(other);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(target);
        assertThat(gd.stack).isEmpty();
    }
}
