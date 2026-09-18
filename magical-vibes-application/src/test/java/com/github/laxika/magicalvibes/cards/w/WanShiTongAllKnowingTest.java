package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InnocenceKami;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WanShiTongAllKnowing.class, GrizzlyBears.class, InnocenceKami.class, Island.class})
class WanShiTongAllKnowingTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts the target second from the top and creates two Spirit tokens")
    void etbTucksTargetAndCreatesSpirits() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new Island();
        harness.setLibrary(player2, List.of(topCard, new Island()));

        castWanShiTong(target);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.TargetLibraryDestinationChoice.class);
        harness.handleListChoice(player2, "Second from the top");
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Island", "Grizzly Bears", "Island");
        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
    }

    @Test
    @DisplayName("The ETB cannot target a land")
    void etbCannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.setHand(player1, List.of(new WanShiTongAllKnowing()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Spirit tokens can block and be blocked only by Spirit creatures")
    void spiritTokenCombatRestriction() {
        castWanShiTongAndResolve();
        Permanent token = findPermanents(player1, "Spirit").getFirst();
        Permanent nonSpirit = addCreatureReady(player2, new GrizzlyBears());
        Permanent spirit = addCreatureReady(player2, new InnocenceKami());

        assertThat(bls.canBlockAttacker(gd, nonSpirit, token,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, spirit, token,
                gd.playerBattlefields.get(player2.getId()))).isTrue();
        assertThat(bls.canBlockAttacker(gd, token, nonSpirit,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
        assertThat(bls.canBlockAttacker(gd, token, spirit,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
    }

    private void castWanShiTong(Permanent target) {
        harness.setHand(player1, List.of(new WanShiTongAllKnowing()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0, target.getId());
    }

    private void castWanShiTongAndResolve() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player2, List.of(new Island()));
        castWanShiTong(target);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Second from the top");
        harness.passBothPriorities();
    }
}
