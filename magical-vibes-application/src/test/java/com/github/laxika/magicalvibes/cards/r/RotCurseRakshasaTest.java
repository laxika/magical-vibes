package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RotCurseRakshasa.class, GrizzlyBears.class})
class RotCurseRakshasaTest extends BaseCardTest {

    @Test
    @DisplayName("Renew puts decayed counters on X target creatures and exiles the card")
    void renewPutsDecayedCountersOnTargetCreatures() {
        RotCurseRakshasa rakshasa = new RotCurseRakshasa();
        harness.setGraveyard(player1, List.of(rakshasa));
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLACK, 4);

        gs.activateGraveyardAbility(gd, player1, 0, 0, 2, null, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.DECAYED)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.DECAYED)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, first, com.github.laxika.magicalvibes.model.Keyword.DECAYED)).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(rakshasa.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(rakshasa.getId()));
    }

    @Test
    @DisplayName("A decayed Rot-Curse Rakshasa cannot block and is sacrificed after attacking")
    void decayedCreatureCannotBlockAndIsSacrificedAfterAttacking() {
        Permanent rakshasa = addCreatureReady(player1, new RotCurseRakshasa());

        assertThat(bls.canBlock(gd, rakshasa)).isFalse();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(rakshasa.getId()));
    }
}
