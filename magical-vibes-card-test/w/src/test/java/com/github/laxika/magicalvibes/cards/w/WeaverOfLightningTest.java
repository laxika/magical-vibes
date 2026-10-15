package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeaverOfLightning.class, GrizzlyBears.class, HillGiant.class, MindRot.class, Shock.class})
class WeaverOfLightningTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant deals 1 damage to a target creature an opponent controls")
    void instantCastDealsDamageToOpponentCreature() {
        harness.addToBattlefield(player1, new WeaverOfLightning());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        UUID ownCreatureId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactly(victim.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(ownCreatureId);

        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a sorcery deals 1 damage to a target creature an opponent controls")
    void sorceryCastDealsDamageToOpponentCreature() {
        harness.addToBattlefield(player1, new WeaverOfLightning());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HillGiant());

        harness.setHand(player1, List.of(new MindRot()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, player2.getId());

        harness.handlePermanentChosen(player1, victim.getId());
        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature does not trigger Weaver of Lightning")
    void creatureCastDoesNotTrigger() {
        harness.addToBattlefield(player1, new WeaverOfLightning());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent casting an instant does not trigger Weaver of Lightning")
    void opponentInstantDoesNotTrigger() {
        harness.addToBattlefield(player1, new WeaverOfLightning());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(victim.getMarkedDamage()).isZero();
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Without an opposing creature the trigger does not prevent the spell resolving")
    void noLegalTargetDoesNotBlockSpell() {
        Permanent weaver = harness.addToBattlefieldAndReturn(player1, new WeaverOfLightning());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(weaver.getMarkedDamage()).isZero();
        assertThat(ownCreature.getMarkedDamage()).isZero();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The triggered ability still deals damage after Weaver leaves the battlefield")
    void triggerResolvesAfterSourceDies() {
        Permanent weaver = harness.addToBattlefieldAndReturn(player1, new WeaverOfLightning());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.handlePermanentChosen(player1, victim.getId());

        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, weaver.getId());
        harness.castAndResolveInstant(player2, 0, weaver.getId());
        harness.assertInGraveyard(player1, "Weaver of Lightning");
        harness.assertNotOnBattlefield(player1, "Weaver of Lightning");

        harness.passBothPriorities();

        assertThat(victim.getMarkedDamage()).isEqualTo(1);
    }
}
