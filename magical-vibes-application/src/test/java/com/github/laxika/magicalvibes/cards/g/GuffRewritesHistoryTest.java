package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.OblivionRing;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GuffRewritesHistory.class, Forest.class, GrizzlyBears.class, OblivionRing.class})
class GuffRewritesHistoryTest extends BaseCardTest {

    @Test
    @DisplayName("Shuffles one eligible permanent per player and offers each controller their own exiled card")
    void shufflesTargetsAndOffersEachControllerTheirOwnCard() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent ownEnchantment = harness.addToBattlefieldAndReturn(player1, new OblivionRing());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        castGuff(List.of(ownCreature.getId(), opponentCreature.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownCreature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownLand, ownEnchantment);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1)
                .allMatch(card -> card.hasType(CardType.LAND));
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1)
                .allMatch(card -> card.hasType(CardType.LAND));
        assertThat(gd.findExiledCard(ownCreature.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(opponentCreature.getCard().getId())).isNotNull();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, false);
    }

    @Test
    @DisplayName("A player may cast the nonland card exiled for them without paying its mana cost")
    void mayCastExiledCardForFree() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));
        castGuff(List.of(ownCreature.getId(), opponentCreature.getId()));

        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(ownCreature.getCard().getId()));
        assertThat(gd.findExiledCard(ownCreature.getCard().getId())).isNull();
        assertThat(gd.findExiledCard(opponentCreature.getCard().getId())).isNotNull();
    }

    private void castGuff(List<java.util.UUID> targetIds) {
        harness.setHand(player1, List.of(new GuffRewritesHistory()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, targetIds);
        harness.passBothPriorities();
    }
}
