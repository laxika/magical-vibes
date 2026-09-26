package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AlabasterLeech;
import com.github.laxika.magicalvibes.cards.c.ChromaticSphere;
import com.github.laxika.magicalvibes.cards.c.CollectiveRestraint;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HannaShipsNavigator.class, ChromaticSphere.class, CollectiveRestraint.class,
        AlabasterLeech.class})
class HannaShipsNavigatorTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target artifact card from your graveyard to your hand")
    void returnsArtifactFromGraveyardToHand() {
        Card artifact = new ChromaticSphere();
        Permanent hanna = addCreatureReady(player1, new HannaShipsNavigator());
        harness.setGraveyard(player1, List.of(artifact));
        addActivationMana();

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(artifact.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(artifact.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(artifact.getId()));
        assertThat(hanna.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Returns target enchantment card from your graveyard to your hand")
    void returnsEnchantmentFromGraveyardToHand() {
        Card enchantment = new CollectiveRestraint();
        addCreatureReady(player1, new HannaShipsNavigator());
        harness.setGraveyard(player1, List.of(enchantment));
        addActivationMana();

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(enchantment.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(enchantment.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(enchantment.getId()));
    }

    @Test
    @DisplayName("Cannot target a creature card")
    void cannotTargetCreatureCard() {
        Card creature = new AlabasterLeech();
        addCreatureReady(player1, new HannaShipsNavigator());
        harness.setGraveyard(player1, List.of(creature));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target an artifact in an opponent's graveyard")
    void cannotTargetOpponentsGraveyard() {
        Card artifact = new ChromaticSphere();
        Permanent hanna = addCreatureReady(player1, new HannaShipsNavigator());
        harness.setGraveyard(player2, List.of(artifact));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(artifact);
        assertThat(hanna.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
