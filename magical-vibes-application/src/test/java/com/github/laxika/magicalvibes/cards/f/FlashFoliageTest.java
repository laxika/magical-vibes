package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FlashFoliage.class, GrizzlyBears.class})
class FlashFoliageTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Saproling blocking the targeted creature and draws a card")
    void createsBlockingSaprolingAndDrawsCard() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(List.of(0));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));

        castFlashFoliage(attacker);

        Permanent token = findPermanents(player2, "Saproling").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.isBlocking()).isTrue();
        assertThat(token.getBlockingTargetIds()).containsExactly(attacker.getId());
        assertThat(gqs.isBlockedByAnyCreature(gd, attacker)).isTrue();
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot be cast before blockers are declared")
    void cannotCastBeforeBlockersAreDeclared() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(0));
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        giveSpell();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cannot target a creature attacking another player")
    void cannotTargetCreatureAttackingAnotherPlayer() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        giveSpell();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature attacking you");
    }

    private void castFlashFoliage(Permanent target) {
        giveSpell();
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
    }

    private void giveSpell() {
        harness.setHand(player2, List.of(new FlashFoliage()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
    }
}
