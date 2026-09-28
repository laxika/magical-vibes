package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VeiledAscension.class, GrizzlyBears.class})
class VeiledAscensionTest extends BaseCardTest {

    @Test
    void entersWithFlyingCountersOnOwnFaceDownCreaturesOnly() {
        Permanent ownFaceDown = addFaceDownCreature(player1);
        Permanent opponentFaceDown = addFaceDownCreature(player2);
        Permanent ownFaceUp = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new VeiledAscension());
        resolveAllTriggers();

        assertThat(ownFaceDown.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(ownFaceDown.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(opponentFaceDown.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(ownFaceUp.getCounterCount(CounterType.FLYING)).isZero();
    }

    @Test
    void upkeepMayCloakTopCardOfLibrary() {
        Permanent ascension = harness.addToBattlefieldAndReturn(player1, new VeiledAscension());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent cloaked = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == topCard)
                .findFirst()
                .orElseThrow();
        assertThat(cloaked.isFaceDown()).isTrue();
        assertThat(cloaked.isCloaked()).isTrue();
        assertThat(cloaked.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(ascension).isIn(gd.playerBattlefields.get(player1.getId()));
    }

    @Test
    void decliningUpkeepAbilityLeavesTopCardOnLibrary() {
        harness.addToBattlefieldAndReturn(player1, new VeiledAscension());
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == topCard);
    }

    private Permanent addFaceDownCreature(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        permanent.setFaceDownAsCloaked();
        return permanent;
    }
}
