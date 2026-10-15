package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AlacrianJaguar;
import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RiseFromTheWreck.class, AlacrianJaguar.class, DuskLegionDreadnought.class,
        GrizzlyBears.class, LlanowarElves.class})
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

    @Test
    void mayDeclineAllTargets() {
        Card mount = new AlacrianJaguar();
        harness.setGraveyard(player1, List.of(mount));
        harness.setHand(player1, List.of(new RiseFromTheWreck()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(mount);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnsTwoMountsAsCreatureAndMountButNotThree() {
        Card first = new AlacrianJaguar();
        Card second = new AlacrianJaguar();
        Card third = new AlacrianJaguar();
        harness.setGraveyard(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new RiseFromTheWreck()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(third).doesNotContain(first, second);
    }

    @Test
    void cannotTargetCardsInOpponentsGraveyard() {
        Card ownMount = new AlacrianJaguar();
        Card opposingMount = new AlacrianJaguar();
        harness.setGraveyard(player1, List.of(ownMount));
        harness.setGraveyard(player2, List.of(opposingMount));
        harness.setHand(player1, List.of(new RiseFromTheWreck()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(opposingMount.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(ownMount.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ownMount);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingMount);
    }

    @Test
    void mayTargetSameMountForCreatureAndMountCategories() {
        Card mount = new AlacrianJaguar();
        harness.setGraveyard(player1, List.of(mount));
        harness.setHand(player1, List.of(new RiseFromTheWreck()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(mount.getId(), mount.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(mount);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(mount);
    }

    @Test
    void canCastWithEmptyGraveyard() {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new RiseFromTheWreck()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Rise from the Wreck");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnsRemainingTargetWhenAnotherLeavesGraveyardBeforeResolution() {
        Card first = new AlacrianJaguar();
        Card second = new AlacrianJaguar();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new RiseFromTheWreck()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.setGraveyard(player1, List.of(second));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(second);
    }
}
