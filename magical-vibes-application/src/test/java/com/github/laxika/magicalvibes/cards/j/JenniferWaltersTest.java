package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TheSensationalSheHulk;
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

@CardUsed({JenniferWalters.class, TheSensationalSheHulk.class, GrizzlyBears.class, Shock.class})
class JenniferWaltersTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast using either face")
    void castsUsingEitherFace() {
        prepareMainPhase();
        harness.setHand(player1, List.of(new JenniferWalters(), new JenniferWalters()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTransformed()).isFalse();

        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()).get(1).isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Transforms at sorcery speed")
    void transformsAtSorcerySpeed() {
        Permanent jennifer = addFrontReady();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(jennifer.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Prevents opponents from casting spells during its controller's turn")
    void restrictsOpponentsDuringControllerTurn() {
        addBackReady();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        prepareMainPhase();
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Reflects damage dealt to a creature you control once each turn")
    void reflectsDamageOnceEachTurn() {
        Permanent sheHulk = addBackReady();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        Permanent opposingCreature = gd.playerBattlefields.get(player2.getId()).getFirst();
        harness.castInstant(player2, 0, opposingCreature.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();

        harness.castInstant(player2, 0, sheHulk.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.castInstant(player2, 0, sheHulk.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addFrontReady() {
        return addCreatureReady(player1, new JenniferWalters());
    }

    private Permanent addBackReady() {
        JenniferWalters card = new JenniferWalters();
        Permanent permanent = addCreatureReady(player1, card);
        permanent.setCard(card.getBackFaceCard());
        permanent.setTransformed(true);
        return permanent;
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
