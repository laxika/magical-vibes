package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KorFirewalkerTest extends BaseCardTest {

    @Test
    @DisplayName("Protection from red prevents red spells from targeting Kor Firewalker")
    void protectionFromRedPreventsRedSpellTargeting() {
        var firewalker = harness.addToBattlefieldAndReturn(player2, new KorFirewalker());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, firewalker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A red spell cast by any player may gain Kor Firewalker's controller 1 life")
    void redSpellCastByAnyPlayerMayGainLife() {
        harness.addToBattlefield(player1, new KorFirewalker());
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        int startingLife = gd.playerLifeTotals.get(player1.getId());
        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 1);
    }

    @Test
    @DisplayName("A nonred spell does not trigger Kor Firewalker")
    void nonredSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new KorFirewalker());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
