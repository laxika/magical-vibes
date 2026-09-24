package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.q.QuickSliver;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrimalWhisperer.class, FugitiveWizard.class, QuickSliver.class})
class PrimalWhispererTest extends BaseCardTest {

    @Test
    void getsTwoPlusTwoForEachFaceDownCreatureOnTheBattlefield() {
        Permanent whisperer = harness.addToBattlefieldAndReturn(player1, new PrimalWhisperer());
        Permanent ownFaceDownCreature = addFaceDownCreature(player1);
        Permanent opposingFaceDownCreature = addFaceDownCreature(player2);

        assertThat(gqs.getEffectivePower(gd, whisperer)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, whisperer)).isEqualTo(6);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, ownFaceDownCreature));

        assertThat(gqs.getEffectivePower(gd, whisperer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, whisperer)).isEqualTo(4);

        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, opposingFaceDownCreature));

        assertThat(gqs.getEffectivePower(gd, whisperer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, whisperer)).isEqualTo(2);
    }

    @Test
    void doesNotCountFaceDownNoncreatures() {
        Permanent whisperer = harness.addToBattlefieldAndReturn(player1, new PrimalWhisperer());
        Permanent faceDownNoncreature = addCreatureReady(player2, new QuickSliver());
        faceDownNoncreature.setFaceDown(2, 2, Set.of(CardType.ARTIFACT));

        assertThat(gqs.getEffectivePower(gd, whisperer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, whisperer)).isEqualTo(2);
    }

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForItsMorphCost() {
        harness.setHand(player1, List.of(new PrimalWhisperer()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent whisperer = findPermanent(player1, "Primal Whisperer");
        assertThat(whisperer.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(whisperer));
        harness.passBothPriorities();

        assertThat(whisperer.isFaceDown()).isFalse();
    }

    private Permanent addFaceDownCreature(Player player) {
        Permanent permanent = addCreatureReady(player, new FugitiveWizard());
        permanent.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        return permanent;
    }
}
