package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.d.DevilsPlay;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IvyElemental;
import com.github.laxika.magicalvibes.cards.k.KnollspineInvocation;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnboundFlourishing.class, DevilsPlay.class, GrizzlyBears.class, IvyElemental.class,
        KnollspineInvocation.class})
class UnboundFlourishingTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles X in a permanent spell's cast-time value")
    void doublesPermanentSpellX() {
        harness.addToBattlefield(player1, new UnboundFlourishing());
        harness.setHand(player1, List.of(new IvyElemental()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        gs.playCard(gd, player1, 0, 2, null, null);

        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(4);
        harness.passBothPriorities();

        Permanent ivy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Ivy Elemental"))
                .findFirst()
                .orElseThrow();
        assertThat(ivy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Copies an X instant or sorcery spell")
    void copiesXSpell() {
        harness.addToBattlefield(player1, new UnboundFlourishing());
        harness.setHand(player1, List.of(new DevilsPlay()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, false);
        }
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Copies an activated ability")
    void copiesActivatedAbility() {
        harness.addToBattlefield(player1, new UnboundFlourishing());
        harness.addToBattlefield(player1, new KnollspineInvocation());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, 2, player2.getId());
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, false);
        }
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }
}
