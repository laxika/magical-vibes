package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DarkPrivilege;
import com.github.laxika.magicalvibes.cards.f.FreewindFalcon;
import com.github.laxika.magicalvibes.cards.g.GossamerChains;
import com.github.laxika.magicalvibes.cards.q.Quicksand;
import com.github.laxika.magicalvibes.cards.s.SisaysRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TeferisRealm.class, DarkPrivilege.class, FreewindFalcon.class, GossamerChains.class,
        Quicksand.class, SisaysRing.class})
class TeferisRealmTest extends BaseCardTest {

    @Test
    @DisplayName("Active player chooses creature and all nontoken creatures phase out")
    void choosingCreaturePhasesOutCreatures() {
        Permanent realm = addToBattlefield(player1, new TeferisRealm());
        Permanent creature = addToBattlefield(player1, new FreewindFalcon());
        Permanent opponentCreature = addToBattlefield(player2, new FreewindFalcon());
        Permanent land = addToBattlefield(player1, new Quicksand());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, "CREATURE");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(realm, land);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentCreature);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(opponentCreature);
    }

    @Test
    @DisplayName("Choosing artifact phases out all nontoken artifacts and no other types")
    void choosingArtifactPhasesOutArtifacts() {
        Permanent realm = addToBattlefield(player1, new TeferisRealm());
        Permanent artifact = addToBattlefield(player1, new SisaysRing());
        Permanent opponentArtifact = addToBattlefield(player2, new SisaysRing());
        Permanent creature = addToBattlefield(player1, new FreewindFalcon());
        Permanent land = addToBattlefield(player1, new Quicksand());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ARTIFACT");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(realm, creature, land);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentArtifact);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(artifact);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(opponentArtifact);
    }

    @Test
    @DisplayName("Choosing land phases out all nontoken lands and no other types")
    void choosingLandPhasesOutLands() {
        Permanent realm = addToBattlefield(player1, new TeferisRealm());
        Permanent land = addToBattlefield(player1, new Quicksand());
        Permanent opponentLand = addToBattlefield(player2, new Quicksand());
        Permanent creature = addToBattlefield(player1, new FreewindFalcon());
        Permanent artifact = addToBattlefield(player1, new SisaysRing());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "LAND");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(realm, creature, artifact);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(land);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentLand);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(land);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(opponentLand);
    }

    @Test
    @DisplayName("Opponent chooses during their upkeep")
    void activePlayerChoosesEvenWhenNotController() {
        addToBattlefield(player1, new TeferisRealm());
        Permanent opponentCreature = addToBattlefield(player2, new FreewindFalcon());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, "CREATURE");

        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(opponentCreature);
    }

    @Test
    @DisplayName("Tokens of the chosen type do not phase out")
    void tokensAreExempt() {
        addToBattlefield(player1, new TeferisRealm());
        Permanent creature = addToBattlefield(player1, new FreewindFalcon());
        Permanent token = addToBattlefield(player1, createTokenCreature("Saproling Token"));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "CREATURE");

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
    }

    @Test
    @DisplayName("Choosing non-Aura enchantment phases out Teferi's Realm itself, not Auras")
    void choosingEnchantmentPhasesOutRealmNotAura() {
        Permanent realm = addToBattlefield(player1, new TeferisRealm());
        Permanent creature = addToBattlefield(player1, new FreewindFalcon());
        Permanent otherEnchantment = addToBattlefield(player1, new GossamerChains());
        Permanent aura = addToBattlefield(player1, new DarkPrivilege());
        aura.setAttachedTo(creature.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "NON_AURA_ENCHANTMENT");

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(realm, otherEnchantment);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature, aura);
    }

    @Test
    @DisplayName("An Aura attached to a phased-out creature phases out with it")
    void attachedAuraPhasesOutWithCreature() {
        Permanent realm = addToBattlefield(player1, new TeferisRealm());
        Permanent creature = addToBattlefield(player1, new FreewindFalcon());
        Permanent aura = addToBattlefield(player1, new DarkPrivilege());
        aura.setAttachedTo(creature.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "CREATURE");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(realm);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature, aura);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature, aura);
    }

    @Test
    @DisplayName("Phased-out permanents phase in during their controller's next untap")
    void phasedOutPermanentsPhaseBackIn() {
        addToBattlefield(player1, new TeferisRealm());
        Permanent creature = addToBattlefield(player1, new FreewindFalcon());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "CREATURE");
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(creature);

        advanceTurn(); // player2's turn — Realm triggers again; choose land so creatures stay out
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleListChoice(player2, "LAND");
        }
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(creature);

        advanceTurn(); // back to player1's untap step
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent addToBattlefield(Player player, Card card) {
        return harness.addToBattlefieldAndReturn(player, card);
    }

    private Card createTokenCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{G}");
        card.setColor(CardColor.GREEN);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
