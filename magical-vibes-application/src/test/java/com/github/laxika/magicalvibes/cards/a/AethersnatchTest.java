package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaAxe;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Aethersnatch.class, GrizzlyBears.class, LavaAxe.class})
class AethersnatchTest extends BaseCardTest {

    @Test
    void gainsControlOfCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Aethersnatch()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void mayRetargetControlledSpell() {
        LavaAxe lavaAxe = new LavaAxe();
        harness.setHand(player1, List.of(lavaAxe));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.setHand(player2, List.of(new Aethersnatch()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());

        harness.castSorcery(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, lavaAxe.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore - 5);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore);
    }
}
