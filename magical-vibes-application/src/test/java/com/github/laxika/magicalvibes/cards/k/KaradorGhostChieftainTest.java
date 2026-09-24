package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KaradorGhostChieftain.class, GrizzlyBears.class, Shock.class, LightningBolt.class, WalkingCorpse.class})
class KaradorGhostChieftainTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less for each creature card in its controller's graveyard")
    void reducesCostForCreatureCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new LightningBolt()));
        harness.setHand(player1, List.of(new KaradorGhostChieftain()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        prepareMainPhase();
        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Allows one creature spell from the controller's graveyard each turn")
    void allowsOneCreatureFromGraveyardEachTurn() {
        harness.addToBattlefield(player1, new KaradorGhostChieftain());
        harness.setGraveyard(player1, List.of(new WalkingCorpse(), new WalkingCorpse()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        prepareMainPhase();
        harness.castFromGraveyard(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Walking Corpse");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Cannot cast a noncreature spell from the graveyard")
    void cannotCastNoncreatureFromGraveyard() {
        harness.addToBattlefield(player1, new KaradorGhostChieftain());
        harness.setGraveyard(player1, List.of(new LightningBolt()));
        harness.setHand(player1, List.of());

        prepareMainPhase();

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
