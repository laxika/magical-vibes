package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PropheticPrismTest extends BaseCardTest {

    @Test
    @DisplayName("ETB draws a card")
    void etbDrawsACard() {
        harness.setHand(player1, List.of(new PropheticPrism()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int libraryBefore = gd.playerDecks.get(player1.getId()).size();

        harness.castArtifact(player1, 0, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libraryBefore - 1);
    }

    @Test
    @DisplayName("Ability prompts for a color and adds one mana of it")
    void abilityAddsChosenColor() {
        harness.addToBattlefield(player1, new PropheticPrism());
        Permanent prism = gd.playerBattlefields.get(player1.getId()).getFirst();
        prism.setSummoningSick(false);

        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(prism.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
