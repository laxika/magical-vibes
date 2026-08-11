package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MorningtidesLightTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles any number of target creatures and returns them tapped under their owners' control")
    void exilesAndReturnsTargetCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GoldMyr());

        castMorningtidesLight(List.of(ownCreature.getId(), opponentCreature.getId()));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Gold Myr");
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Morningtide's Light");

        gd.playerAutoStopSteps.put(player1.getId(), Set.of(TurnStep.END_STEP));
        gd.playerAutoStopSteps.put(player2.getId(), Set.of(TurnStep.END_STEP));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent returnedOwnCreature = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        Permanent returnedOpponentCreature = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Gold Myr"))
                .findFirst()
                .orElseThrow();
        assertThat(returnedOwnCreature.isTapped()).isTrue();
        assertThat(returnedOpponentCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Prevents damage to its controller until that player's next turn")
    void preventsDamageUntilNextTurn() {
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        castMorningtidesLight(List.of());
        castShock(player2, player1.getId());
        harness.assertLife(player1, 20);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        castShock(player2, player1.getId());
        harness.assertLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playersWithAllPlayerDamagePreventedUntilNextTurn).doesNotContain(player1.getId());
        castShock(player2, player1.getId());
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Only creatures can be targeted")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new MorningtidesLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castMorningtidesLight(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new MorningtidesLight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, targetIds);
        harness.passBothPriorities();
    }

    private void castShock(com.github.laxika.magicalvibes.model.Player caster, UUID targetId) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castInstant(caster, 0, targetId);
        harness.passBothPriorities();
    }
}
