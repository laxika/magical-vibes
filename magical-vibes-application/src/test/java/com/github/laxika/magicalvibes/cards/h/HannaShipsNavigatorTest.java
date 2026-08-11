package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AuraOfSilence;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HannaShipsNavigatorTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target artifact card from your graveyard to your hand")
    void returnsArtifactFromGraveyardToHand() {
        Card artifact = new Ornithopter();
        harness.addToBattlefield(player1, new HannaShipsNavigator());
        harness.setGraveyard(player1, List.of(artifact));
        addActivationMana();

        harness.activateAbility(player1, 0, 0, null, artifact.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(artifact.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(artifact.getId()));
    }

    @Test
    @DisplayName("Returns target enchantment card from your graveyard to your hand")
    void returnsEnchantmentFromGraveyardToHand() {
        Card enchantment = new AuraOfSilence();
        harness.addToBattlefield(player1, new HannaShipsNavigator());
        harness.setGraveyard(player1, List.of(enchantment));
        addActivationMana();

        harness.activateAbility(player1, 0, 0, null, enchantment.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(enchantment.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(enchantment.getId()));
    }

    @Test
    @DisplayName("Cannot target a creature card")
    void cannotTargetCreatureCard() {
        Card creature = new GrizzlyBears();
        harness.addToBattlefield(player1, new HannaShipsNavigator());
        harness.setGraveyard(player1, List.of(creature));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, creature.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
