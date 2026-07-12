package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpittingSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Activating auto-sacrifices the only land and puts the ability on the stack")
    void autoSacrificesOnlyLand() {
        harness.addToBattlefield(player1, new SpittingSpider());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Forest"));
        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Deals 1 damage to each creature with flying, sparing non-flyers and the Spider itself")
    void damagesOnlyFlyers() {
        harness.addToBattlefield(player1, new SpittingSpider());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new SuntailHawk());   // 1/1 flying -> dies
        harness.addToBattlefield(player2, new GrizzlyBears());  // 2/2 non-flying -> survives

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Suntail Hawk"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        // Spitting Spider has reach, not flying, so it is unaffected
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Spitting Spider"));
    }

    @Test
    @DisplayName("Does not damage players")
    void doesNotDamagePlayers() {
        harness.addToBattlefield(player1, new SpittingSpider());
        harness.addToBattlefield(player1, new Forest());
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("With multiple lands, asks which land to sacrifice")
    void asksWhichLandToSacrifice() {
        harness.addToBattlefield(player1, new SpittingSpider());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without a land to sacrifice")
    void cannotActivateWithoutLand() {
        harness.addToBattlefield(player1, new SpittingSpider());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
