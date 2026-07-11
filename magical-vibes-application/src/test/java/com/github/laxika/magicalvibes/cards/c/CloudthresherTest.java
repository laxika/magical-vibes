package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CloudthresherTest extends BaseCardTest {

    private void addMana(com.github.laxika.magicalvibes.model.Player player) {
        harness.addMana(player, ManaColor.GREEN, 4);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    // ===== Hardcast =====

    @Test
    @DisplayName("Hardcast ETB: 2 damage to each player, kills flyers, spares non-flyers, and stays on the battlefield")
    void hardcastEtbDamage() {
        harness.addToBattlefield(player2, new SuntailHawk());   // 1/1 flying -> dies
        harness.addToBattlefield(player2, new GrizzlyBears());   // 2/2 non-flying -> survives
        harness.setHand(player1, List.of(new Cloudthresher()));
        addMana(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger on stack
        harness.passBothPriorities(); // resolve ETB trigger

        // 2 damage to each player
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        // flyer destroyed, non-flyer survives
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Suntail Hawk"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));

        // Cloudthresher has Reach (not flying), so it is unaffected and remains
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Cloudthresher"));
    }

    // ===== Evoke =====

    @Test
    @DisplayName("Evoke: ETB still deals 2 damage to each player and Cloudthresher is sacrificed as it enters")
    void evokeSacrificesSelfButEtbFires() {
        harness.addToBattlefield(player2, new SuntailHawk());
        harness.setHand(player1, List.of(new Cloudthresher()));
        addMana(player1); // evoke costs {2}{G}{G}; leftover mana is irrelevant

        harness.castCreatureWithEvoke(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        // ETB damage happened
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Suntail Hawk"));

        // Cloudthresher sacrificed on entry
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Cloudthresher"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Cloudthresher"));
    }
}
