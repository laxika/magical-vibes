package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KathariScreecher;
import com.github.laxika.magicalvibes.cards.m.MerfolkLooter;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WanderwineFarewell.class, Forest.class, GrizzlyBears.class, MerfolkLooter.class,
        KathariScreecher.class})
class WanderwineFarewellTest extends BaseCardTest {

    @Test
    @DisplayName("Returns one or two target nonland permanents and creates one token per permanent with a Merfolk")
    void returnsTargetsAndCreatesMatchingTokens() {
        harness.addToBattlefield(player1, new MerfolkLooter());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castFarewell(List.of(bear.getId(), secondBear.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bear.getId()) || p.getId().equals(secondBear.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .hasSize(2);
    }

    @Test
    @DisplayName("Does not create tokens when the controller does not control a Merfolk")
    void noMerfolkMeansNoTokens() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castFarewell(List.of(bear.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .isEmpty();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returningLastMerfolkDoesNotCreateTokens() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new MerfolkLooter());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castFarewell(List.of(merfolk.getId(), bear.getId()));

        harness.assertInHand(player1, "Merfolk Looter");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void opponentMerfolkDoesNotEnableTokens() {
        harness.addToBattlefield(player2, new MerfolkLooter());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castFarewell(List.of(bear.getId()));

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void oneRemainingTargetCreatesOneToken() {
        harness.addToBattlefield(player1, new MerfolkLooter());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent missingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCast();
        harness.castSorcery(player1, 0, List.of(bear.getId(), missingBear.getId()));
        harness.getPermanentRemovalService().removePermanentToExile(gd, missingBear);

        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()).hasSize(1);
    }

    @Test
    void allTargetsGoneDoesNotCreateTokens() {
        harness.addToBattlefield(player1, new MerfolkLooter());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCast();
        harness.castSorcery(player1, 0, List.of(bear.getId()));
        harness.getPermanentRemovalService().removePermanentToExile(gd, bear);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Wanderwine Farewell");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken());
    }

    @Test
    void returnedTokenCountsAndNewTokenHasOracleCharacteristics() {
        harness.addToBattlefield(player1, new MerfolkLooter());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castFarewell(List.of(bear.getId()));
        Permanent originalToken = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).findFirst().orElseThrow();

        castFarewell(List.of(originalToken.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken())
                .singleElement().satisfies(p -> {
                    assertThat(p.getId()).isNotEqualTo(originalToken.getId());
                    assertThat(gqs.isCreature(gd, p)).isTrue();
                    assertThat(gqs.getEffectivePower(gd, p)).isEqualTo(1);
                    assertThat(gqs.getEffectiveToughness(gd, p)).isEqualTo(1);
                    assertThat(p.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
                    assertThat(p.getCard().getSubtypes()).contains(CardSubtype.MERFOLK);
                });
    }

    @Test
    void convokePaysBlueManaWithSummoningSickMerfolk() {
        Permanent merfolk = harness.addToBattlefieldAndReturn(player1, new MerfolkLooter());
        merfolk.setSummoningSick(true);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WanderwineFarewell()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castInstantWithConvoke(player1, 0, List.of(bear.getId()), List.of(merfolk.getId()));
        assertThat(merfolk.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()).hasSize(1);
    }

    @Test
    void unearthedPermanentExiledInsteadOfReturnedDoesNotCountForTokens() {
        harness.addToBattlefield(player1, new MerfolkLooter());
        harness.setGraveyard(player1, List.of(new KathariScreecher()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        Permanent screecher = findPermanent(player1, "Kathari Screecher");
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castFarewell(List.of(screecher.getId(), bear.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Kathari Screecher"));
        harness.assertNotInHand(player1, "Kathari Screecher");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().isToken()).hasSize(1);
    }

    @Test
    void cannotCastWithoutTargetsOrWithMoreThanTwoTargets() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareCast();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    private void castFarewell(List<UUID> targetIds) {
        prepareCast();
        harness.castAndResolveSorcery(player1, 0, targetIds);
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new WanderwineFarewell()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
