package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({GrenzoHavocRaiser.class, Divination.class, Forest.class, GrizzlyBears.class})
class GrenzoHavocRaiserTest extends BaseCardTest {

    @Test
    @DisplayName("The exile mode grants an end-of-turn any-color cast permission")
    void exileModeGrantsCastPermission() {
        addGrenzoAttacking();
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard, new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setLife(player1, 20);

        resolveGrenzoTrigger("Exile the top card");

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).contains(topCard.getId());
        assertThat(gd.exilePlayAnyManaType).contains(topCard.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
    }

    @Test
    @DisplayName("The exile mode does not grant land-play permission")
    void exileModeDoesNotGrantLandPermission() {
        addGrenzoAttacking();
        Card topCard = new Forest();
        harness.setLibrary(player2, List.of(topCard));

        resolveGrenzoTrigger("Exile the top card");

        assertThat(gd.findExiledCard(topCard.getId())).isNotNull();
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThatThrownBy(() -> harness.castFromExile(player1, topCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The goad mode targets a creature controlled by the damaged player")
    void goadModeTargetsDamagedPlayerCreature() {
        addGrenzoAttacking();
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        resolveGrenzoTrigger("Goad target creature");
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    private void addGrenzoAttacking() {
        Card grenzo = new GrenzoHavocRaiser();
        grenzo.setName("Grenzo, Havoc Raiser");
        addCreatureReady(player1, grenzo);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
    }

    private void resolveGrenzoTrigger(String mode) {
        resolveCombat();
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        harness.handleListChoice(player1, mode);
        resolveAllTriggers();
    }
}
