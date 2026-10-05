package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BoskBanneret;
import com.github.laxika.magicalvibes.cards.c.ChangelingSentinel;
import com.github.laxika.magicalvibes.cards.e.Earthbrawn;
import com.github.laxika.magicalvibes.cards.m.MosquitoGuard;
import com.github.laxika.magicalvibes.cards.v.VioletPall;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OrchardWarden.class, BoskBanneret.class, MosquitoGuard.class,
        ChangelingSentinel.class, Earthbrawn.class, VioletPall.class})
class OrchardWardenTest extends BaseCardTest {

    @Test
    @DisplayName("Gains life equal to the entering Treefolk's toughness when accepting")
    void gainsLifeWhenTreefolkEntersAccept() {
        harness.addToBattlefield(player1, new OrchardWarden());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new BoskBanneret(), "{1}{G}");

        harness.passBothPriorities(); // Resolve the creature spell and queue the trigger.
        harness.passBothPriorities(); // Resolve the trigger and prompt for the optional life gain.

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 23); // +3 = Bosk Banneret's toughness
    }

    @Test
    @DisplayName("No life gain when declining the may")
    void noLifeGainWhenDeclining() {
        harness.addToBattlefield(player1, new OrchardWarden());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new BoskBanneret(), "{1}{G}");

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not trigger when a non-Treefolk creature enters")
    void doesNotTriggerForNonTreefolk() {
        harness.addToBattlefield(player1, new OrchardWarden());
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new MosquitoGuard(), "{W}");

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not trigger when Orchard Warden itself enters")
    void doesNotTriggerForSelfEntering() {
        harness.setLife(player1, 20);

        harness.castFromHand(player1, new OrchardWarden(), "{4}{G}{G}");

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Uses the entering creature's toughness when the trigger resolves")
    void usesToughnessAtResolution() {
        harness.addToBattlefield(player1, new OrchardWarden());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new BoskBanneret(), "{1}{G}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Earthbrawn()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Bosk Banneret"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 26);
    }

    @Test
    @DisplayName("Uses last known toughness when the entering creature leaves before resolution")
    void usesLastKnownToughness() {
        harness.addToBattlefield(player1, new OrchardWarden());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new BoskBanneret(), "{1}{G}");
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Earthbrawn(), new VioletPall()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        var banneretId = harness.getPermanentId(player1, "Bosk Banneret");
        harness.castAndResolveInstant(player1, 0, banneretId);
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castAndResolveInstant(player1, 0, banneretId);
        harness.assertInGraveyard(player1, "Bosk Banneret");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 26);
    }

    @Test
    @DisplayName("Triggers for a creature with changeling")
    void triggersForChangeling() {
        harness.addToBattlefield(player1, new OrchardWarden());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new ChangelingSentinel(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Does not trigger for an opponent's Treefolk")
    void doesNotTriggerForOpponentTreefolk() {
        harness.addToBattlefield(player1, new OrchardWarden());
        harness.setLife(player1, 20);
        harness.enterBattlefieldAndReturn(player2, new BoskBanneret());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An existing Orchard Warden triggers for a second Orchard Warden")
    void triggersForAnotherWarden() {
        harness.addToBattlefield(player1, new OrchardWarden());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new OrchardWarden(), "{4}{G}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 26);
    }
}
