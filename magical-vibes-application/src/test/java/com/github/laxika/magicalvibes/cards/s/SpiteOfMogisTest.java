package com.github.laxika.magicalvibes.cards.s;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hubris;
import com.github.laxika.magicalvibes.cards.m.MagmaJet;
import com.github.laxika.magicalvibes.cards.m.MagmaSpray;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.n.NessianGameWarden;
import com.github.laxika.magicalvibes.cards.r.RiseOfEagles;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({SpiteOfMogis.class, GrizzlyBears.class, MagmaJet.class, Shock.class, Mountain.class,
        MagmaSpray.class, NessianGameWarden.class, RiseOfEagles.class, Hubris.class})
class SpiteOfMogisTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to instant and sorcery cards in the controller's graveyard, then scries 1")
    void dealsDamageAndScries() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new MagmaJet(), new Shock(), new Mountain()));
        harness.setGraveyard(player2, List.of(new Shock()));
        harness.setLibrary(player1, List.of(new Mountain(), new Mountain()));
        harness.setHand(player1, List.of(new SpiteOfMogis()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Spite of Mogis");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new SpiteOfMogis()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An empty graveyard deals zero damage, still scries, and allows bottoming the card")
    void zeroDamageStillScries() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new NessianGameWarden());
        RiseOfEagles top = new RiseOfEagles();
        MagmaSpray next = new MagmaSpray();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(top, next));
        harness.setHand(player1, List.of(new SpiteOfMogis()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(top);
        harness.assertNotInGraveyard(player1, "Spite of Mogis");

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(next, top);
        harness.assertOnBattlefield(player1, "Nessian Game Warden");
        harness.assertInGraveyard(player1, "Spite of Mogis");
    }

    @Test
    @DisplayName("Counts instants and sorceries, including another Spite, but excludes creatures and opponents' cards")
    void countsBothSpellTypesAndOtherCopiesWithEmptyLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NessianGameWarden());
        harness.setGraveyard(player1, List.of(new MagmaSpray(), new RiseOfEagles(),
                new SpiteOfMogis(), new NessianGameWarden()));
        harness.setGraveyard(player2, List.of(new MagmaSpray(), new RiseOfEagles()));
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new SpiteOfMogis()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Nessian Game Warden");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof SpiteOfMogis).hasSize(2);
    }

    @Test
    @DisplayName("Damage uses the graveyard at resolution rather than when the spell was cast")
    void countsGraveyardAtResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NessianGameWarden());
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new SpiteOfMogis(), new MagmaSpray()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Magma Spray");
        assertThat(target.getMarkedDamage()).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Nessian Game Warden");
        harness.assertInGraveyard(player1, "Spite of Mogis");
    }

    @Test
    @DisplayName("Does not scry if its only target leaves the battlefield before resolution")
    void illegalTargetPreventsScry() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NessianGameWarden());
        RiseOfEagles top = new RiseOfEagles();
        harness.setLibrary(player1, List.of(top));
        harness.setHand(player1, List.of(new SpiteOfMogis()));
        harness.setHand(player2, List.of(new Hubris()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.assertNotOnBattlefield(player2, "Nessian Game Warden");
        harness.assertInHand(player2, "Nessian Game Warden");
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        harness.assertInGraveyard(player1, "Spite of Mogis");
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new SpiteOfMogis()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
