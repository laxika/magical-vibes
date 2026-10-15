package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.ArtificialEvolution;
import com.github.laxika.magicalvibes.cards.b.BearCub;
import com.github.laxika.magicalvibes.cards.b.Bitterblossom;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RangersMerit;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UrsineGuideRangersMerit.class, RangersMerit.class, BearCub.class, GrizzlyBears.class, ArtificialEvolution.class, Bitterblossom.class})
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

        harness.assertNotOnBattlefield(player1, "Bear Cub");
        assertThat(guide.isPrepared()).isTrue();
    }

    @Test
    void entersPreparedWithoutWaitingForATrigger() {
        Permanent guide = harness.enterBattlefieldAndReturn(player1, new UrsineGuideRangersMerit());

        assertThat(guide.isPrepared()).isTrue();
        assertThat(guide.getPreparedSpellCardId()).isNotNull();
        assertThat(gd.findExiledCard(guide.getPreparedSpellCardId()).card())
                .isInstanceOf(RangersMerit.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void outsideStartingDeckSpellConjuresBeforeItResolvesWithoutDuplicatingPreparation() {
        Permanent guide = harness.enterBattlefieldAndReturn(player1, new UrsineGuideRangersMerit());
        UUID originalPreparedSpell = guide.getPreparedSpellCardId();
        BearCub outsideDeckBear = new BearCub();
        outsideDeckBear.setOwnerId(player1.getId());

        harness.forceActivePlayer(player1);
        harness.castFromHand(player1, outsideDeckBear, "{1}{G}");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Bear Cub")).isEqualTo(1);
        Permanent conjuredBear = findPermanent(player1, "Bear Cub");
        assertThat(conjuredBear.getCard().getId()).isNotEqualTo(outsideDeckBear.getId());
        assertThat(conjuredBear.getCard().getOwnerId()).isEqualTo(player1.getId());
        assertThat(guide.getPreparedSpellCardId()).isEqualTo(originalPreparedSpell);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        assertThat(countPermanents(player1, "Bear Cub")).isEqualTo(2);
    }

    @Test
    void opponentsOutsideStartingDeckSpellDoesNotTrigger() {
        Permanent guide = harness.enterBattlefieldAndReturn(player1, new UrsineGuideRangersMerit());
        UUID originalPreparedSpell = guide.getPreparedSpellCardId();
        BearCub outsideDeckBear = new BearCub();
        outsideDeckBear.setOwnerId(player2.getId());

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, outsideDeckBear, "{1}{G}");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Bear Cub");
        assertThat(countPermanents(player2, "Bear Cub")).isEqualTo(1);
        assertThat(guide.getPreparedSpellCardId()).isEqualTo(originalPreparedSpell);
    }

    @Test
    void meritAffectsOnlyCurrentControlledBearsAndTrampleExpiresButCountersRemain() {
        Permanent guide = harness.enterBattlefieldAndReturn(player1, new UrsineGuideRangersMerit());
        Permanent opposingBear = harness.enterBattlefieldAndReturn(player2, new BearCub());
        UUID preparedSpell = guide.getPreparedSpellCardId();

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromExile(player1, preparedSpell);
        resolveAllTriggers();

        Permanent conjuredBear = findPermanent(player1, "Bear Cub");
        assertThat(conjuredBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, conjuredBear, Keyword.TRAMPLE)).isTrue();
        assertThat(opposingBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, opposingBear, Keyword.TRAMPLE)).isFalse();
        Permanent lateBear = harness.enterBattlefieldAndReturn(player1, new BearCub());
        assertThat(lateBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, lateBear, Keyword.TRAMPLE)).isFalse();

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, guide, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, conjuredBear, Keyword.TRAMPLE)).isFalse();
        assertThat(guide.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(conjuredBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void meritAlsoPlacesCountersOnNoncreatureBears() {
        Permanent blossom = makeNoncreatureBear();
        castPreparedMerit();

        assertThat(blossom.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void meritAlsoGrantsTrampleToNoncreatureBears() {
        Permanent blossom = makeNoncreatureBear();
        castPreparedMerit();

        assertThat(gqs.hasKeyword(gd, blossom, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void castingPreparedMeritUnpreparesThenRepreparesBeforeTheSorceryResolves() {
        Permanent guide = harness.enterBattlefieldAndReturn(player1, new UrsineGuideRangersMerit());
        UUID originalPreparedSpell = guide.getPreparedSpellCardId();
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromExile(player1, originalPreparedSpell);

        assertThat(guide.isPrepared()).isFalse();
        assertThat(guide.getPreparedSpellCardId()).isNull();
        harness.passBothPriorities();

        Permanent conjuredBear = findPermanent(player1, "Bear Cub");
        assertThat(conjuredBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(guide.isPrepared()).isTrue();
        assertThat(guide.getPreparedSpellCardId()).isNotNull().isNotEqualTo(originalPreparedSpell);
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(conjuredBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void meritDoesNotAffectABearChangedToAnotherCreatureType() {
        Permanent formerBear = harness.enterBattlefieldAndReturn(player1, new BearCub());
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, formerBear.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BEAR");
        harness.handleListChoice(player1, "FAERIE");
        resolveAllTriggers();
        castPreparedMerit();

        assertThat(formerBear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, formerBear, Keyword.TRAMPLE)).isFalse();
    }
    private Permanent makeNoncreatureBear() {
        Permanent blossom = harness.enterBattlefieldAndReturn(player1, new Bitterblossom());
        harness.setHand(player1, List.of(new ArtificialEvolution()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, blossom.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "FAERIE");
        harness.handleListChoice(player1, "BEAR");
        resolveAllTriggers();
        assertThat(gqs.hasEffectiveSubtype(gd, blossom, CardSubtype.BEAR)).isTrue();
        assertThat(gqs.isCreature(gd, blossom)).isFalse();
        return blossom;
    }

    private void castPreparedMerit() {
        Permanent guide = harness.enterBattlefieldAndReturn(player1, new UrsineGuideRangersMerit());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromExile(player1, guide.getPreparedSpellCardId());
        resolveAllTriggers();
    }
}
