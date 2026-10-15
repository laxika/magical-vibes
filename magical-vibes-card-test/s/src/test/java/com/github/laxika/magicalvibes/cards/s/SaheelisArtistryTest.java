package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CowlProwler;
import com.github.laxika.magicalvibes.cards.d.DecoctionModule;
import com.github.laxika.magicalvibes.cards.d.DukharaPeafowl;
import com.github.laxika.magicalvibes.cards.p.PerpetualTimepiece;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({SaheelisArtistry.class, CowlProwler.class, PerpetualTimepiece.class,
        DukharaPeafowl.class, DecoctionModule.class})
class SaheelisArtistryTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a token copy of a target artifact")
    void createsTokenCopyOfArtifact() {
        Permanent timepiece = harness.addToBattlefieldAndReturn(player2, new PerpetualTimepiece());
        prepareCard();

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0},
                List.of(timepiece.getId()), null);
        harness.passBothPriorities();

        assertThat(tokenCopies(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Creates an artifact token copy of a target creature")
    void createsArtifactTokenCopyOfCreature() {
        Permanent prowler = harness.addToBattlefieldAndReturn(player2, new CowlProwler());
        prepareCard();

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{1},
                List.of(prowler.getId()), null);
        harness.passBothPriorities();

        assertThat(tokenCopies(player1)).singleElement().satisfies(token -> {
            assertThat(token.getCard().getAdditionalTypes()).contains(CardType.ARTIFACT);
        });
    }

    @Test
    @DisplayName("Can choose both modes and target the same permanent")
    void choosesBothModesWithSharedTarget() {
        Permanent peafowl = harness.addToBattlefieldAndReturn(player2, new DukharaPeafowl());
        prepareCard();

        UUID targetId = peafowl.getId();
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(targetId, targetId), null);
        harness.passBothPriorities();

        assertThat(tokenCopies(player1)).hasSize(2);
    }

    @Test
    @DisplayName("Rejects a target that does not match the chosen mode")
    void rejectsInvalidModeTarget() {
        Permanent prowler = harness.addToBattlefieldAndReturn(player2, new CowlProwler());
        prepareCard();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0},
                List.of(prowler.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothModesCopyTheirOwnDistinctTargets() {
        Permanent timepiece = harness.addToBattlefieldAndReturn(player2, new PerpetualTimepiece());
        Permanent prowler = harness.addToBattlefieldAndReturn(player2, new CowlProwler());
        prepareCard();

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(timepiece.getId(), prowler.getId()), null);
        harness.passBothPriorities();

        assertThat(tokenCopies(player1)).hasSize(2);
        harness.assertOnBattlefield(player1, "Perpetual Timepiece");
        harness.assertOnBattlefield(player1, "Cowl Prowler");
        assertThat(tokenCopies(player1)).allSatisfy(token ->
                assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue());
        assertThat(tokenCopies(player2)).isEmpty();
    }

    @Test
    void creatureModeStillResolvesWhenArtifactTargetLeaves() {
        Permanent timepiece = harness.addToBattlefieldAndReturn(player2, new PerpetualTimepiece());
        Permanent prowler = harness.addToBattlefieldAndReturn(player2, new CowlProwler());
        prepareCard();

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(timepiece.getId(), prowler.getId()), null);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, timepiece);
        harness.passBothPriorities();

        assertThat(tokenCopies(player1)).hasSize(1);
        harness.assertOnBattlefield(player1, "Cowl Prowler");
        harness.assertNotOnBattlefield(player1, "Perpetual Timepiece");
    }

    @Test
    void artifactModeStillResolvesWhenCreatureTargetLeaves() {
        Permanent timepiece = harness.addToBattlefieldAndReturn(player2, new PerpetualTimepiece());
        Permanent prowler = harness.addToBattlefieldAndReturn(player2, new CowlProwler());
        prepareCard();

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(timepiece.getId(), prowler.getId()), null);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, prowler);
        harness.passBothPriorities();

        assertThat(tokenCopies(player1)).hasSize(1);
        harness.assertOnBattlefield(player1, "Perpetual Timepiece");
        harness.assertNotOnBattlefield(player1, "Cowl Prowler");
    }

    @Test
    void noTokensAreCreatedWhenSharedTargetLeaves() {
        Permanent peafowl = harness.addToBattlefieldAndReturn(player2, new DukharaPeafowl());
        prepareCard();

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(peafowl.getId(), peafowl.getId()), null);
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, peafowl);
        harness.passBothPriorities();

        assertThat(tokenCopies(player1)).isEmpty();
        harness.assertInGraveyard(player1, "Saheeli's Artistry");
    }

    @Test
    void copyDoesNotInheritCountersOrTappedStatus() {
        Permanent prowler = harness.addToBattlefieldAndReturn(player2, new CowlProwler());
        prowler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        prowler.tap();
        prepareCard();

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{1},
                List.of(prowler.getId()), null);
        harness.passBothPriorities();

        assertThat(tokenCopies(player1)).singleElement().satisfies(token -> {
            assertThat(token.getPlusOnePlusOneCounters()).isZero();
            assertThat(token.isTapped()).isFalse();
            assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
        });
    }

    @Test
    void copiedCreatureRetainsItsActivatedAbility() {
        Permanent peafowl = harness.addToBattlefieldAndReturn(player2, new DukharaPeafowl());
        prepareCard();

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{1},
                List.of(peafowl.getId()), null);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(tokenCopies(player1)).singleElement().satisfies(token ->
                assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue());
        assertThat(gqs.hasKeyword(gd, peafowl, Keyword.FLYING)).isFalse();
    }

    @Test
    void rejectsNoncreatureArtifactForCreatureMode() {
        Permanent timepiece = harness.addToBattlefieldAndReturn(player2, new PerpetualTimepiece());
        prepareCard();

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{1},
                List.of(timepiece.getId()), null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void artifactCopyOfCreatureTokenKeepsTheArtifactCopyException() {
        Permanent prowler = harness.addToBattlefieldAndReturn(player2, new CowlProwler());
        prepareCard();
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{1},
                List.of(prowler.getId()), null);
        harness.passBothPriorities();
        Permanent firstCopy = tokenCopies(player1).getFirst();

        prepareCard();
        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0},
                List.of(firstCopy.getId()), null);
        harness.passBothPriorities();

        assertThat(tokenCopies(player1)).hasSize(2).allSatisfy(token -> {
            assertThat(token.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        });
        assertThat(prowler.getCard().hasType(CardType.ARTIFACT)).isFalse();
    }

    @Test
    void firstModesCopiedArtifactSeesSecondModesCreatureEnter() {
        Permanent module = harness.addToBattlefieldAndReturn(player2, new DecoctionModule());
        Permanent prowler = harness.addToBattlefieldAndReturn(player2, new CowlProwler());
        prepareCard();

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1},
                List.of(module.getId(), prowler.getId()), null);
        harness.passBothPriorities();

        assertThat(tokenCopies(player1)).hasSize(2);
        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerEnergyCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
    }

    private void prepareCard() {
        harness.setHand(player1, List.of(new SaheelisArtistry()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    private List<Permanent> tokenCopies(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
