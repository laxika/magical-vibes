package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrazynTheInfinite.class, RodOfRuin.class, ProdigalPyromancer.class})
class TrazynTheInfiniteTest extends BaseCardTest {

    @Test
    @DisplayName("Gains activated abilities from artifact cards in its controller's graveyard")
    void gainsAbilitiesFromOwnArtifactGraveyard() {
        Permanent trazyn = addReadyTrazyn();
        harness.setGraveyard(player1, new ArrayList<>(List.of(new RodOfRuin())));

        List<ActivatedAbility> granted = gqs.computeStaticBonus(gd, trazyn).grantedActivatedAbilities();

        assertThat(granted).hasSize(1);
        assertThat(granted.getFirst().getManaCost()).isEqualTo("{3}");
        assertThat(granted.getFirst().isRequiresTap()).isTrue();
    }

    @Test
    @DisplayName("Ignores non-artifacts and artifacts in an opponent's graveyard")
    void filtersGraveyardCards() {
        Permanent trazyn = addReadyTrazyn();
        harness.setGraveyard(player1, new ArrayList<>(List.of(new ProdigalPyromancer())));
        harness.setGraveyard(player2, new ArrayList<>(List.of(new RodOfRuin())));

        assertThat(gqs.computeStaticBonus(gd, trazyn).grantedActivatedAbilities()).isEmpty();
    }

    @Test
    @DisplayName("Can activate and resolve a copied artifact ability")
    void activatesCopiedAbility() {
        Permanent trazyn = addReadyTrazyn();
        harness.setGraveyard(player1, new ArrayList<>(List.of(new RodOfRuin())));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(trazyn.isTapped()).isTrue();
    }

    private Permanent addReadyTrazyn() {
        Permanent trazyn = harness.addToBattlefieldAndReturn(player1, new TrazynTheInfinite());
        trazyn.setSummoningSick(false);
        return trazyn;
    }
}
