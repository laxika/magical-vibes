package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.e.EncroachingWastes;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RayOfRuin.class, GrizzlyBears.class, DuskLegionDreadnought.class, EncroachingWastes.class, Plains.class})
class RayOfRuinTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a target creature and scries 1")
    void exilesTargetCreatureAndScriesOne() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(target);

        assertExiled(target);
        completeScry();
    }

    @Test
    @DisplayName("Exiles a target Vehicle")
    void exilesTargetVehicle() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DuskLegionDreadnought());

        cast(target);

        assertExiled(target);
        completeScry();
    }

    @Test
    @DisplayName("Exiles a target nonbasic land")
    void exilesTargetNonbasicLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EncroachingWastes());

        cast(target);

        assertExiled(target);
        completeScry();
    }

    @Test
    @DisplayName("Cannot target a basic land")
    void cannotTargetBasicLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new RayOfRuin()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new RayOfRuin()));
        addMana();
        harness.castSorcery(player1, 0, 0, target.getId());
        harness.passBothPriorities();
    }

    private void assertExiled(Permanent target) {
        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNotNull();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    private void completeScry() {
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.stack).isEmpty();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
