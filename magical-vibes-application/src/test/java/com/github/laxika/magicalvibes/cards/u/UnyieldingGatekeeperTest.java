package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnyieldingGatekeeper.class, GrizzlyBears.class, Plains.class})
class UnyieldingGatekeeperTest extends BaseCardTest {

    @Test
    void turningFaceUpExilesAndReturnsPermanentYouControlTapped() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent gatekeeper = castFaceDown();

        turnFaceUp(gatekeeper);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        Permanent target = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(findPermanent(player1, "Unyielding Gatekeeper").isFaceDown()).isFalse();
    }

    @Test
    void turningFaceUpExilesOpponentPermanentAndItsControllerCreatesDetective() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent gatekeeper = castFaceDown();

        turnFaceUp(gatekeeper);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        Permanent detective = findPermanent(player2, "Detective");
        assertThat(detective.getEffectivePower()).isEqualTo(2);
        assertThat(detective.getEffectiveToughness()).isEqualTo(2);
        assertThat(detective.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
    }

    @Test
    void turningFaceUpCannotTargetALand() {
        harness.addToBattlefield(player1, new Plains());
        Permanent gatekeeper = castFaceDown();

        turnFaceUp(gatekeeper);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Unyielding Gatekeeper").isFaceDown()).isFalse();
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new UnyieldingGatekeeper()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanent(player1, "Unyielding Gatekeeper");
    }

    private void turnFaceUp(Permanent gatekeeper) {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(gatekeeper));
    }
}
