package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AshesToAshes.class, GrizzlyBears.class, HillGiant.class, Ornithopter.class, Unsummon.class})
class AshesToAshesTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles two nonartifact creatures and deals 5 damage to controller")
    void exilesTwoCreaturesAndDamagesController() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new AshesToAshes()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castAndResolveSorcery(player1, 0, List.of(bearsId, giantId));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        // Exiled, not to graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears")
                        || c.getName().equals("Hill Giant"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Hill Giant"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 5);
    }

    @Test
    @DisplayName("Can target nonartifact creatures controlled by the caster")
    void canTargetOwnCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new AshesToAshes()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        UUID giantId = harness.getPermanentId(player1, "Hill Giant");
        harness.castAndResolveSorcery(player1, 0, List.of(bearsId, giantId));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Hill Giant"));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 5);
    }

    @Test
    @DisplayName("Deals 5 damage even when a target is removed before resolution")
    void damagesEvenWhenTargetRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new AshesToAshes()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castSorcery(player1, 0, List.of(bearsId, giantId));

        gd.playerBattlefields.get(player2.getId())
                .removeIf(p -> p.getCard().getName().equals("Grizzly Bears"));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Hill Giant");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 5);
    }

    @Test
    @DisplayName("Does not deal damage when both targets are removed before resolution")
    void doesNotDealDamageWhenBothTargetsAreRemoved() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new AshesToAshes()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castSorcery(player1, 0, List.of(bearsId, giantId));

        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        harness.addToBattlefield(player2, new Ornithopter());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AshesToAshes()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID artifactId = harness.getPermanentId(player2, "Ornithopter");
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(artifactId, bearsId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("nonartifact");
    }

    @Test
    @DisplayName("Cannot choose the same creature for both targets")
    void cannotTargetSameCreatureTwice() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AshesToAshes()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bearsId, bearsId)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("All targets must be different");
    }

    @Test
    @DisplayName("Requires exactly two targets")
    void cannotCastWithOnlyOneTarget() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AshesToAshes()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(bearsId)))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Exiles creatures controlled by different players")
    void canTargetCreaturesWithDifferentControllers() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new AshesToAshes()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveSorcery(player1, 0, List.of(
                harness.getPermanentId(player1, "Grizzly Bears"),
                harness.getPermanentId(player2, "Hill Giant")));

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Hill Giant"));
        harness.assertLife(player1, 15);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Exiles the first target and deals damage when the second target is bounced")
    void resolvesWithOnlyFirstTargetStillLegal() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new AshesToAshes()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.setLife(player1, 20);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castSorcery(player1, 0, List.of(bearsId, giantId));
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, giantId);
        harness.passBothPriorities();

        harness.assertInHand(player2, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .noneMatch(c -> c.getName().equals("Hill Giant"));
        harness.assertLife(player1, 15);
        harness.assertInGraveyard(player1, "Ashes to Ashes");
    }
}
