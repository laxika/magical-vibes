package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkirkVolcanist.class, Mountain.class, Forest.class, GrizzlyBears.class})
class SkirkVolcanistTest extends BaseCardTest {

    @Test
    void turningFaceUpSacrificesTwoMountainsAndDividesDamageAmongThreeCreatures() {
        Permanent mountain1 = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent mountain2 = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent firstTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent thirdTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent volcanist = castFaceDown();

        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(volcanist),
                List.of(mountain1.getId(), mountain2.getId()));

        assertThat(volcanist.isFaceDown()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(mountain1, mountain2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(mountain1.getCard(), mountain2.getCard());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(firstTarget.getId(), secondTarget.getId(), thirdTarget.getId());
        harness.handlePermanentChosen(player1, firstTarget.getId());
        harness.handlePermanentChosen(player1, secondTarget.getId());
        harness.handlePermanentChosen(player1, thirdTarget.getId());
        harness.passBothPriorities();

        harness.handleXValueChosen(player1, 1);
        harness.handleXValueChosen(player1, 1);
        harness.handleXValueChosen(player1, 1);

        assertThat(firstTarget.getMarkedDamage()).isEqualTo(1);
        assertThat(secondTarget.getMarkedDamage()).isEqualTo(1);
        assertThat(thirdTarget.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void cannotTurnFaceUpBySacrificingANonMountain() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent volcanist = castFaceDown();

        assertThatThrownBy(() -> harness.turnFaceUp(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(volcanist),
                List.of(mountain.getId(), forest.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(volcanist.isFaceDown()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(mountain, forest, volcanist);
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new SkirkVolcanist()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        return findPermanent(player1, "Skirk Volcanist");
    }
}
