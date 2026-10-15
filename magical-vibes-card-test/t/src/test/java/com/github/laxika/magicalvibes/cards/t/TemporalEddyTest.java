package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BenalishCavalry;
import com.github.laxika.magicalvibes.cards.c.CandlesOfLeng;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TemporalEddy.class, Forest.class, BenalishCavalry.class, CandlesOfLeng.class})
class TemporalEddyTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a target creature on top of its owner's library")
    void putsCreatureOnTopOfOwnersLibrary() {
        Card targetCard = new BenalishCavalry();
        Permanent targetPermanent = harness.addToBattlefieldAndReturn(player2, targetCard);
        UUID targetId = targetPermanent.getId();
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        castAndResolve(targetId);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(targetCard);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(targetId));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(card -> card.getId().equals(targetCard.getId()));
    }

    @Test
    @DisplayName("Puts a target land on top of its owner's library")
    void putsLandOnTopOfOwnersLibrary() {
        Card targetCard = new Forest();
        Permanent targetPermanent = harness.addToBattlefieldAndReturn(player2, targetCard);
        UUID targetId = targetPermanent.getId();
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        castAndResolve(targetId);

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore + 1);
        assertThat(gd.playerDecks.get(player2.getId()).getFirst()).isSameAs(targetCard);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(targetId));
    }

    @Test
    @DisplayName("Cannot target a noncreature nonland permanent")
    void cannotTargetNonCreatureNonlandPermanent() {
        Permanent targetPermanent = harness.addToBattlefieldAndReturn(player2, new CandlesOfLeng());
        UUID targetId = targetPermanent.getId();

        harness.setHand(player1, List.of(new TemporalEddy()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature or land");
    }

    @Test
    @DisplayName("Can target its controller's creature without changing existing library order")
    void targetsOwnCreatureAndPreservesLibraryOrder() {
        Card targetCard = new BenalishCavalry();
        Permanent target = harness.addToBattlefieldAndReturn(player1, targetCard);
        Card first = new Forest();
        Card second = new CandlesOfLeng();
        harness.setLibrary(player1, List.of(first, second));

        castAndResolve(target.getId());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(targetCard, first, second);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof TemporalEddy);
    }

    @Test
    @DisplayName("Puts an opponent-owned creature into its owner's library rather than its controller's")
    void returnsControlledCreatureToOwnersLibrary() {
        Card targetCard = new BenalishCavalry();
        targetCard.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, targetCard);
        Card controllerTop = new Forest();
        Card ownerTop = new CandlesOfLeng();
        harness.setLibrary(player1, List.of(controllerTop));
        harness.setLibrary(player2, List.of(ownerTop));

        castAndResolve(target.getId());

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(targetCard, ownerTop);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(controllerTop);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Does not move a target that has left the battlefield before resolution")
    void doesNotMoveTargetThatLeftBattlefield() {
        Card targetCard = new BenalishCavalry();
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);
        Card libraryTop = new Forest();
        harness.setLibrary(player2, List.of(libraryTop));
        harness.setHand(player1, List.of(new TemporalEddy()));
        addMana();
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(targetCard));

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(libraryTop);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(targetCard);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof TemporalEddy);
        assertThat(gd.stack).isEmpty();
    }
    private void castAndResolve(UUID targetId) {
        harness.setHand(player1, List.of(new TemporalEddy()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, targetId);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
