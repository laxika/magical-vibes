package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.k.KozilekButcherOfTruth;
import com.github.laxika.magicalvibes.cards.y.YargleAndMultani;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TitansPresence.class, GrizzlyBears.class, HillGiant.class, KozilekButcherOfTruth.class,
        YargleAndMultani.class})
class TitansPresenceTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a target creature whose power is less than the revealed creature's power")
    void exilesCreatureWithinRevealedPower() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        KozilekButcherOfTruth revealed = new KozilekButcherOfTruth();
        harness.setHand(player1, List.of(new TitansPresence(), revealed));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(revealed);
    }

    @Test
    @DisplayName("Exiles a target creature whose power equals the revealed creature's power")
    void exilesCreatureAtRevealedPower() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new KozilekButcherOfTruth());
        KozilekButcherOfTruth revealed = new KozilekButcherOfTruth();
        harness.setHand(player1, List.of(new TitansPresence(), revealed));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    @DisplayName("Leaves a target creature with greater power on the battlefield")
    void leavesCreatureWithGreaterPower() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new YargleAndMultani());
        KozilekButcherOfTruth revealed = new KozilekButcherOfTruth();
        harness.setHand(player1, List.of(new TitansPresence(), revealed));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstantWithDiscard(player1, 0, target.getId(), 1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(target.getCard());
    }

    @Test
    @DisplayName("Requires revealing a colorless creature card")
    void requiresColorlessCreatureCardToReveal() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new TitansPresence(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, target.getId(), 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Revealed card must be colorless creature card");
    }
}
