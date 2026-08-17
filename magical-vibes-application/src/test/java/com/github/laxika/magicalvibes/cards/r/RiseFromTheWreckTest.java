package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AlacrianJaguar;
import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RiseFromTheWreckTest extends BaseCardTest {

    @Test
    void returnsUpToOneCardForEachTargetGroup() {
        Card creature = new LlanowarElves();
        Card mount = new AlacrianJaguar();
        Card vehicle = new DuskLegionDreadnought();
        Card creatureWithNoAbilities = new GrizzlyBears();
        RiseFromTheWreck spell = new RiseFromTheWreck();
        harness.setGraveyard(player1, List.of(creature, mount, vehicle, creatureWithNoAbilities));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(4);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                creature.getId(), mount.getId(), vehicle.getId(), creatureWithNoAbilities.getId());

        harness.handleMultipleCardsChosen(player1,
                List.of(creature.getId(), mount.getId(), vehicle.getId(), creatureWithNoAbilities.getId()));
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(creature, mount, vehicle, creatureWithNoAbilities);
    }

    @Test
    void cannotUseOneCreatureForTwoTargetGroups() {
        Card firstCreature = new LlanowarElves();
        Card secondCreature = new LlanowarElves();
        harness.setGraveyard(player1, List.of(firstCreature, secondCreature));
        harness.setHand(player1, List.of(new RiseFromTheWreck()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("different target group");
    }
}
