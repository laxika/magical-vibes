package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CloudsculptArmorer;
import com.github.laxika.magicalvibes.cards.d.DazzlingLights;
import com.github.laxika.magicalvibes.cards.e.EtalisFavor;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.ThrabenInspector;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValMaroonedSurveyor.class, ThrabenInspector.class, EtalisFavor.class,
        GrizzlyBears.class, CloudsculptArmorer.class, Shock.class, Opt.class, DazzlingLights.class})
class ValMaroonedSurveyorTest extends BaseCardTest {

    @Test
    void triggersOnEveryInvestigation() {
        harness.addToBattlefield(player1, new ValMaroonedSurveyor());
        harness.setHand(player1, List.of(new ThrabenInspector(), new ThrabenInspector()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        int player1Life = gd.getLife(player1.getId());
        int player2Life = gd.getLife(player2.getId());

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1Life + 4);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2Life - 4);
    }

    @Test
    void triggersOnDiscover() {
        harness.addToBattlefield(player1, new ValMaroonedSurveyor());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new EtalisFavor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int player1Life = gd.getLife(player1.getId());
        int player2Life = gd.getLife(player2.getId());

        harness.castEnchantment(player1, 0, gd.playerBattlefields.get(player1.getId()).get(1).getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, -1);
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1Life + 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2Life - 2);
    }

    @Test
    void triggersOnScryAndSurveil() {
        harness.addToBattlefield(player1, new ValMaroonedSurveyor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int player1Life = gd.getLife(player1.getId());
        int player2Life = gd.getLife(player2.getId());

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        resolveAllTriggers();

        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        resolveAllTriggers();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1Life + 4);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2Life - 4);
    }

    @Test
    void triggersOnSeek() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new ValMaroonedSurveyor());
        Card sought = new GrizzlyBears();
        harness.setLibrary(player1, List.of(sought));
        harness.setHand(player1, List.of(new CloudsculptArmorer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int player1Life = gd.getLife(player1.getId());
        int player2Life = gd.getLife(player2.getId());

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(sought);
        assertThat(gd.getLife(player1.getId())).isEqualTo(player1Life + 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2Life - 2);
    }
}
