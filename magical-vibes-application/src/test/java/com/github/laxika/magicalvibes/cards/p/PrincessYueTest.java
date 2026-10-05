package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AshayaSoulOfTheWild;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrincessYue.class, AshayaSoulOfTheWild.class, Forest.class})
class PrincessYueTest extends BaseCardTest {

    @Test
    void returnsTappedAsNamedLandAndGainsColorlessManaAbility() {
        Permanent yue = harness.addToBattlefieldAndReturn(player1, new PrincessYue());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, yue));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Princess Yue");
        assertThat(gqs.getEffectiveName(gd, returned)).isEqualTo("Moon");
        assertThat(gqs.getEffectiveCardTypes(gd, returned)).containsExactly(CardType.LAND);
        assertThat(returned.isTapped()).isTrue();

        harness.performUntapStep(player1);
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isOne();
    }

    @Test
    void doesNotReturnWhenItDiesAsALandCreature() {
        harness.addToBattlefield(player1, new AshayaSoulOfTheWild());
        Permanent yue = harness.addToBattlefieldAndReturn(player1, new PrincessYue());
        assertThat(gqs.isLand(gd, yue)).isTrue();
        assertThat(gqs.isCreature(gd, yue)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, yue));

        harness.assertInGraveyard(player1, "Princess Yue");
        harness.assertNotOnBattlefield(player1, "Princess Yue");
    }

    @Test
    void canScryTwo() {
        Permanent yue = harness.addToBattlefieldAndReturn(player1, new PrincessYue());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.performUntapStep(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(yue.isTapped()).isTrue();
    }

    @Test
    void returnedMoonRetainsScryAbility() {
        Permanent yue = harness.addToBattlefieldAndReturn(player1, new PrincessYue());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, yue));
        harness.passBothPriorities();
        harness.performUntapStep(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(findPermanent(player1, "Princess Yue").isTapped()).isTrue();
    }

    @Test
    void returnedMoonDoesNotReturnAgainWhenPutIntoGraveyard() {
        Permanent yue = harness.addToBattlefieldAndReturn(player1, new PrincessYue());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, yue));
        harness.passBothPriorities();

        Permanent moon = findPermanent(player1, "Princess Yue");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, moon));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Princess Yue");
        harness.assertNotOnBattlefield(player1, "Princess Yue");
    }

    @Test
    void doesNotReturnIfCardLeavesGraveyardBeforeTriggerResolves() {
        Permanent yue = harness.addToBattlefieldAndReturn(player1, new PrincessYue());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, yue));
        gd.playerGraveyards.get(player1.getId()).remove(yue.getCard());
        gd.playerHands.get(player1.getId()).add(yue.getCard());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Princess Yue");
        assertThat(gd.playerHands.get(player1.getId())).contains(yue.getCard());
    }

    @Test
    void returnedMoonLosesCreatureSubtypes() {
        Permanent yue = harness.addToBattlefieldAndReturn(player1, new PrincessYue());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, yue));
        harness.passBothPriorities();

        Permanent moon = findPermanent(player1, "Princess Yue");
        assertThat(gqs.hasEffectiveSubtype(gd, moon, CardSubtype.HUMAN)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, moon, CardSubtype.NOBLE)).isFalse();
        assertThat(gqs.hasEffectiveSubtype(gd, moon, CardSubtype.ALLY)).isFalse();
    }
}
