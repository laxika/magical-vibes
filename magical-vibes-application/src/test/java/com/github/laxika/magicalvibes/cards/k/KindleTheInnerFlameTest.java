package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KindleTheInnerFlameTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a hasty token copy of a creature you control and sacrifices it at end step")
    void createsHastyTokenCopyWithEndStepSacrifice() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castFromHand(harness.getPermanentId(player1, "Grizzly Bears"));

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(token.getCard().getKeywords()).contains(Keyword.HASTE);
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .contains(new DelayedPermanentAction(token.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));
    }

    @Test
    @DisplayName("Cannot target a creature controlled by an opponent")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new KindleTheInnerFlame()));
        addFlashbackMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback can behold three distinct Elementals from the battlefield and hand")
    void flashbackBeholdsThreeDistinctElementals() {
        Permanent firstElemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent secondElemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.addToBattlefield(player1, new GrizzlyBears());
        AirElemental handElemental = new AirElemental();
        KindleTheInnerFlame card = new KindleTheInnerFlame();
        harness.setHand(player1, List.of(handElemental));
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castFlashbackWithBehold(player1, 0, targetId,
                List.of(firstElemental.getId(), secondElemental.getId()), List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(firstElemental, secondElemental);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(handElemental);
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken());
    }

    private void castFromHand(UUID targetId) {
        harness.setHand(player1, List.of(new KindleTheInnerFlame()));
        addFlashbackMana();
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private void addFlashbackMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
