package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AirliftChaplain;
import com.github.laxika.magicalvibes.cards.c.CombatCourier;
import com.github.laxika.magicalvibes.cards.c.Curate;
import com.github.laxika.magicalvibes.cards.e.EnergyRefractor;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.Recommission;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({KeeperOfTheCadence.class, EnergyRefractor.class, Recommission.class, Curate.class,
        AirliftChaplain.class, CombatCourier.class, Forest.class, Island.class})
class KeeperOfTheCadenceTest extends BaseCardTest {

    @Test
    @DisplayName("Puts target artifact, instant, or sorcery cards on the bottom of their owners' libraries")
    void tucksSupportedCardTypesIntoOwnersLibraries() {
        int keeperIndex = addKeeper();
        harness.addMana(player1, ManaColor.COLORLESS, 9);

        Card artifact = new EnergyRefractor();
        Card sorcery = new Recommission();
        Card instant = new Curate();
        harness.setGraveyard(player1, List.of(artifact, sorcery));
        harness.setGraveyard(player2, List.of(instant));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Island()));

        harness.activateAbilityWithGraveyardTargets(player1, keeperIndex, 0, List.of(artifact.getId()));
        harness.passBothPriorities();
        harness.activateAbilityWithGraveyardTargets(player1, keeperIndex, 0, List.of(instant.getId()));
        harness.passBothPriorities();
        harness.activateAbilityWithGraveyardTargets(player1, keeperIndex, 0, List.of(sorcery.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getLast().getId()).isEqualTo(sorcery.getId());
        assertThat(gd.playerDecks.get(player2.getId()).getLast().getId()).isEqualTo(instant.getId());
    }

    @Test
    @DisplayName("Cannot target a nonartifact creature card")
    void rejectsUnsupportedCardType() {
        int keeperIndex = addKeeper();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Card creature = new AirliftChaplain();
        harness.setGraveyard(player1, List.of(creature));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, keeperIndex, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void putsArtifactCreatureIntoOpponentsEmptyLibrary() {
        int keeperIndex = addKeeper();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        Card courier = new CombatCourier();
        harness.setGraveyard(player2, List.of(courier));
        harness.setLibrary(player2, List.of());

        harness.activateAbilityWithGraveyardTargets(player1, keeperIndex, 0, List.of(courier.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(courier);
    }

    @Test
    void doesNotMoveAnotherCardWhenTargetLeavesGraveyardBeforeResolution() {
        int keeperIndex = addKeeper();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        Card target = new EnergyRefractor();
        Card other = new Curate();
        Card top = new Island();
        harness.setGraveyard(player2, List.of(target, other));
        harness.setLibrary(player2, List.of(top));

        harness.activateAbilityWithGraveyardTargets(player1, keeperIndex, 0, List.of(target.getId()));
        harness.activateAbilityWithGraveyardTargets(player1, keeperIndex, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(other);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top, target);
    }

    @Test
    void rejectsLandCard() {
        int keeperIndex = addKeeper();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        Card land = new Forest();
        harness.setGraveyard(player2, List.of(land));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, keeperIndex, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requiresExactlyOneTarget() {
        int keeperIndex = addKeeper();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        Card first = new EnergyRefractor();
        Card second = new Curate();
        harness.setGraveyard(player1, List.of(first, second));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, keeperIndex, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, keeperIndex, 0, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithOnlyTwoMana() {
        int keeperIndex = addKeeper();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Card target = new EnergyRefractor();
        harness.setGraveyard(player1, List.of(target));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, keeperIndex, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(target);
        assertThat(gd.stack).isEmpty();
    }

    private int addKeeper() {
        Permanent keeper = harness.addToBattlefieldAndReturn(player1, new KeeperOfTheCadence());
        return gd.playerBattlefields.get(player1.getId()).indexOf(keeper);
    }
}
