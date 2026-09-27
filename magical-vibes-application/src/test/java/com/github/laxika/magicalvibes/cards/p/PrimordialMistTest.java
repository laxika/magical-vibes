package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.z.ZoeticCavern;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrimordialMist.class, GrizzlyBears.class, ZoeticCavern.class})
class PrimordialMistTest extends BaseCardTest {

    @Test
    void mayManifestTopCardAtEndStep() {
        harness.addToBattlefieldAndReturn(player1, new PrimordialMist());
        GrizzlyBears topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.isManifested())
                .findFirst()
                .orElseThrow();
        assertThat(manifested.getCard().getId()).isEqualTo(topCard.getId());
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void exilesFaceDownPermanentAndAllowsPlayingThatCard() {
        harness.addToBattlefieldAndReturn(player1, new PrimordialMist());
        ZoeticCavern cavernCard = new ZoeticCavern();
        harness.setHand(player1, List.of(cavernCard));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent faceDownCavern = findPermanent(player1, "Zoetic Cavern");
        assertThat(faceDownCavern.isFaceDown()).isTrue();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.findExiledCard(cavernCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).containsEntry(cavernCard.getId(), player1.getId());

        harness.castFromExile(player1, cavernCard.getId());

        assertThat(gd.findExiledCard(cavernCard.getId())).isNull();
        Permanent playedCavern = findPermanent(player1, "Zoetic Cavern");
        assertThat(playedCavern.isFaceDown()).isFalse();
        assertThat(gqs.isLand(gd, playedCavern)).isTrue();
    }
}
