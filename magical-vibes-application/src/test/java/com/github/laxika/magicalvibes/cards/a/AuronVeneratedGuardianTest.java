package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AuronVeneratedGuardian.class, GrizzlyBears.class, SerraAngel.class, Murder.class})
class AuronVeneratedGuardianTest extends BaseCardTest {

    @Test
    void attackPutsCounterBeforeChoosingExileTarget() {
        Permanent auron = addCreatureReady(player1, new AuronVeneratedGuardian());
        Permanent weakCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent equalOrGreaterCreature = addCreatureReady(player2, new SerraAngel());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(auron.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(weakCreature.getId());
        assertThat(choice.validIds()).doesNotContain(equalOrGreaterCreature.getId());

        harness.handlePermanentChosen(player1, weakCreature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    void exiledCreatureReturnsWhenAuronLeaves() {
        Permanent auron = addCreatureReady(player1, new AuronVeneratedGuardian());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Murder()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, auron.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Auron, Venerated Guardian")).isEmpty();
        assertThat(findPermanents(player2, "Grizzly Bears")).hasSize(1);
    }
}
