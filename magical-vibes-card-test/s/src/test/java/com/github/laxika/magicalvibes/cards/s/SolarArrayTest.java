package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.ArcboundBruiser;
import com.github.laxika.magicalvibes.cards.m.MyrRetriever;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SolarArray.class, ArcboundBruiser.class, MyrRetriever.class})
class SolarArrayTest extends BaseCardTest {

    @Test
    void givesTheNextArtifactSpellSunburst() {
        harness.addToBattlefieldAndReturn(player1, new SolarArray());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.WHITE.name());

        harness.setHand(player1, List.of(new ArcboundBruiser()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent bruiser = findPermanent(player1, "Arcbound Bruiser");
        assertThat(bruiser.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    void boonIsConsumedByTheFirstArtifactSpell() {
        harness.addToBattlefieldAndReturn(player1, new SolarArray());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.WHITE.name());

        MyrRetriever first = new MyrRetriever();
        MyrRetriever second = new MyrRetriever();
        harness.setHand(player1, List.of(first, second));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(first).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(findPermanent(second).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent findPermanent(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
