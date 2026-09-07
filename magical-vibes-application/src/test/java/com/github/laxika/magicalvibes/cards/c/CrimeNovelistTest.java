package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrimeNovelist.class})
class CrimeNovelistTest extends BaseCardTest {

    @Test
    void sacrificingAnArtifactAddsACounterAndRedMana() {
        Permanent novelist = harness.addToBattlefieldAndReturn(player1, new CrimeNovelist());
        Permanent artifact = addPermanent(player1, CardType.ARTIFACT);

        sacrifice(artifact);
        resolveAllTriggers();

        assertThat(novelist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void sacrificingANonArtifactDoesNotTrigger() {
        Permanent novelist = harness.addToBattlefieldAndReturn(player1, new CrimeNovelist());
        Permanent creature = addPermanent(player1, CardType.CREATURE);

        sacrifice(creature);
        resolveAllTriggers();

        assertThat(novelist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }

    private Permanent addPermanent(Player player, CardType type) {
        Card card = new Card();
        card.setType(type);
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void sacrifice(Permanent permanent) {
        Card card = permanent.getCard();
        gd.playerBattlefields.get(player1.getId()).remove(permanent);
        gd.playerGraveyards.get(player1.getId()).add(card);
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkAllyPermanentSacrificedTriggers(gd, player1.getId(), card));
    }
}
