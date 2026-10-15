package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirliftChaplain;
import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.cards.t.TheMightstoneAndWeakstone;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SplittingThePowerstone.class, TheMightstoneAndWeakstone.class, EnergyRefractor.class, AirliftChaplain.class})
class SplittingThePowerstoneTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two tapped Powerstones and draws when the sacrificed artifact is legendary")
    void legendaryArtifactCreatesPowerstonesAndDraws() {
        TheMightstoneAndWeakstone legendaryArtifact = new TheMightstoneAndWeakstone();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, legendaryArtifact);
        harness.setLibrary(player1, List.of(new EnergyRefractor()));
        harness.setHand(player1, List.of(new SplittingThePowerstone()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorceryWithSacrifice(player1, 0, artifact.getId());
        harness.passBothPriorities();

        List<Permanent> powerstones = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.POWERSTONE))
                .toList();
        assertThat(powerstones).hasSize(2);
        assertThat(powerstones).allMatch(permanent -> permanent.isTapped()
                && permanent.getCard().hasType(CardType.ARTIFACT));
        harness.assertInHand(player1, "Energy Refractor");
    }

    @Test
    @DisplayName("Does not draw when the sacrificed artifact is not legendary")
    void nonlegendaryArtifactDoesNotDraw() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());
        harness.setLibrary(player1, List.of(new EnergyRefractor()));
        harness.setHand(player1, List.of(new SplittingThePowerstone()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorceryWithSacrifice(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card instanceof EnergyRefractor);
    }

    @Test
    @DisplayName("Cannot cast without an artifact to sacrifice")
    void cannotCastWithoutArtifact() {
        harness.setHand(player1, List.of(new SplittingThePowerstone()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    void sacrificeIsPaidBeforeResolution() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());
        harness.setHand(player1, List.of(new SplittingThePowerstone()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorceryWithSacrifice(player1, 0, artifact.getId());

        harness.assertNotOnBattlefield(player1, "Energy Refractor");
        harness.assertInGraveyard(player1, "Energy Refractor");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void remembersLegendaryArtifactAfterItLeavesGraveyard() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new TheMightstoneAndWeakstone());
        harness.setLibrary(player1, List.of(new EnergyRefractor()));
        harness.setHand(player1, List.of(new SplittingThePowerstone()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorceryWithSacrifice(player1, 0, artifact.getId());

        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Energy Refractor");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    void cannotSacrificeOpponentsArtifact() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new EnergyRefractor());
        harness.setHand(player1, List.of(new SplittingThePowerstone()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Energy Refractor");
        harness.assertInHand(player1, "Splitting the Powerstone");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotSacrificeNonartifact() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AirliftChaplain());
        harness.setHand(player1, List.of(new SplittingThePowerstone()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Airlift Chaplain");
        harness.assertInHand(player1, "Splitting the Powerstone");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void powerstonesProduceRestrictedManaAfterUntapping() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new EnergyRefractor());
        harness.setHand(player1, List.of(new SplittingThePowerstone()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castSorceryWithSacrifice(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.performUntapStep(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(Permanent::isTapped);
        harness.setHand(player1, List.of(new EnergyRefractor()));
        harness.castArtifact(player1, 0);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getPowerstoneOnlyColorless()).isZero();
    }
}
