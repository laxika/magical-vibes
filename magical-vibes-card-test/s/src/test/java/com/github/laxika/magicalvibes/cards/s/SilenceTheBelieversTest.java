package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GnarledScarhide;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.v.VelaTheNightClad;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilenceTheBelievers.class, GrizzlyBears.class, Pacifism.class,
        LeoninScimitar.class, FountainOfYouth.class, GnarledScarhide.class,
        Unsummon.class, VelaTheNightClad.class})
class SilenceTheBelieversTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles each target creature and all Auras attached to them")
    void exilesTargetsAndAttachedAuras() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attachAura(player1, ownCreature);
        attachAura(player2, opponentCreature);

        castAndResolveSilenceTheBelievers(List.of(ownCreature.getId(), opponentCreature.getId()));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName())
                .contains("Pacifism");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Pacifism");
    }

    @Test
    @DisplayName("Leaves Equipment attached to an exiled creature on the battlefield")
    void onlyExilesAttachedAuras() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeoninScimitar());
        equipment.setAttachedTo(creature.getId());

        castAndResolveSilenceTheBelievers(List.of(creature.getId()));

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Strive requires {2}{B} for each additional target")
    void striveAddsCostForEachAdditionalTarget() {
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new SilenceTheBelievers()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can target only creatures")
    void cannotTargetNonCreaturePermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        harness.setHand(player1, List.of(new SilenceTheBelievers()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can resolve with no targets for its base cost")
    void canCastWithZeroTargets() {
        harness.addToBattlefield(player2, new GnarledScarhide());
        harness.setHand(player1, List.of(new SilenceTheBelievers()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0, List.<UUID>of());

        harness.assertOnBattlefield(player2, "Gnarled Scarhide");
        harness.assertInGraveyard(player1, "Silence the Believers");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Three targets cost the base cost plus two strive payments")
    void threeTargetsPayTwoStriveCosts() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GnarledScarhide());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GnarledScarhide());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GnarledScarhide());
        harness.setHand(player1, List.of(new SilenceTheBelievers()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId(), third.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getId())
                .containsExactlyInAnyOrder(first.getCard().getId(), second.getCard().getId(), third.getCard().getId());
        harness.assertNotOnBattlefield(player2, "Gnarled Scarhide");
    }

    @Test
    @DisplayName("Exiles bestowed Auras instead of leaving them as creatures")
    void exilesBestowedAura() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new GnarledScarhide());
        GnarledScarhide aura = new GnarledScarhide();
        harness.setHand(player1, List.of(aura));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();

        castAndResolveSilenceTheBelievers(List.of(host.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getId()).contains(aura.getId());
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getId()).contains(host.getCard().getId());
        harness.assertNotOnBattlefield(player1, "Gnarled Scarhide");
        harness.assertNotOnBattlefield(player2, "Gnarled Scarhide");
    }

    @Test
    @DisplayName("Still exiles a legal target when another target leaves in response")
    void resolvesForRemainingLegalTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attachAura(player2, first);
        attachAura(player1, second);
        prepareSilenceTheBelievers(List.of(first.getId(), second.getId()));
        harness.castInstant(player1, 0, List.of(first.getId(), second.getId()));

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, first.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Pacifism");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getId()).containsExactly(second.getCard().getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName()).containsExactly("Pacifism");
    }

    @Test
    @DisplayName("Simultaneously exiled creatures see each other's leave triggers")
    void simultaneousExilePreservesLeaveTriggers() {
        Permanent vela = harness.addToBattlefieldAndReturn(player2, new VelaTheNightClad());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GnarledScarhide());
        harness.setLife(player1, 20);

        castAndResolveSilenceTheBelievers(List.of(vela.getId(), other.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertNotOnBattlefield(player2, "Vela the Night-Clad");
        harness.assertNotOnBattlefield(player2, "Gnarled Scarhide");
    }

    private void castAndResolveSilenceTheBelievers(List<UUID> targetIds) {
        prepareSilenceTheBelievers(targetIds);
        harness.castAndResolveInstant(player1, 0, targetIds);
    }

    private void prepareSilenceTheBelievers(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new SilenceTheBelievers()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, targetIds.size() == 1 ? 2 : 4);
    }

    private void attachAura(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new Pacifism());
        aura.setAttachedTo(creature.getId());
    }
}
