package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GoldMyr;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SuddenDisappearance.class, Forest.class, GloriousAnthem.class, GoldMyr.class, GrizzlyBears.class,
        SpitefulShadows.class})
class SuddenDisappearanceTest extends BaseCardTest {

    

    @Test
    @DisplayName("Casting puts it on the stack as SORCERY_SPELL targeting a player")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new SuddenDisappearance()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Exiles all nonland permanents target player controls")
    void exilesAllNonlandPermanents() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GoldMyr());
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.setHand(player1, List.of(new SuddenDisappearance()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Gold Myr");
        harness.assertNotOnBattlefield(player2, "Glorious Anthem");

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(c -> c.getName())
                .contains("Grizzly Bears", "Gold Myr", "Glorious Anthem");
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).hasSize(1);
    }

    @Test
    @DisplayName("Does not exile lands the target player controls")
    void doesNotExileLands() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new SuddenDisappearance()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(c -> c.getName())
                .contains("Grizzly Bears")
                .doesNotContain("Forest");
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).hasSize(1);
    }

    @Test
    @DisplayName("Does not affect the other player's permanents")
    void doesNotAffectOtherPlayersNonlandPermanents() {
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SuddenDisappearance()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertOnBattlefield(player1, "Gold Myr");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(c -> c.getName())
                .contains("Grizzly Bears");
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).hasSize(1);
    }

    @Test
    @DisplayName("Returns exiled permanents at beginning of next end step under owner's control")
    void returnsExiledPermanentsAtNextEndStep() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GoldMyr());
        harness.setHand(player1, List.of(new SuddenDisappearance()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.getDelayedActions(PendingExileReturn.class)).hasSize(1);

        advanceToEndStep();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Gold Myr");
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).isEmpty();
    }

    @Test
    @DisplayName("Can target self to exile own nonland permanents")
    void canTargetSelf() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GoldMyr());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new SuddenDisappearance()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Gold Myr");
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).hasSize(1);
    }

    @Test
    @DisplayName("Works when target player has no nonland permanents")
    void worksWithNoNonlandPermanents() {
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new SuddenDisappearance()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.getDelayedActions(PendingExileReturn.class)).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sudden Disappearance goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SuddenDisappearance()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Sudden Disappearance");
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
    }

    @Test
    @DisplayName("All exiled cards return together through one delayed triggered ability")
    void returnsAllCardsTogether() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GoldMyr());
        harness.setHand(player1, List.of(new SuddenDisappearance()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        advanceToEndStep();

        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Gold Myr");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Gold Myr");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Exiles an affected Aura together with its enchanted creature")
    void exilesAuraTogetherWithCreature() {
        var creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        var aura = harness.addToBattlefieldAndReturn(player2, new SpitefulShadows());
        aura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new SuddenDisappearance()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(c -> c.getName())
                .contains("Grizzly Bears", "Spiteful Shadows");
        harness.assertNotInGraveyard(player2, "Spiteful Shadows");
    }
}
