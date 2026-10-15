package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.e.EncroachingWastes;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SunbirdStandard;
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

@CardUsed({RayOfRuin.class, GrizzlyBears.class, DuskLegionDreadnought.class, EncroachingWastes.class, Plains.class,
        SunbirdStandard.class})
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

    @Test
    @DisplayName("Cannot target an artifact that is neither a creature nor a Vehicle")
    void cannotTargetOrdinaryArtifact() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SunbirdStandard());
        harness.setHand(player1, List.of(new RayOfRuin()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can exile a creature controlled by the caster")
    void canExileOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        cast(target);

        assertExiled(target);
        completeScry();
    }

    @Test
    @DisplayName("The caster can keep the scryed card on top")
    void keepsScryedCardOnTop() {
        Plains top = new Plains();
        RayOfRuin next = new RayOfRuin();
        harness.setLibrary(player1, List.of(top, next));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(target);

        assertExiled(target);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).containsExactly(top);
        completeScry();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, next);
        harness.assertInGraveyard(player1, "Ray of Ruin");
    }

    @Test
    @DisplayName("The caster can put the scryed card on the bottom")
    void putsScryedCardOnBottom() {
        Plains top = new Plains();
        RayOfRuin next = new RayOfRuin();
        harness.setLibrary(player1, List.of(top, next));
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(target);

        assertExiled(target);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Ray of Ruin");
    }

    @Test
    @DisplayName("Exiles its target even when the caster's library is empty")
    void exilesWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast(target);

        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNotNull();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Ray of Ruin");
    }

    @Test
    @DisplayName("Does not scry when the only target is sacrificed in response")
    void doesNotScryWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new EncroachingWastes());
        Plains top = new Plains();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new RayOfRuin()));
        addMana();
        harness.castSorcery(player1, 0, 0, target.getId());

        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.passPriority(player1);
        harness.activateAbility(player2, 0, 1, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(target.getOriginalCard().getId())).isNull();
        harness.assertInGraveyard(player2, "Encroaching Wastes");
        harness.assertInGraveyard(player1, "Ray of Ruin");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void cast(Permanent target) {
        harness.setHand(player1, List.of(new RayOfRuin()));
        addMana();
        harness.castAndResolveSorcery(player1, 0, 0, target.getId());
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
