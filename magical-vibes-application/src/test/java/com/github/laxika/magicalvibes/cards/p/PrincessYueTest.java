package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AshayaSoulOfTheWild;
import com.github.laxika.magicalvibes.cards.f.Forest;
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
        gd.playerDecks.put(player1.getId(), new java.util.ArrayList<>(List.of(new Forest(), new Forest())));

        harness.performUntapStep(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(yue.isTapped()).isTrue();
    }
}
