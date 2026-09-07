package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ForumFamiliar.class, GrizzlyBears.class, Plains.class})
class ForumFamiliarTest extends BaseCardTest {

    @Test
    void turningFaceUpReturnsAnotherPermanentYouControlAndPutsCounterOnIt() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent opposingPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ForumFamiliar()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent familiar = findPermanent(player1, "Forum Familiar");
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(familiar));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(ownPermanent.getId())
                .doesNotContain(familiar.getId(), opposingPermanent.getId());
        harness.handlePermanentChosen(player1, ownPermanent.getId());
        harness.passBothPriorities();

        assertThat(familiar.isFaceDown()).isFalse();
        assertThat(familiar.getEffectivePower()).isEqualTo(2);
        assertThat(familiar.getEffectiveToughness()).isEqualTo(2);
        harness.assertInHand(player1, "Plains");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    void turningFaceUpWithoutAnotherPermanentDoesNotCreateAResolution() {
        harness.setHand(player1, List.of(new ForumFamiliar()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent familiar = findPermanent(player1, "Forum Familiar");
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(familiar));

        assertThat(familiar.isFaceDown()).isFalse();
        assertThat(familiar.getEffectivePower()).isEqualTo(1);
        assertThat(familiar.getEffectiveToughness()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
