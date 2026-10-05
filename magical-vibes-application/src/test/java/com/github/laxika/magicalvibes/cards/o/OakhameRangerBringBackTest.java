package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BringBack;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OakhameRangerBringBack.class, BringBack.class})
class OakhameRangerBringBackTest extends BaseCardTest {

    @Test
    void adventureCreatesTwoWhiteHumanTokensAndExilesTheCard() {
        OakhameRangerBringBack card = new OakhameRangerBringBack();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.HUMAN);
            assertThat(token.getCard().getPower()).isEqualTo(1);
            assertThat(token.getCard().getToughness()).isEqualTo(1);
        });
        assertThat(gd.findExiledCard(card.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(card.getId())).isEqualTo(player1.getId());
    }

    @Test
    void tapAbilityBoostsAllCreaturesYouControlUntilEndOfTurn() {
        Permanent ranger = addCreatureReady(player1, new OakhameRangerBringBack());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new OakhameRangerBringBack());
        Permanent opponentBear = harness.addToBattlefieldAndReturn(player2, new OakhameRangerBringBack());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(ranger.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ranger)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentBear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBear)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ranger)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
    }

    @Test
    void creatureCanBeCastFromExileAfterAdventureWithMixedHybridMana() {
        OakhameRangerBringBack card = new OakhameRangerBringBack();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castFromExile(player1, card.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(findPermanent(player1, "Oakhame Ranger").getCard().getId()).isEqualTo(card.getId());
    }

    @Test
    void newlyEnteredRangerCannotPayTapCost() {
        Permanent ranger = harness.addToBattlefieldAndReturn(player1, new OakhameRangerBringBack());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(ranger.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, ranger)).isEqualTo(2);
    }

    @Test
    void boostIncludesCreaturesEnteringBeforeResolutionButNotAfterward() {
        addCreatureReady(player1, new OakhameRangerBringBack());
        harness.activateAbility(player1, 0, 0, null, null);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new OakhameRangerBringBack());
        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new OakhameRangerBringBack());

        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, afterResolution)).isEqualTo(2);
    }
}
