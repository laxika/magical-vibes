package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.c.CatharCommando;
import com.github.laxika.magicalvibes.cards.s.SigardaHostOfHerons;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KillingWave.class, GrizzlyBears.class, HillGiant.class,
        CatharCommando.class, SigardaHostOfHerons.class})
class KillingWaveTest extends BaseCardTest {

    @Test
    @DisplayName("Casting stores the paid X on the stack entry")
    void castingStoresX() {
        harness.setHand(player1, List.of(new KillingWave()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, 3);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getXValue()).isEqualTo(3);
    }

    @Test
    @DisplayName("X=0 allows both players to pay zero life to keep their creatures")
    void xZeroCanKeepEveryCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new KillingWave()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        harness.handleMultiplePermanentsChosen(player2, List.of(giant.getId()));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Paying X life keeps a creature; declining sacrifices it")
    void payKeepsDeclineSacrifices() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new KillingWave()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 2);

        // Active player chooses first — keep the Bears.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        // Opponent declines to pay — sacrifice Hill Giant.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A player may keep some creatures and sacrifice the rest")
    void partialKeep() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new KillingWave()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 2);

        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears"))
                .hasSize(1)
                .first()
                .extracting(Permanent::getId)
                .isEqualTo(first.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Can't pay more life than available — max keeps is floored by life/X")
    void lifeCapsKeeps() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 5); // X=3 → at most 1 keep
        harness.setHand(player1, List.of(new KillingWave()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 3);

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.maxCount()).isEqualTo(1);
        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears"))
                .hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    @Test
    @DisplayName("A player who can't afford any payment auto-sacrifices with no prompt")
    void cantAffordAutoSacrifices() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLife(player1, 2);
        harness.setHand(player1, List.of(new KillingWave()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 3);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(2);
    }

    @Test
    @CardUsed({KillingWave.class, CatharCommando.class})
    @DisplayName("At X=0 a player may decline payment for some creatures")
    void xZeroAllowsSelectiveSacrifice() {
        Permanent kept = harness.addToBattlefieldAndReturn(player1, new CatharCommando());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new CatharCommando());
        harness.setHand(player1, List.of(new KillingWave()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultiplePermanentsChosen(player1, List.of(kept.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId).contains(kept.getId()).doesNotContain(sacrificed.getId());
        harness.assertInGraveyard(player1, "Cathar Commando");
        harness.assertLife(player1, 20);
    }

    @Test
    @CardUsed({KillingWave.class, CatharCommando.class, SigardaHostOfHerons.class})
    @DisplayName("Sigarda lets an opponent decline payment without sacrificing creatures")
    void sigardaPreventsOpponentCausedSacrifices() {
        harness.addToBattlefield(player2, new SigardaHostOfHerons());
        harness.addToBattlefield(player2, new CatharCommando());
        harness.setHand(player1, List.of(new KillingWave()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 2);
        harness.handleMultiplePermanentsChosen(player2, List.of());

        harness.assertOnBattlefield(player2, "Sigarda, Host of Herons");
        harness.assertOnBattlefield(player2, "Cathar Commando");
        harness.assertNotInGraveyard(player2, "Sigarda, Host of Herons");
        harness.assertNotInGraveyard(player2, "Cathar Commando");
        harness.assertLife(player2, 20);
    }

    @Test
    @CardUsed({KillingWave.class, CatharCommando.class, SigardaHostOfHerons.class})
    @DisplayName("Sigarda does not prevent sacrifices caused by its controller's Killing Wave")
    void sigardaDoesNotPreventOwnSacrifices() {
        harness.addToBattlefield(player1, new SigardaHostOfHerons());
        harness.addToBattlefield(player1, new CatharCommando());
        harness.setHand(player1, List.of(new KillingWave()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 2);
        harness.handleMultiplePermanentsChosen(player1, List.of());

        harness.assertNotOnBattlefield(player1, "Sigarda, Host of Herons");
        harness.assertNotOnBattlefield(player1, "Cathar Commando");
        harness.assertInGraveyard(player1, "Sigarda, Host of Herons");
        harness.assertInGraveyard(player1, "Cathar Commando");
        harness.assertLife(player1, 20);
    }

    @Test
    @CardUsed({KillingWave.class})
    @DisplayName("With no creatures, Killing Wave resolves without choices or life payments")
    void emptyBattlefieldNeedsNoChoices() {
        harness.setHand(player1, List.of(new KillingWave()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, 3);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Killing Wave");
    }

    @Test
    @DisplayName("Each player chooses in APNAP order")
    void apnapOrder() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new KillingWave()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveSorcery(player1, 0, 1);

        UUID firstChooser = gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId();
        assertThat(firstChooser).isEqualTo(player1.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Hill Giant");
    }
}
