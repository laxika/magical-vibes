package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CloudKey.class, GrizzlyBears.class, Divination.class})
class CloudKeyTest extends BaseCardTest {

    @Test
    void choosesOneOfThePrintedCardTypes() {
        castCloudKey();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly(
                CardType.CREATURE.name(),
                CardType.ENCHANTMENT.name(),
                CardType.SORCERY.name(),
                CardType.INSTANT.name(),
                CardType.ARTIFACT.name());
    }

    @Test
    void spellsOfChosenTypeCostOneLess() {
        castCloudKey();
        harness.handleListChoice(player1, CardType.CREATURE.name());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void spellsOfOtherTypesAreNotReduced() {
        castCloudKey();
        harness.handleListChoice(player1, CardType.CREATURE.name());

        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reductionDoesNotApplyToOpponents() {
        castCloudKey();
        harness.handleListChoice(player1, CardType.CREATURE.name());

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castCloudKey() {
        harness.setHand(player1, List.of(new CloudKey()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
    }
}
