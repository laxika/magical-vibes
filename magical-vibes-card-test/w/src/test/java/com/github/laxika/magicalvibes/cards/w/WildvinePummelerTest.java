package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BitterbloomBearer;
import com.github.laxika.magicalvibes.cards.e.EnragedFlamecaster;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.l.LluwenImperfectNaturalist;
import com.github.laxika.magicalvibes.cards.p.PrismaticUndercurrents;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WildvinePummeler.class, EnragedFlamecaster.class, LluwenImperfectNaturalist.class,
        PrismaticUndercurrents.class, Forest.class, BitterbloomBearer.class})
class WildvinePummelerTest extends BaseCardTest {

    @Test
    @DisplayName("Costs one less for one distinct color among permanents you control")
    void costsOneLessForOneColor() {
        addCreatureReady(player1, new WildvinePummeler());
        harness.setHand(player1, List.of(new WildvinePummeler()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Counts each controlled color only once")
    void countsEachColorOnlyOnce() {
        addCreatureReady(player1, new WildvinePummeler());
        addCreatureReady(player1, new WildvinePummeler());
        harness.setHand(player1, List.of(new WildvinePummeler()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Does not count colors among permanents controlled by an opponent")
    void ignoresOpponentsColors() {
        addCreatureReady(player2, new EnragedFlamecaster());
        harness.setHand(player1, List.of(new WildvinePummeler()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void costsFullManaWithNoControlledPermanents() {
        harness.setHand(player1, List.of(new WildvinePummeler()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void countsBothColorsOfOneHybridPermanent() {
        harness.addToBattlefield(player1, new LluwenImperfectNaturalist());
        harness.setHand(player1, List.of(new WildvinePummeler()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void countsColorsOfNoncreaturePermanents() {
        harness.addToBattlefield(player1, new PrismaticUndercurrents());
        harness.setHand(player1, List.of(new WildvinePummeler()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void basicLandDoesNotContributeItsManaColor() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new WildvinePummeler()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void reductionDoesNotPayTheGreenRequirement() {
        harness.addToBattlefield(player1, new LluwenImperfectNaturalist());
        harness.setHand(player1, List.of(new WildvinePummeler()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void colorsOfCardsInHandAndGraveyardDoNotReduceCost() {
        harness.setHand(player1, List.of(new WildvinePummeler(), new EnragedFlamecaster()));
        harness.setGraveyard(player1, List.of(new LluwenImperfectNaturalist()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void reachAllowsBlockingAFlyingCreature() {
        addCreatureReady(player1, new BitterbloomBearer());
        Permanent pummeler = addCreatureReady(player2, new WildvinePummeler());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(pummeler.isBlocking()).isTrue();
    }

    @Test
    void trampleDealsExcessDamageToDefendingPlayer() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new WildvinePummeler());
        Permanent blocker = addCreatureReady(player2, new LluwenImperfectNaturalist());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 3, player2.getId(), 3));

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player2, "Lluwen, Imperfect Naturalist");
        harness.assertOnBattlefield(player1, "Wildvine Pummeler");
    }
}
