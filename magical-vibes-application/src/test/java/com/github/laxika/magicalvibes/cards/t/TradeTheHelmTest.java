package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingMine;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TradeTheHelmTest extends BaseCardTest {

    private void prepareSpell() {
        harness.setHand(player1, List.of(new TradeTheHelm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @Test
    @DisplayName("Exchanges control of an artifact you control and a creature an opponent controls")
    void exchangesControlOfArtifactAndCreature() {
        prepareSpell();
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castAndResolveSorcery(player1, 0, List.of(ownArtifact.getId(), opponentCreature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(opponentCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(ownArtifact);
    }

    @Test
    @DisplayName("Rejects a second target that is not an artifact or creature an opponent controls")
    void rejectsIllegalOpponentTarget() {
        prepareSpell();
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new HowlingMine());
        Permanent opponentEnchantment = harness.addToBattlefieldAndReturn(player2, new Pacifism());

        assertThatThrownBy(() -> harness.castSorcery(
                player1, 0, List.of(ownArtifact.getId(), opponentEnchantment.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Second target must be an artifact or creature an opponent controls");
    }

    @Test
    @DisplayName("Cycling discards Trade the Helm and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new TradeTheHelm()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Trade the Helm");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
