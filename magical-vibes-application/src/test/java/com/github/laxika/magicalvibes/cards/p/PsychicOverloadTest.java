package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AuraGraft;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PsychicOverload.class, AuraGraft.class, FountainOfYouth.class, GrizzlyBears.class, LeoninScimitar.class})
class PsychicOverloadTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Psychic Overload taps the enchanted permanent")
    void resolvingTapsEnchantedPermanent() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        harness.setHand(player1, List.of(new PsychicOverload()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, fountain.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(fountain.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted permanent does not untap during its controller's untap step")
    void enchantedPermanentDoesNotUntap() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.tap();

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PsychicOverload());
        aura.setAttachedTo(bears.getId());

        advanceToUpkeep(player2);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted permanent's controller can discard two artifact cards to untap it")
    void discardingTwoArtifactsUntapsEnchantedPermanent() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.tap();

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PsychicOverload());
        aura.setAttachedTo(bears.getId());

        harness.setHand(player2, List.of(new LeoninScimitar(), new LeoninScimitar()));

        harness.activateAbility(player2, 0, null, null);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        assertThat(bears.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Leonin Scimitar");
    }

    @Test
    @DisplayName("The granted ability cannot be activated without two artifact cards")
    void cannotActivateWithoutTwoArtifacts() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PsychicOverload());
        aura.setAttachedTo(bears.getId());

        harness.setHand(player1, List.of(new LeoninScimitar()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The granted ability cannot be activated by discarding nonartifact cards")
    void cannotActivateWithNonartifactCards() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.tap();

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PsychicOverload());
        aura.setAttachedTo(bears.getId());

        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The enter trigger taps the current enchanted permanent after Aura Graft moves the Aura")
    void enterTriggerFollowsMovedAura() {
        Permanent original = addCreatureReady(player2, new GrizzlyBears());
        Permanent destination = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new PsychicOverload()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, original.getId());
        harness.passBothPriorities();

        Permanent aura = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player2, List.of(new AuraGraft()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, aura.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player2, destination.getId());
        assertThat(aura.getAttachedTo()).isEqualTo(destination.getId());
        harness.passBothPriorities();

        assertThat(destination.isTapped()).isTrue();
        assertThat(original.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A summoning-sick creature can use the granted untap ability, and discarding is a cost")
    void summoningSickCreaturePaysBeforeUntapping() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.tap();
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new PsychicOverload());
        aura.setAttachedTo(bears.getId());
        harness.setHand(player1, List.of(new LeoninScimitar(), new LeoninScimitar()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.passBothPriorities();
        assertThat(bears.isTapped()).isFalse();
    }
}
