package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.ObsidianBattleAxe;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SecondSunriseTest extends BaseCardTest {

    private void castSecondSunrise() {
        harness.setHand(player1, List.of(new SecondSunrise()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private boolean isOnBattlefield(UUID playerId, UUID cardId) {
        return gd.playerBattlefields.get(playerId).stream()
                .anyMatch(permanent -> permanent.getCard().getId().equals(cardId));
    }

    @Test
    @DisplayName("Each player returns artifact, creature, enchantment, and land cards put there from the battlefield this turn")
    void returnsQualifyingCardsForEachPlayer() {
        Permanent p1Creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent p1Artifact = harness.addToBattlefieldAndReturn(player1, new ObsidianBattleAxe());
        Permanent p1Enchantment = harness.addToBattlefieldAndReturn(player1, new GloriousAnthem());
        Permanent p1Land = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent p2Creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent p2Land = harness.addToBattlefieldAndReturn(player2, new Plains());

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, p1Creature);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, p1Artifact);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, p1Enchantment);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, p1Land);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, p2Creature);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, p2Land);
        });

        castSecondSunrise();

        assertThat(isOnBattlefield(player1.getId(), p1Creature.getCard().getId())).isTrue();
        assertThat(isOnBattlefield(player1.getId(), p1Artifact.getCard().getId())).isTrue();
        assertThat(isOnBattlefield(player1.getId(), p1Enchantment.getCard().getId())).isTrue();
        assertThat(isOnBattlefield(player1.getId(), p1Land.getCard().getId())).isTrue();
        assertThat(isOnBattlefield(player2.getId(), p2Creature.getCard().getId())).isTrue();
        assertThat(isOnBattlefield(player2.getId(), p2Land.getCard().getId())).isTrue();
    }

    @Test
    @DisplayName("Does not return cards that were not put into the graveyard from the battlefield this turn")
    void ignoresOldCardsAndNonpermanents() {
        Card oldCreature = new GrizzlyBears();
        Card instant = new DoomBlade();
        Card p2OldCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(oldCreature, instant));
        harness.setGraveyard(player2, List.of(p2OldCreature));

        castSecondSunrise();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(oldCreature.getId(), instant.getId());
        assertThat(gameData.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(p2OldCreature.getId());
        assertThat(isOnBattlefield(player1.getId(), oldCreature.getId())).isFalse();
    }
}
