package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HithlainRope;
import com.github.laxika.magicalvibes.cards.m.MyrBattlesphere;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScrapMastery.class, DarksteelRelic.class, GrizzlyBears.class, SolRing.class, MyrBattlesphere.class,
        HithlainRope.class})
class ScrapMasteryTest extends BaseCardTest {

    private void castScrapMastery() {
        harness.setHand(player1, List.of(new ScrapMastery()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("Returns graveyard artifacts and sacrifices battlefield artifacts")
    void swapsArtifactsWithBattlefields() {
        Permanent ownBattlefieldArtifact = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Permanent opponentBattlefieldArtifact = harness.addToBattlefieldAndReturn(player2, new DarksteelRelic());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Card ownGraveyardArtifact = new DarksteelRelic();
        Card opponentGraveyardArtifact = new DarksteelRelic();
        Card ownGraveyardCreature = new GrizzlyBears();
        Card opponentGraveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownGraveyardArtifact, ownGraveyardCreature));
        harness.setGraveyard(player2, List.of(opponentGraveyardArtifact, opponentGraveyardCreature));

        castScrapMastery();

        harness.assertOnBattlefield(player1, "Darksteel Relic");
        harness.assertOnBattlefield(player2, "Darksteel Relic");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(ownBattlefieldArtifact.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(opponentBattlefieldArtifact.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(ownBattlefieldArtifact.getCard().getId()))
                .contains(ownGraveyardCreature);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(opponentBattlefieldArtifact.getCard().getId()))
                .contains(opponentGraveyardCreature);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Leaves nonartifact graveyard cards and permanents alone")
    void leavesNonartifactsAlone() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card ownGraveyardCreature = new GrizzlyBears();
        Card opponentGraveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownGraveyardCreature));
        harness.setGraveyard(player2, List.of(opponentGraveyardCreature));

        castScrapMastery();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(ownCreature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(opponentCreature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownGraveyardCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentGraveyardCreature);
    }

    @Test
    @DisplayName("Sacrifices artifacts even when neither graveyard contains artifacts")
    void sacrificesArtifactsWithEmptyGraveyards() {
        Card ownArtifact = new SolRing();
        Card opponentArtifact = new SolRing();
        harness.addToBattlefield(player1, ownArtifact);
        harness.addToBattlefield(player2, opponentArtifact);
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        castScrapMastery();

        harness.assertNotOnBattlefield(player1, "Sol Ring");
        harness.assertNotOnBattlefield(player2, "Sol Ring");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownArtifact);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentArtifact);
    }

    @Test
    @DisplayName("Returns every graveyard artifact but leaves previously exiled artifacts alone")
    void returnsOnlyArtifactsExiledByThisSpell() {
        Card firstArtifact = new SolRing();
        Card secondArtifact = new SolRing();
        Card opponentArtifact = new SolRing();
        Card previouslyExiled = new SolRing();
        harness.setGraveyard(player1, List.of(firstArtifact, secondArtifact));
        harness.setGraveyard(player2, List.of(opponentArtifact));
        harness.setExile(player1, List.of(previouslyExiled));

        castScrapMastery();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(firstArtifact.getId(), secondArtifact.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(opponentArtifact.getId());
        assertThat(gd.exiledCards).hasSize(1);
        harness.assertNotInGraveyard(player1, "Sol Ring");
        harness.assertNotInGraveyard(player2, "Sol Ring");
    }

    @Test
    @DisplayName("Returned artifact creatures trigger their enter abilities after the sacrifice step")
    void returnedArtifactCreatureTriggersEnterAbility() {
        harness.setGraveyard(player1, List.of(new MyrBattlesphere()));

        castScrapMastery();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Myr Battlesphere");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(4)
                .allMatch(permanent -> permanent.getCard().getName().equals("Myr"));
        harness.assertNotInGraveyard(player1, "Myr Battlesphere");
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Artifacts that cannot be sacrificed remain on the battlefield for both players")
    void cannotSacrificeProtectedArtifacts() {
        Permanent ownRope = harness.addToBattlefieldAndReturn(player1, new HithlainRope());
        Permanent opponentRope = harness.addToBattlefieldAndReturn(player2, new HithlainRope());
        harness.addToBattlefield(player1, new SolRing());
        harness.addToBattlefield(player2, new SolRing());

        castScrapMastery();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownRope);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentRope);
        harness.assertNotInGraveyard(player1, "Hithlain Rope");
        harness.assertNotInGraveyard(player2, "Hithlain Rope");
        harness.assertNotOnBattlefield(player1, "Sol Ring");
        harness.assertNotOnBattlefield(player2, "Sol Ring");
        harness.assertInGraveyard(player1, "Sol Ring");
        harness.assertInGraveyard(player2, "Sol Ring");
    }
}
