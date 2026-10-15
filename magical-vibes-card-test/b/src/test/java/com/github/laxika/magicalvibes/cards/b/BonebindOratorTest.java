package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DaggerfangDuo;
import com.github.laxika.magicalvibes.cards.f.FeedTheCycle;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BonebindOrator.class, DaggerfangDuo.class, FeedTheCycle.class})
class BonebindOratorTest extends BaseCardTest {

    @Test
    void exilesItselfAndReturnsAnotherCreatureToHand() {
        Card orator = new BonebindOrator();
        Card creature = new DaggerfangDuo();
        harness.setGraveyard(player1, List.of(orator, creature));
        addActivationMana();

        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0, List.of(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(orator.getId()));
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
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

    @Test
    void cannotTargetNoncreatureCard() {
        Card orator = new BonebindOrator();
        Card instant = new FeedTheCycle();
        harness.setGraveyard(player1, List.of(orator, instant));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(instant.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(orator, instant);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetOpponentsCreatureCard() {
        Card orator = new BonebindOrator();
        Card creature = new DaggerfangDuo();
        harness.setGraveyard(player1, List.of(orator));
        harness.setGraveyard(player2, List.of(creature));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(orator);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithoutBlackMana() {
        Card orator = new BonebindOrator();
        Card creature = new DaggerfangDuo();
        harness.setGraveyard(player1, List.of(orator, creature));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(orator, creature);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotReturnAnotherCreatureWhenTargetLeavesGraveyard() {
        Card orator = new BonebindOrator();
        Card target = new DaggerfangDuo();
        Card other = new DaggerfangDuo();
        harness.setGraveyard(player1, List.of(orator, target, other));
        addActivationMana();

        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(other));
        harness.setHand(player1, List.of(target));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(orator.getId()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotChooseMoreThanOneTarget() {
        Card orator = new BonebindOrator();
        Card first = new DaggerfangDuo();
        Card second = new DaggerfangDuo();
        harness.setGraveyard(player1, List.of(orator, first, second));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateGraveyardAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(orator, first, second);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canReturnAnotherBonebindOrator() {
        harness.setHand(player1, List.of());
        Card orator = new BonebindOrator();
        Card other = new BonebindOrator();
        harness.setGraveyard(player1, List.of(orator, other));
        addActivationMana();

        harness.activateGraveyardAbilityWithGraveyardTargets(player1, 0, 0, List.of(other.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(other);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).anyMatch(exiled -> exiled.card().getId().equals(orator.getId()));
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
