package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PsychicOverloadTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Psychic Overload taps the enchanted permanent")
    void resolvingTapsEnchantedPermanent() {
        Permanent fountain = new Permanent(new FountainOfYouth());
        gd.playerBattlefields.get(player2.getId()).add(fountain);

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
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.tap();
        gd.playerBattlefields.get(player2.getId()).add(bears);

        Permanent aura = new Permanent(new PsychicOverload());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        advanceToNextTurn(player1);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted permanent's controller can discard two artifact cards to untap it")
    void discardingTwoArtifactsUntapsEnchantedPermanent() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.tap();
        gd.playerBattlefields.get(player2.getId()).add(bears);

        Permanent aura = new Permanent(new PsychicOverload());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

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
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.tap();
        gd.playerBattlefields.get(player1.getId()).add(bears);

        Permanent aura = new Permanent(new PsychicOverload());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        harness.setHand(player1, List.of(new LeoninScimitar()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void advanceToNextTurn(com.github.laxika.magicalvibes.model.Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
