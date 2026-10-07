package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AdaptiveSnapjaw;
import com.github.laxika.magicalvibes.cards.a.ArmoredTransport;
import com.github.laxika.magicalvibes.cards.g.GruulGuildgate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StructuralCollapse.class, ArmoredTransport.class, AdaptiveSnapjaw.class, GruulGuildgate.class, SimicGuildgate.class, DarksteelCitadel.class})
class StructuralCollapseTest extends BaseCardTest {

    @Test
    @DisplayName("Target player sacrifices their only artifact and land, then takes 2 damage")
    void sacrificesArtifactAndLandThenTakesDamage() {
        harness.addToBattlefield(player2, new ArmoredTransport());
        harness.addToBattlefield(player2, new GruulGuildgate());
        harness.addToBattlefield(player2, new AdaptiveSnapjaw());
        harness.setHand(player1, List.of(new StructuralCollapse()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertInGraveyard(player2, "Armored Transport");
        harness.assertInGraveyard(player2, "Gruul Guildgate");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Target player chooses which artifact and which land to sacrifice")
    void playerChoosesWhichPermanentsToSacrifice() {
        Permanent transport = harness.addToBattlefieldAndReturn(player2, new ArmoredTransport());
        harness.addToBattlefield(player2, new ArmoredTransport());
        Permanent gate = harness.addToBattlefieldAndReturn(player2, new GruulGuildgate());
        harness.addToBattlefield(player2, new SimicGuildgate());
        harness.setHand(player1, List.of(new StructuralCollapse()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(transport.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(transport, gate);
        harness.assertLife(player2, 20);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(gate.getId()));

        harness.assertInGraveyard(player2, "Armored Transport");
        harness.assertInGraveyard(player2, "Gruul Guildgate");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Damage is still dealt when the target player controls no artifact or land")
    void damageStillDealtWithNothingToSacrifice() {
        harness.addToBattlefield(player2, new AdaptiveSnapjaw());
        harness.setHand(player1, List.of(new StructuralCollapse()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        harness.assertLife(player2, 18);
    }

    @Test
    void sacrificesLandAndDealsDamageWithoutAnArtifact() {
        harness.addToBattlefield(player2, new GruulGuildgate());
        harness.setHand(player1, List.of(new StructuralCollapse()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertInGraveyard(player2, "Gruul Guildgate");
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    void sacrificesArtifactAndDealsDamageWithoutALand() {
        harness.addToBattlefield(player2, new ArmoredTransport());
        harness.setHand(player1, List.of(new StructuralCollapse()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertInGraveyard(player2, "Armored Transport");
        harness.assertLife(player2, 18);
    }

    @Test
    void canTargetItsController() {
        harness.addToBattlefield(player1, new ArmoredTransport());
        harness.addToBattlefield(player1, new GruulGuildgate());
        harness.setHand(player1, List.of(new StructuralCollapse()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveSorcery(player1, 0, player1.getId());

        harness.assertInGraveyard(player1, "Armored Transport");
        harness.assertInGraveyard(player1, "Gruul Guildgate");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    void sacrificesASoleArtifactLandOnlyOnceDespiteIndestructible() {
        harness.addToBattlefield(player2, new DarksteelCitadel());
        harness.setHand(player1, List.of(new StructuralCollapse()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertInGraveyard(player2, "Darksteel Citadel");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        harness.assertLife(player2, 18);
    }

    @Test
    void onlyLandCannotBeChosenAsArtifactInsteadOfAnotherArtifact() {
        Permanent citadel = harness.addToBattlefieldAndReturn(player2, new DarksteelCitadel());
        Permanent transport = harness.addToBattlefieldAndReturn(player2, new ArmoredTransport());
        harness.setHand(player1, List.of(new StructuralCollapse()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        if (choice != null) {
            assertThat(choice.validIds()).contains(transport.getId()).doesNotContain(citadel.getId());
            harness.handleMultiplePermanentsChosen(player2, List.of(transport.getId()));
        }

        harness.assertInGraveyard(player2, "Armored Transport");
        harness.assertInGraveyard(player2, "Darksteel Citadel");
        harness.assertLife(player2, 18);
    }
}
