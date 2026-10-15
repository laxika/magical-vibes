package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.a.AirtightAlibi;
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

@CardUsed({UnyieldingGatekeeper.class, GrizzlyBears.class, Plains.class, AirtightAlibi.class})
class UnyieldingGatekeeperTest extends BaseCardTest {

    @Test
    void turningFaceUpExilesAndReturnsPermanentYouControlTapped() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent gatekeeper = castFaceDown();

        turnFaceUp(gatekeeper);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
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

    @Test
    void controlledStolenPermanentReturnsUnderAbilityControllersControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new UnyieldingGatekeeper());
        gd.stolenCreatures.put(target.getId(), player2.getId());
        Permanent gatekeeper = castFaceDown();

        turnFaceUp(gatekeeper);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanents(player1, "Unyielding Gatekeeper").stream()
                .filter(permanent -> !permanent.getId().equals(gatekeeper.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.getId()).isNotEqualTo(target.getId());
        assertThat(returned.isTapped()).isTrue();
        assertThat(gd.stolenCreatures.get(returned.getId())).isEqualTo(player2.getId());
        harness.assertNotOnBattlefield(player2, "Unyielding Gatekeeper");
        assertThat(countPermanents(player1, "Detective")).isZero();
        assertThat(countPermanents(player2, "Detective")).isZero();
    }

    @Test
    void returningAuraAttachesToALegalCreature() {
        Permanent gatekeeper = castFaceDown();
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new AirtightAlibi());
        aura.setAttachedTo(gatekeeper.getId());

        turnFaceUp(gatekeeper);
        harness.handlePermanentChosen(player1, aura.getId());
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.PermanentChoice) {
            harness.handlePermanentChosen(player1, gatekeeper.getId());
            harness.passBothPriorities();
        }

        Permanent returned = findPermanent(player1, "Airtight Alibi");
        assertThat(returned.getId()).isNotEqualTo(aura.getId());
        assertThat(returned.getAttachedTo()).isEqualTo(gatekeeper.getId());
        assertThat(returned.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Airtight Alibi");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    void targetLeavingBeforeResolutionDoesNotCreateDetective() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UnyieldingGatekeeper());
        Permanent gatekeeper = castFaceDown();

        turnFaceUp(gatekeeper);
        harness.handlePermanentChosen(player1, target.getId());
        harness.getPermanentRemovalService().removePermanentToExile(gd, target);
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Detective")).isZero();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target.getCard());
    }

    @Test
    void castingFaceUpDoesNotExileAnotherPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UnyieldingGatekeeper());
        harness.setHand(player1, List.of(new UnyieldingGatekeeper()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player2, "Unyielding Gatekeeper")).isSameAs(target);
        assertThat(countPermanents(player2, "Detective")).isZero();
    }

    private Permanent castFaceDown() {
        harness.setHand(player1, List.of(new UnyieldingGatekeeper()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        return findPermanents(player1, "Unyielding Gatekeeper").stream()
                .filter(Permanent::isFaceDown).findFirst().orElseThrow();
    }

    private void turnFaceUp(Permanent gatekeeper) {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(gatekeeper));
    }
}
