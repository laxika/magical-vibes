package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({APremonitionOfYourDemise.class, Forest.class, GrizzlyBears.class, Shock.class})
class APremonitionOfYourDemiseTest extends BaseCardTest {

    @Test
    void putsBothRevealedCardsIntoHandAndDealsTheirCombinedNonlandManaValue() {
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(shock, bears));
        int startingLife = gd.getLife(player2.getId());

        resolveScheme(player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(shock, bears);
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 3);
    }

    @Test
    void landsGoToHandButDoNotContributeToDamage() {
        Forest forest = new Forest();
        Shock shock = new Shock();
        harness.setLibrary(player1, List.of(forest, shock));
        int startingLife = gd.getLife(player2.getId());

        resolveScheme(player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest, shock);
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 1);
    }

    private void resolveScheme(java.util.UUID targetId) {
        APremonitionOfYourDemise scheme = new APremonitionOfYourDemise();
        gd.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(EffectSlot.SPELL),
                targetId,
                (Zone) null));
        harness.passBothPriorities();
    }
}
