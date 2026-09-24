package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DaruLancer;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SkirkProspector;
import com.github.laxika.magicalvibes.cards.s.SnarlingUndorak;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

@CardUsed({HarshMercy.class, DaruLancer.class, Plains.class, SkirkProspector.class, SnarlingUndorak.class})
class HarshMercyTest extends BaseCardTest {
    @Test
    void chosenTypesAreUnionedAcrossPlayers() {
        harness.addToBattlefield(player1, new DaruLancer());
        harness.addToBattlefield(player1, new SkirkProspector());
        harness.addToBattlefield(player2, new SnarlingUndorak());
        harness.addToBattlefield(player2, new SkirkProspector());
        cast();

        org.assertj.core.api.Assertions.assertThat(
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleListChoice(player1, "HUMAN");
        org.assertj.core.api.Assertions.assertThat(
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, "BEAST");

        harness.assertOnBattlefield(player1, "Daru Lancer");
        harness.assertOnBattlefield(player2, "Snarling Undorak");
        harness.assertNotOnBattlefield(player1, "Skirk Prospector");
        harness.assertNotOnBattlefield(player2, "Skirk Prospector");
    }

    @Test
    void creaturesCannotBeRegenerated() {
        harness.addToBattlefield(player1, new DaruLancer());
        var goblin = harness.addToBattlefieldAndReturn(player1, new SkirkProspector());
        goblin.setRegenerationShield(1);
        cast();

        harness.handleListChoice(player1, "HUMAN");
        harness.handleListChoice(player2, "HUMAN");

        harness.assertNotOnBattlefield(player1, "Skirk Prospector");
        harness.assertInGraveyard(player1, "Skirk Prospector");
    }

    @Test
    void onlyCreaturesAreDestroyed() {
        harness.addToBattlefield(player1, new DaruLancer());
        harness.addToBattlefield(player1, new Plains());
        cast();

        harness.handleListChoice(player1, "GOBLIN");
        harness.handleListChoice(player2, "GOBLIN");

        harness.assertNotOnBattlefield(player1, "Daru Lancer");
        harness.assertOnBattlefield(player1, "Plains");
    }

    private void cast() {
        harness.setHand(player1, List.of(new HarshMercy()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
