package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrazynTheInfinite.class, RodOfRuin.class, ProdigalPyromancer.class, MindStone.class, SolRing.class})
class TrazynTheInfiniteTest extends BaseCardTest {

    @Test
    @DisplayName("Gains activated abilities from artifact cards in its controller's graveyard")
    void gainsAbilitiesFromOwnArtifactGraveyard() {
        Permanent trazyn = addReadyTrazyn();
        harness.setGraveyard(player1, List.of(new RodOfRuin()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(trazyn.isTapped()).isFalse();
        harness.assertLife(player2, 20);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 19);
        assertThat(trazyn.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ignores non-artifacts and artifacts in an opponent's graveyard")
    void filtersGraveyardCards() {
        addReadyTrazyn();
        harness.setGraveyard(player1, List.of(new ProdigalPyromancer()));
        harness.setGraveyard(player2, List.of(new RodOfRuin()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Can activate and resolve a copied artifact ability")
    void activatesCopiedAbility() {
        Permanent trazyn = addReadyTrazyn();
        harness.setGraveyard(player1, List.of(new RodOfRuin()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(trazyn.isTapped()).isTrue();
    }

    private Permanent addReadyTrazyn() {
        return addCreatureReady(player1, new TrazynTheInfinite());
    }

    @Test
    void gainsManaAbilitiesAndResolvesThemWithoutUsingTheStack() {
        Permanent trazyn = addReadyTrazyn();
        harness.setGraveyard(player1, List.of(new SolRing()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(trazyn.isTapped()).isTrue();
    }

    @Test
    void copiedTapAbilityStillRequiresTrazynToNotBeSummoningSick() {
        harness.addToBattlefield(player1, new TrazynTheInfinite());
        harness.setGraveyard(player1, List.of(new SolRing()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    void sacrificeCostSacrificesTrazynAndNotTheGraveyardArtifact() {
        addReadyTrazyn();
        MindStone graveyardStone = new MindStone();
        MindStone drawnCard = new MindStone();
        harness.setGraveyard(player1, List.of(graveyardStone));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        harness.assertNotOnBattlefield(player1, "Trazyn the Infinite");
        harness.assertInGraveyard(player1, "Trazyn the Infinite");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardStone);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    void losesAbilityWhenArtifactLeavesGraveyard() {
        Permanent trazyn = addReadyTrazyn();
        harness.setGraveyard(player1, List.of(new SolRing()));
        harness.activateAbility(player1, 0, null, null);
        trazyn.untap();
        harness.setGraveyard(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(trazyn.isTapped()).isFalse();
    }

    @Test
    void activatedAbilitySurvivesArtifactLeavingGraveyard() {
        addReadyTrazyn();
        harness.setGraveyard(player1, List.of(new RodOfRuin()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, player2.getId());

        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    void copiedDamageAbilityUsesTrazynsDeathtouch() {
        addReadyTrazyn();
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new TrazynTheInfinite());
        harness.setGraveyard(player1, List.of(new RodOfRuin()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, victim.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Trazyn the Infinite");
        harness.assertInGraveyard(player2, "Trazyn the Infinite");
    }

    @Test
    void gainsAbilitiesFromEveryArtifactAndCanUseTheLaterArtifactsAbility() {
        Permanent trazyn = addReadyTrazyn();
        harness.setGraveyard(player1, List.of(new MindStone(), new SolRing()));

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(trazyn.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
