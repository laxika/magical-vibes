package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BraveTheWilds.class, DarksteelRelic.class, Forest.class, GrizzlyBears.class})
class BraveTheWildsTest extends BaseCardTest {

    @Test
    void withoutBargainSearchesForABasicLand() {
        harness.setHand(player1, List.of(new BraveTheWilds()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Forest");
    }

    @Test
    void bargainAnimatesTargetLandAndSearchesForABasicLand() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new BraveTheWilds()));
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.getGameService().playCard(gd, player1, 0, 0, land.getId(), null,
                List.of(), List.of(), false, sacrifice.getId(), null, null, null, null, true);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(land.isAnimatedUntilEndOfTurn()).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
        assertThat(land.getTransientSubtypes()).contains(CardSubtype.ELEMENTAL);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(land.getCard().hasType(CardType.LAND)).isTrue();
        harness.assertInGraveyard(player1, "Darksteel Relic");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void bargainAnimationWearsOffAtEndOfTurn() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new BraveTheWilds()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.getGameService().playCard(gd, player1, 0, 0, land.getId(), null,
                List.of(), List.of(), false, sacrifice.getId(), null, null, null, null, true);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(land.isAnimatedUntilEndOfTurn()).isFalse();
        assertThat(gqs.isCreature(gd, land)).isFalse();
    }

    @Test
    void bargainedSpellCannotTargetAnOpponentLand() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new BraveTheWilds()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, land.getId(), null,
                List.of(), List.of(), false, sacrifice.getId(), null, null, null, null, true))
                .isInstanceOf(IllegalStateException.class);
    }
}
