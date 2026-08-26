package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(GleamingGeardrake.class)
class GleamingGeardrakeTest extends BaseCardTest {

    @Test
    void entersAndInvestigates() {
        harness.enterBattlefieldAndReturn(player1, new GleamingGeardrake());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void growsWhenYouSacrificeAnArtifact() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new GleamingGeardrake());
        Permanent artifact = addPermanent(player1, CardType.ARTIFACT);

        sacrifice(player1, artifact);
        resolveAllTriggers();

        assertThat(drake.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotGrowWhenYouSacrificeANonartifact() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new GleamingGeardrake());
        Permanent creature = addPermanent(player1, CardType.CREATURE);

        sacrifice(player1, creature);
        resolveAllTriggers();

        assertThat(drake.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotGrowWhenAnOpponentSacrificesAnArtifact() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new GleamingGeardrake());
        Permanent artifact = addPermanent(player2, CardType.ARTIFACT);

        sacrifice(player2, artifact);
        resolveAllTriggers();

        assertThat(drake.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addPermanent(Player player, CardType type) {
        Card card = new Card();
        card.setType(type);
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void sacrifice(Player player, Permanent permanent) {
        Card card = permanent.getCard();
        gd.playerBattlefields.get(player.getId()).remove(permanent);
        gd.playerGraveyards.get(player.getId()).add(card);
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkAllyPermanentSacrificedTriggers(gd, player.getId(), card));
    }
}
