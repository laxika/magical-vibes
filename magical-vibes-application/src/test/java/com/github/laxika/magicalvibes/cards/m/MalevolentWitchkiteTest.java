package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IvoryCup;
import com.github.laxika.magicalvibes.cards.o.Opposition;
import com.github.laxika.magicalvibes.cards.s.SailorOfMeans;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MalevolentWitchkite.class, Forest.class, GrizzlyBears.class, IvoryCup.class,
        Opposition.class, SailorOfMeans.class})
class MalevolentWitchkiteTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices any number of artifacts, enchantments, and tokens, then draws that many cards")
    void sacrificesEligiblePermanentsAndDrawsPerPermanent() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new IvoryCup());
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new Opposition());
        Permanent tokenSource = harness.enterBattlefieldAndReturn(player1, new SailorOfMeans());
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Treasure");
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.enterBattlefieldAndReturn(player1, new MalevolentWitchkite());
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                artifact.getId(), enchantment.getId(), token.getId());

        harness.handleMultiplePermanentsChosen(player1,
                List.of(artifact.getId(), enchantment.getId(), token.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(tokenSource);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(artifact.getId())
                        || permanent.getId().equals(enchantment.getId())
                        || permanent.getId().equals(token.getId()));
    }

    @Test
    @DisplayName("Only the controller's artifacts, enchantments, and tokens can be sacrificed")
    void onlySacrificesControllerPermanents() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new IvoryCup());
        Permanent opponentArtifact = harness.addToBattlefieldAndReturn(player2, new IvoryCup());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.enterBattlefieldAndReturn(player1, new MalevolentWitchkite());
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(ownArtifact.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(ownArtifact.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentArtifact);
    }
}
