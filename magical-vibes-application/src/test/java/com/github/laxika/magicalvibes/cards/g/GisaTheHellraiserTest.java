package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.f.ForsakenMiner;
import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GisaTheHellraiser.class, Gravecrawler.class, GrizzlyBears.class, Shock.class, ForsakenMiner.class, AmoeboidChangeling.class})
class GisaTheHellraiserTest extends BaseCardTest {

    @Test
    @DisplayName("Skeletons and Zombies you control get +1/+1 and menace")
    void buffsSkeletonsAndZombiesYouControl() {
        Permanent zombie = harness.addToBattlefieldAndReturn(player1, new Gravecrawler());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GisaTheHellraiser());

        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, zombie, Keyword.MENACE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Creates two tapped blue-black Zombie Rogue tokens after a crime")
    void createsTwoTappedZombieRogueTokensAfterCrime() {
        harness.addToBattlefield(player1, new GisaTheHellraiser());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        castShockAtOpponent();

        resolveAllTriggers();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        for (Permanent token : tokens) {
            assertThat(token.isTapped()).isTrue();
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLUE);
            assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLUE, CardColor.BLACK);
            assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ZOMBIE, CardSubtype.ROGUE);
            assertThat(token.getCard().getPower()).isEqualTo(2);
            assertThat(token.getCard().getToughness()).isEqualTo(2);
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
            assertThat(gqs.hasKeyword(gd, token, Keyword.MENACE)).isTrue();
        }
    }

    @Test
    @DisplayName("Creates tokens only once per turn for crimes")
    void triggersOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new GisaTheHellraiser());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        castShockAtOpponent();
        resolveAllTriggers();
        castShockAtOpponent();
        resolveAllTriggers();

        assertThat(countTokens()).isEqualTo(2);
    }

    @Test
    @DisplayName("Targeting yourself does not trigger the crime ability")
    void targetingYourselfDoesNotTrigger() {
        harness.addToBattlefield(player1, new GisaTheHellraiser());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player1.getId());
        resolveAllTriggers();

        assertThat(countTokens()).isZero();
    }

    @Test
    void buffsOwnSkeletonButNotOpponentsSkeleton() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new ForsakenMiner());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new ForsakenMiner());
        harness.addToBattlefield(player1, new GisaTheHellraiser());

        assertThat(gqs.getEffectivePower(gd, own)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, own)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, own, Keyword.MENACE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opposing)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposing)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposing, Keyword.MENACE)).isFalse();
    }

    @Test
    void buffsGisaWhenSheGainsSkeletonAndZombieTypes() {
        Permanent gisa = harness.addToBattlefieldAndReturn(player1, new GisaTheHellraiser());
        Permanent changeling = harness.addToBattlefieldAndReturn(player1, new AmoeboidChangeling());
        changeling.setSummoningSick(false);

        harness.activateAbility(player1, 1, 0, null, gisa.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, gisa)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, gisa)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, gisa, Keyword.MENACE)).isTrue();
    }

    @Test
    void wardCountersOpponentsSpellWhenTheyCannotPayMana() {
        Permanent gisa = harness.addToBattlefieldAndReturn(player1, new GisaTheHellraiser());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, gisa.getId());
        resolveAllTriggers();

        assertThat(gisa.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
        harness.assertLife(player2, 20);
        assertThat(countTokens()).isZero();
    }

    @Test
    void targetingOpponentsPermanentCommitsCrime() {
        harness.addToBattlefield(player1, new GisaTheHellraiser());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new ForsakenMiner());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, opposing.getId());
        resolveAllTriggers();

        assertThat(countTokens()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Forsaken Miner");
    }
    private void castShockAtOpponent() {
        harness.castInstant(player1, 0, player2.getId());
    }

    private long countTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .count();
    }
}


