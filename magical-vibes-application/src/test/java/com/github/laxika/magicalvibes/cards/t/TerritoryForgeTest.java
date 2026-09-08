package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TerritoryForge.class, Forest.class, GrizzlyBears.class, RodOfRuin.class})
class TerritoryForgeTest extends BaseCardTest {

    @Test
    @DisplayName("When cast, exiles a target artifact and gains its activated abilities")
    void exilesArtifactAndGainsItsAbilities() {
        Permanent rod = harness.addToBattlefieldAndReturn(player2, new RodOfRuin());

        Permanent forge = castForge(rod);

        assertThat(gd.getCardsExiledByPermanent(forge.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Rod of Ruin");
        var granted = gqs.computeStaticBonus(gd, forge).grantedActivatedAbilities();
        assertThat(granted).hasSize(1);
        assertThat(granted.getFirst().getManaCost()).isEqualTo("{3}");

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("When cast, can target and exile a land")
    void exilesLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        Permanent forge = castForge(forest);

        assertThat(gd.getCardsExiledByPermanent(forge.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest");
        assertThat(findPermanents(player2, "Forest")).isEmpty();
    }

    @Test
    @DisplayName("Gains activated abilities of a tracked exiled artifact")
    void gainsDirectlyTrackedArtifactAbility() {
        Permanent forge = harness.addToBattlefieldAndReturn(player1, new TerritoryForge());
        gd.addToExile(player1.getId(), new RodOfRuin(), forge.getId());

        assertThat(gqs.computeStaticBonus(gd, forge).grantedActivatedAbilities()).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        TerritoryForge forge = new TerritoryForge();
        harness.setHand(player1, List.of(forge));
        addForgeMana();

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or land");
    }

    private Permanent castForge(Permanent target) {
        harness.setHand(player1, List.of(new TerritoryForge()));
        addForgeMana();
        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Territory Forge");
    }

    private void addForgeMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
