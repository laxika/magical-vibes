package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BonebindOrator.class, GrizzlyBears.class})
class BonebindOratorTest extends BaseCardTest {

    @Test
    void exilesItselfAndReturnsAnotherCreatureToHand() {
        Card orator = new BonebindOrator();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(orator, creature));
        addActivationMana();

        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(creature.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(orator.getId()));
    }

    @Test
    void cannotTargetItself() {
        Card orator = new BonebindOrator();
        harness.setGraveyard(player1, List.of(orator));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(orator.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(orator);
        assertThat(gd.exiledCards).noneMatch(exiled -> exiled.card().getId().equals(orator.getId()));
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
