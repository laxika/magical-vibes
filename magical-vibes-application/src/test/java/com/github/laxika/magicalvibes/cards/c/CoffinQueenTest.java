package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.Twiddle;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CoffinQueenTest extends BaseCardTest {

    @Test
    @DisplayName("Reanimates a creature card from an opponent's graveyard under your control")
    void reanimatesFromOpponentGraveyard() {
        Permanent queen = addReadyQueen();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        activateReanimate(queen, bears);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(c -> c.getId().equals(bears.getId()));
        assertThat(queen.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Untapping Coffin Queen exiles the reanimated creature")
    void untappingExilesReanimatedCreature() {
        Permanent queen = addReadyQueen();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        activateReanimate(queen, bears);

        harness.setHand(player1, List.of(new Twiddle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, queen.getId());
        harness.passBothPriorities(); // Twiddle resolves, the Queen untaps, the trigger goes on the stack
        harness.passBothPriorities(); // trigger resolves

        assertThat(queen.isTapped()).isFalse();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(c -> c.getId().equals(bears.getId()));
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(bears.getId())
                && e.ownerId().equals(player2.getId()));
    }

    @Test
    @DisplayName("Coffin Queen leaving the battlefield exiles the reanimated creature")
    void leavingExilesReanimatedCreature() {
        Permanent queen = addReadyQueen();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        activateReanimate(queen, bears);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, queen.getId());
        harness.passBothPriorities(); // bolt resolves, the Queen dies, the trigger goes on the stack
        harness.passBothPriorities(); // trigger resolves

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.exiledCards).anyMatch(e -> e.card().getId().equals(bears.getId())
                && e.ownerId().equals(player2.getId()));
    }

    @Test
    @DisplayName("A reanimated creature that dies first goes to its owner's graveyard")
    void reanimatedCreatureDiesToItsOwnersGraveyard() {
        Permanent queen = addReadyQueen();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));

        activateReanimate(queen, bears);

        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        Permanent reanimated = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getId().equals(bears.getId()))
                .findFirst().orElseThrow();
        harness.castInstant(player1, 0, reanimated.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a noncreature card in a graveyard")
    void cannotTargetNoncreatureCard() {
        Permanent queen = addReadyQueen();
        Card shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(queen);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, 0, null, shock.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature card");
    }

    private Permanent addReadyQueen() {
        Permanent perm = new Permanent(new CoffinQueen());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(perm);
        return perm;
    }

    private void activateReanimate(Permanent queen, Card graveyardCard) {
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(queen);
        harness.activateAbility(player1, idx, 0, null, graveyardCard.getId(), Zone.GRAVEYARD);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
