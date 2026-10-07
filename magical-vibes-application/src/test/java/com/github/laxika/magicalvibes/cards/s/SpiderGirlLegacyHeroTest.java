package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.WitnessProtection;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({SpiderGirlLegacyHero.class, WitnessProtection.class})
class SpiderGirlLegacyHeroTest extends BaseCardTest {

    @Test
    @DisplayName("Has flying during its controller's turn only")
    void flyingOnlyDuringControllerTurn() {
        Permanent spiderGirl = harness.addToBattlefieldAndReturn(player1, new SpiderGirlLegacyHero());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, spiderGirl, Keyword.FLYING)).isTrue();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, spiderGirl, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Creates a 1/1 green and white Human Citizen token when it leaves the battlefield")
    void leavingCreatesHumanCitizenToken() {
        Permanent spiderGirl = harness.addToBattlefieldAndReturn(player1, new SpiderGirlLegacyHero());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, spiderGirl));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.HUMAN))
                .findFirst().orElseThrow();

        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.CITIZEN);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    void returningToHandCreatesExactlyOneTokenAfterTriggerResolves() {
        Permanent spiderGirl = harness.addToBattlefieldAndReturn(player1, new SpiderGirlLegacyHero());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, spiderGirl));

        harness.assertInHand(player1, "Spider-Girl, Legacy Hero");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.CITIZEN);
            assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
            assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
        });
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void exileCreatesTokenForControllerRatherThanOwnerDuringOpponentsTurn() {
        SpiderGirlLegacyHero card = new SpiderGirlLegacyHero();
        card.setOwnerId(player1.getId());
        Permanent spiderGirl = harness.addToBattlefieldAndReturn(player2, card);
        gd.stolenCreatures.put(spiderGirl.getId(), player1.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, spiderGirl));
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).singleElement().satisfies(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.HUMAN, CardSubtype.CITIZEN);
        });
    }

    @Test
    void flyingUsesCurrentControllerRatherThanOwner() {
        SpiderGirlLegacyHero card = new SpiderGirlLegacyHero();
        card.setOwnerId(player1.getId());
        Permanent spiderGirl = harness.addToBattlefieldAndReturn(player2, card);
        gd.stolenCreatures.put(spiderGirl.getId(), player1.getId());

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, spiderGirl, Keyword.FLYING)).isFalse();

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, spiderGirl, Keyword.FLYING)).isTrue();
    }

    @Test
    void losingAbilitiesBeforeLeavingDoesNotCreateToken() {
        Permanent spiderGirl = harness.addToBattlefieldAndReturn(player1, new SpiderGirlLegacyHero());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WitnessProtection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, spiderGirl.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasLostPrintedAbilities(gd, spiderGirl)).isTrue();
        assertThat(gqs.hasKeyword(gd, spiderGirl, Keyword.FLYING)).isFalse();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, spiderGirl));

        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Spider-Girl, Legacy Hero");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }
}
