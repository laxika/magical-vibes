package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RangersMerit;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UrsineGuideRangersMerit.class, RangersMerit.class, BearCub.class, GrizzlyBears.class})
class UrsineGuideRangersMeritTest extends BaseCardTest {

    @Test
    void preparedRangersMeritBoostsBearsConjuresBearCubAndReprepares() {
        Permanent guide = harness.enterBattlefieldAndReturn(player1, new UrsineGuideRangersMerit());
        Permanent existingBear = harness.enterBattlefieldAndReturn(player1, new BearCub());
        resolveAllTriggers();

        UUID preparedSpellId = guide.getPreparedSpellCardId();
        harness.forceActivePlayer(player1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.GREEN, 2);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 3);
        harness.castFromExile(player1, preparedSpellId);
        resolveAllTriggers();

        List<Permanent> bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.BEAR))
                .toList();
        assertThat(bears).hasSize(3);
        assertThat(guide.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(existingBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(bears).allSatisfy(bear -> assertThat(bear.hasKeyword(Keyword.TRAMPLE)).isTrue());
        assertThat(guide.isPrepared()).isTrue();
        assertThat(guide.getPreparedSpellCardId()).isNotNull();
    }

    @Test
    void startingDeckSpellDoesNotTrigger() {
        Permanent guide = harness.enterBattlefieldAndReturn(player1, new UrsineGuideRangersMerit());
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() instanceof BearCub);
        assertThat(guide.isPrepared()).isTrue();
    }
}
