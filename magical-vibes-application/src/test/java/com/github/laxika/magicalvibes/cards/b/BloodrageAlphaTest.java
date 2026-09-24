package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.w.WickedWolf;
import com.github.laxika.magicalvibes.cards.w.WyluliWolf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodrageAlpha.class, WickedWolf.class, WyluliWolf.class, GrizzlyBears.class, Ornithopter.class})
class BloodrageAlphaTest extends BaseCardTest {

    @Test
    @DisplayName("The fight mode makes another Wolf or Werewolf you control fight an opposing creature")
    void fightMode() {
        Permanent wolf = addCreatureReady(player1, new WickedWolf());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        castBloodrage(0, List.of(wolf.getId(), opponent.getId()));
        resolveCreatureAndEtb();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(wolf.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boon grants the next Wolf or Werewolf spell an optional enter-the-battlefield fight")
    void boonGrantsNextMatchingSpellFight() {
        Permanent opponent = addCreatureReady(player2, new Ornithopter());
        chooseBoonMode();

        harness.setHand(player1, List.of(new WyluliWolf(), new WyluliWolf()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponent.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(opponent.getMarkedDamage()).isEqualTo(1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(opponent.getMarkedDamage()).isEqualTo(1);
    }

    private void chooseBoonMode() {
        castBloodrage(1, List.of());
        resolveCreatureAndEtb();
    }

    private void castBloodrage(int mode, List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new BloodrageAlpha()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castModalInstant(player1, 0, mode, targetIds);
    }

    private void resolveCreatureAndEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
