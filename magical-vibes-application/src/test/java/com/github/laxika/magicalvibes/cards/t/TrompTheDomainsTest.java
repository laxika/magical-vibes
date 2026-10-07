package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrompTheDomains.class, Forest.class, Island.class, Plains.class, Mountain.class, Swamp.class, TerramorphicExpanse.class, AshcoatBear.class, SongOfTheDryads.class})
class TrompTheDomainsTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts your creatures and grants them trample by domain count")
    void boostsOwnCreaturesByDomainCount() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        Permanent otherBear = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Plains());
        harness.setHand(player1, List.of(new TrompTheDomains()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(bear.getEffectivePower()).isEqualTo(5);
        assertThat(bear.getEffectiveToughness()).isEqualTo(5);
        assertThat(otherBear.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(opponentBear.getEffectivePower()).isEqualTo(2);
        assertThat(opponentBear.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Counts duplicate basic land types only once")
    void countsDistinctBasicLandTypes() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new TrompTheDomains()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(bear.getEffectivePower()).isEqualTo(3);
        assertThat(bear.getEffectiveToughness()).isEqualTo(3);
        assertThat(bear.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Boost and trample wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new TrompTheDomains()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        assertThat(bear.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Grants trample even when no controlled land has a basic land type")
    void grantsTrampleAtZeroDomain() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        harness.addToBattlefield(player1, new TerramorphicExpanse());
        harness.addToBattlefield(player2, new Forest());
        harness.setHand(player1, List.of(new TrompTheDomains()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(bear.getEffectivePower()).isEqualTo(2);
        assertThat(bear.getEffectiveToughness()).isEqualTo(2);
        assertThat(bear.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("All five basic land types give a five-point bonus")
    void countsAllFiveBasicLandTypes() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, new TerramorphicExpanse());
        harness.setHand(player1, List.of(new TrompTheDomains()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(bear.getEffectivePower()).isEqualTo(7);
        assertThat(bear.getEffectiveToughness()).isEqualTo(7);
        assertThat(bear.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Counts lands and affects creatures present at resolution rather than casting")
    void determinesDomainAndCreaturesAtResolution() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new TrompTheDomains(), new AshcoatBear()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, 0);

        harness.castAndResolveInstant(player1, 0);
        Permanent bear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof AshcoatBear)
                .findFirst().orElseThrow();
        harness.addToBattlefield(player1, new Island());
        harness.passBothPriorities();

        assertThat(bear.getEffectivePower()).isEqualTo(4);
        assertThat(bear.getEffectiveToughness()).isEqualTo(4);
        assertThat(bear.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Later lands and creatures do not change the resolved effect")
    void locksBonusAndAffectedCreaturesAtResolution() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new TrompTheDomains(), new AshcoatBear()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.addToBattlefield(player1, new Island());
        harness.castAndResolveInstant(player1, 0);
        Permanent laterBear = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof AshcoatBear)
                .filter(permanent -> !permanent.getId().equals(bear.getId()))
                .findFirst().orElseThrow();

        assertThat(bear.getEffectivePower()).isEqualTo(3);
        assertThat(bear.getEffectiveToughness()).isEqualTo(3);
        assertThat(bear.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(laterBear.getEffectivePower()).isEqualTo(2);
        assertThat(laterBear.getEffectiveToughness()).isEqualTo(2);
        assertThat(laterBear.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Counts a creature turned into a Forest by Song of the Dryads")
    void countsPermanentsThatBecomeLands() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        Permanent enchantedBear = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        harness.setHand(player1, List.of(new SongOfTheDryads(), new TrompTheDomains()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castEnchantment(player1, 0, enchantedBear.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, enchantedBear)).isTrue();
        assertThat(gqs.isCreature(gd, enchantedBear)).isFalse();
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(bear.getEffectivePower()).isEqualTo(3);
        assertThat(bear.getEffectiveToughness()).isEqualTo(3);
        assertThat(bear.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(enchantedBear.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }
}
