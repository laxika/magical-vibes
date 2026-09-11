package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NantukoVigilante.class, FountainOfYouth.class, GloriousAnthem.class, GrizzlyBears.class})
class NantukoVigilanteTest extends BaseCardTest {

    @Test
    void turningFaceUpCanDestroyAnArtifactOrEnchantment() {
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent opponentEnchantment = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent vigilante = castFaceDown();

        turnFaceUp(vigilante);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactlyInAnyOrder(opponentArtifact.getId(), opponentEnchantment.getId(), ownArtifact.getId());
        harness.handlePermanentChosen(player1, ownArtifact.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Fountain of Youth");
        harness.assertOnBattlefield(player2, "Glorious Anthem");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Fountain of Youth");
    }

    @Test
    void turningFaceUpHasNoTargetWhenNoArtifactsOrEnchantmentsExist() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent vigilante = castFaceDown();

        turnFaceUp(vigilante);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(vigilante.isFaceDown()).isFalse();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new NantukoVigilante()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Nantuko Vigilante");
    }

    private void turnFaceUp(Permanent vigilante) {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vigilante));
    }
}
