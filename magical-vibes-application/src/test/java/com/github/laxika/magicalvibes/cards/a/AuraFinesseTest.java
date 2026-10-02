package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.r.RealityAcid;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AuraFinesse.class, GrizzlyBears.class, Island.class, Pacifism.class, RealityAcid.class})
class AuraFinesseTest extends BaseCardTest {

    @Test
    @DisplayName("Attaches a controlled Aura to the target creature and draws a card")
    void attachesAuraAndDrawsCard() {
        Permanent originalHost = addCreature(player1);
        Permanent destination = addCreature(player2);
        Permanent aura = addAuraAttachedTo(player1, originalHost);
        Card drawnCard = new Island();

        harness.setHand(player1, List.of(new AuraFinesse()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, List.of(aura.getId(), destination.getId()));
        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(destination.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Cannot target an Aura controlled by an opponent")
    void cannotTargetOpponentsAura() {
        Permanent host = addCreature(player2);
        Permanent opponentAura = addAuraAttachedTo(player2, host);
        Permanent destination = addCreature(player1);
        addAuraAttachedTo(player1, destination);

        harness.setHand(player1, List.of(new AuraFinesse()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(opponentAura.getId(), destination.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Aura you control");
    }

    @Test
    void movesEnchantPermanentAuraFromLandToCreature() {
        Permanent originalHost = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent destination = addCreature(player2);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new RealityAcid());
        aura.setAttachedTo(originalHost.getId());
        Card drawnCard = new Island();

        harness.setHand(player1, List.of(new AuraFinesse()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, List.of(aura.getId(), destination.getId()));
        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(destination.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(aura);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void targetingCurrentHostStillDrawsCard() {
        Permanent host = addCreature(player1);
        Permanent aura = addAuraAttachedTo(player1, host);
        Card drawnCard = new Island();

        harness.setHand(player1, List.of(new AuraFinesse()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, List.of(aura.getId(), host.getId()));
        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void missingDestinationPreventsMovementButStillDrawsCard() {
        Permanent host = addCreature(player1);
        Permanent destination = addCreature(player2);
        Permanent aura = addAuraAttachedTo(player1, host);
        Card drawnCard = new Island();

        harness.setHand(player1, List.of(new AuraFinesse()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, List.of(aura.getId(), destination.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(destination);
        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void missingAuraStillDrawsCard() {
        Permanent host = addCreature(player1);
        Permanent destination = addCreature(player2);
        Permanent aura = addAuraAttachedTo(player1, host);
        Card drawnCard = new Island();

        harness.setHand(player1, List.of(new AuraFinesse()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, List.of(aura.getId(), destination.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    void bothTargetsMissingPreventsCardDraw() {
        Permanent host = addCreature(player1);
        Permanent destination = addCreature(player2);
        Permanent aura = addAuraAttachedTo(player1, host);
        Card undrawnCard = new Island();

        harness.setHand(player1, List.of(new AuraFinesse()));
        harness.setLibrary(player1, List.of(undrawnCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, List.of(aura.getId(), destination.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerBattlefields.get(player2.getId()).remove(destination);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawnCard);
    }

    @Test
    void auraChangingControllerPreventsMovementButStillDrawsCard() {
        Permanent host = addCreature(player1);
        Permanent destination = addCreature(player2);
        Permanent aura = addAuraAttachedTo(player1, host);
        Card drawnCard = new Island();

        harness.setHand(player1, List.of(new AuraFinesse()));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, List.of(aura.getId(), destination.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerBattlefields.get(player2.getId()).add(aura);
        harness.passBothPriorities();

        assertThat(aura.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    private Permanent addCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }

    private Permanent addAuraAttachedTo(Player player, Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(player, new Pacifism());
        aura.setAttachedTo(host.getId());
        return aura;
    }
}
