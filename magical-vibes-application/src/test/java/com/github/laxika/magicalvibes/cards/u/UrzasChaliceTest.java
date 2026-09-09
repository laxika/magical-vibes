package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UrzasChalice.class, DarksteelRelic.class, GrizzlyBears.class})
class UrzasChaliceTest extends BaseCardTest {

    @Test
    void artifactSpellOffersLifeGainForOneMana() {
        harness.addToBattlefield(player1, new UrzasChalice());
        harness.setHand(player1, List.of(new DarksteelRelic()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castArtifact(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void decliningDoesNotGainLife() {
        harness.addToBattlefield(player1, new UrzasChalice());
        harness.setHand(player1, List.of(new DarksteelRelic()));

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        harness.castArtifact(player1, 0);
        harness.handleMayAbilityChosen(player1, false);
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void nonArtifactSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new UrzasChalice());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }
}
