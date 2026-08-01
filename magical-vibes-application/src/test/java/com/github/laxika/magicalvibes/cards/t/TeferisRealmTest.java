package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TeferisRealmTest extends BaseCardTest {

    @Test
    @DisplayName("Active player chooses creature and all nontoken creatures phase out")
    void choosingCreaturePhasesOutCreatures() {
        Permanent realm = addToBattlefield(player1, new TeferisRealm());
        Permanent bears = addToBattlefield(player1, new GrizzlyBears());
        Permanent opponentBears = addToBattlefield(player2, new GrizzlyBears());
        Permanent island = addToBattlefield(player1, new Island());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleListChoice(player1, "CREATURE");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(realm, island);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentBears);
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(bears);
        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(opponentBears);
    }

    @Test
    @DisplayName("Opponent chooses during their upkeep")
    void activePlayerChoosesEvenWhenNotController() {
        addToBattlefield(player1, new TeferisRealm());
        Permanent opponentBears = addToBattlefield(player2, new GrizzlyBears());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleListChoice(player2, "CREATURE");

        assertThat(gd.phasedOutPermanents.get(player2.getId())).contains(opponentBears);
    }

    @Test
    @DisplayName("Tokens of the chosen type do not phase out")
    void tokensAreExempt() {
        addToBattlefield(player1, new TeferisRealm());
        Permanent bears = addToBattlefield(player1, new GrizzlyBears());
        Permanent token = addToBattlefield(player1, createTokenCreature("Saproling Token"));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "CREATURE");

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
    }

    @Test
    @DisplayName("Choosing non-Aura enchantment phases out Teferi's Realm itself, not Auras")
    void choosingEnchantmentPhasesOutRealmNotAura() {
        Permanent realm = addToBattlefield(player1, new TeferisRealm());
        Permanent bears = addToBattlefield(player1, new GrizzlyBears());
        Permanent aura = addToBattlefield(player1, new Pacifism());
        aura.setAttachedTo(bears.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "NON_AURA_ENCHANTMENT");

        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(realm);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears, aura);
    }

    @Test
    @DisplayName("Phased-out permanents phase in during their controller's next untap")
    void phasedOutPermanentsPhaseBackIn() {
        addToBattlefield(player1, new TeferisRealm());
        Permanent bears = addToBattlefield(player1, new GrizzlyBears());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "CREATURE");
        assertThat(gd.phasedOutPermanents.get(player1.getId())).contains(bears);

        advanceTurn(); // player2's turn — Realm triggers again; choose land so creatures stay out
        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleListChoice(player2, "LAND");
        }
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);

        advanceTurn(); // back to player1's untap step
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
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
