package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DevourInShadow;
import com.github.laxika.magicalvibes.cards.m.MagmaJet;
import com.github.laxika.magicalvibes.cards.m.MyrServitor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AuriokChampion.class, MyrServitor.class, DevourInShadow.class, MagmaJet.class})
class AuriokChampionTest extends BaseCardTest {

    @Test
    @DisplayName("May gain 1 life when another creature enters")
    void mayGainLifeWhenAnotherCreatureEnters() {
        harness.addToBattlefield(player1, new AuriokChampion());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new MyrServitor(), "{1}");

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Does not gain life when declining")
    void doesNotGainLifeWhenDeclining() {
        harness.addToBattlefield(player1, new AuriokChampion());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new MyrServitor(), "{1}");

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("May gain life when an opponent's creature enters")
    void mayGainLifeWhenOpponentsCreatureEnters() {
        harness.addToBattlefield(player1, new AuriokChampion());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new MyrServitor(), "{1}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Does not trigger when Auriok Champion itself enters")
    void doesNotTriggerForItself() {
        harness.castFromHand(player1, new AuriokChampion(), "{W}{W}");

        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot be targeted by black or red spells")
    void cannotBeTargetedByBlackOrRedSpells() {
        Permanent champion = addCreatureReady(player2, new AuriokChampion());
        addCreatureReady(player2, new MyrServitor());

        harness.setHand(player1, List.of(new DevourInShadow()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, champion.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from black");

        harness.setHand(player1, List.of(new MagmaJet()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, champion.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }
}
