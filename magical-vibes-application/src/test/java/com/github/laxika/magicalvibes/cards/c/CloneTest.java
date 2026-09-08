package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.t.TreasureHunter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Clone.class, AirElemental.class, AngelOfMercy.class, AngelicChorus.class,
        ChoMannoRevolutionary.class, GrizzlyBears.class, Spellbook.class, TreasureHunter.class})
class CloneTest extends BaseCardTest {

    // ===== Copying a creature =====

    @Test
    @DisplayName("Clone copies a creature's power and toughness")
    void copiesPowerAndToughness() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Clone(), "{3}{U}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        // Should be prompted for may ability
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true); // accept → inner effect resolves inline

        // Should be prompted to choose a creature
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player1.getId());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        // Clone should now be on the battlefield with Grizzly Bears' stats
        List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
        Permanent clonePerm = bf.stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears")
                        && p.getOriginalCard().getName().equals("Clone"))
                .findFirst().orElse(null);

        assertThat(clonePerm).isNotNull();
        assertThat(clonePerm.getCard().getPower()).isEqualTo(2);
        assertThat(clonePerm.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Clone copies a creature's keywords (e.g., flying)")
    void copiesKeywords() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        harness.handleMayAbilityChosen(player1, true); // accept → inner effect resolves inline

        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        harness.handlePermanentChosen(player1, targetId);

        GameData gd = harness.getGameData();
        Permanent clonePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Clone"))
                .findFirst().orElse(null);

        assertThat(clonePerm).isNotNull();
        assertThat(clonePerm.getCard().getName()).isEqualTo("Air Elemental");
        assertThat(clonePerm.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Clone copies a creature's subtypes")
    void copiesSubtypes() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        harness.handleMayAbilityChosen(player1, true); // accept → inner effect resolves inline

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        GameData gd = harness.getGameData();
        Permanent clonePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Clone"))
                .findFirst().orElse(null);

        assertThat(clonePerm).isNotNull();
        assertThat(clonePerm.getCard().getSubtypes()).containsExactly(CardSubtype.BEAR);
    }

    // ===== Leaving the battlefield =====

    @Test
    @DisplayName("Clone goes to graveyard as Clone (not the copied name) when destroyed")
    void goesToGraveyardAsClone() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        harness.handleMayAbilityChosen(player1, true); // accept → inner effect resolves inline

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        GameData gd = harness.getGameData();

        // Now destroy the Clone (which looks like Grizzly Bears on the battlefield)
        Permanent clonePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Clone"))
                .findFirst().orElse(null);
        assertThat(clonePerm).isNotNull();

        // Remove it manually (simulating destruction)
        gd.playerBattlefields.get(player1.getId()).remove(clonePerm);
        gd.playerGraveyards.get(player1.getId()).add(clonePerm.getOriginalCard());

        // In graveyard it should be "Clone", not "Grizzly Bears"
        harness.assertInGraveyard(player1, "Clone");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Clone returns to hand as Clone when bounced")
    void returnsToHandAsClone() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        harness.handleMayAbilityChosen(player1, true); // accept → inner effect resolves inline

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        GameData gd = harness.getGameData();

        // Simulate bouncing by using the Permanent's getOriginalCard
        Permanent clonePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Clone"))
                .findFirst().orElse(null);
        assertThat(clonePerm).isNotNull();

        gd.playerBattlefields.get(player1.getId()).remove(clonePerm);
        gd.playerHands.get(player1.getId()).add(clonePerm.getOriginalCard());

        // In hand it should be "Clone", not "Grizzly Bears"
        harness.assertInHand(player1, "Clone");
    }

    // ===== Legend rule =====

    @Test
    @DisplayName("Clone triggers legend rule when copying a legendary creature")
    void triggersLegendRule() {
        ChoMannoRevolutionary choManno = new ChoMannoRevolutionary();
        harness.addToBattlefield(player1, choManno);
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        // Accept to copy — inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        // Choose to copy Cho-Manno
        UUID choMannoId = harness.getPermanentId(player1, "Cho-Manno, Revolutionary");
        harness.handlePermanentChosen(player1, choMannoId);

        GameData gd = harness.getGameData();

        // Legend rule should be triggered — player should be asked to choose which to keep
        assertThat(gd.interaction.permanentChoiceContext()).isInstanceOf(PermanentChoiceContext.LegendRule.class);
        assertThat(((PermanentChoiceContext.LegendRule) gd.interaction.permanentChoiceContext()).cardName()).isEqualTo("Cho-Manno, Revolutionary");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player1.getId());
    }

    // ===== Declining / no creatures =====

    @Test
    @DisplayName("Clone enters as 0/0 and dies when player declines to copy")
    void diesWhenPlayerDeclines() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        // Decline to copy
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();

        // Clone should be dead (0/0 killed by SBA)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getOriginalCard().getName().equals("Clone"));

        // Clone should be in graveyard as "Clone"
        harness.assertInGraveyard(player1, "Clone");
    }

    @Test
    @DisplayName("Clone enters as 0/0 and dies when no creatures on battlefield")
    void diesWhenNoCreatures() {
        // No creatures on any battlefield
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Clone should be dead (0/0 killed by SBA — no creatures to copy)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getOriginalCard().getName().equals("Clone"));

        // Clone should be in graveyard as "Clone"
        harness.assertInGraveyard(player1, "Clone");
    }

    // ===== Copied creature's ETB effects =====

    @Test
    @DisplayName("Clone copying a creature with mandatory ETB triggers that effect")
    void copiedCreatureMandatoryETBFires() {
        // Angel of Mercy has ETB: gain 3 life
        harness.addToBattlefield(player2, new AngelOfMercy());
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        harness.handleMayAbilityChosen(player1, true); // accept → inner effect resolves inline

        UUID angelId = harness.getPermanentId(player2, "Angel of Mercy");
        harness.handlePermanentChosen(player1, angelId);

        GameData gd = harness.getGameData();

        // Clone should be on the battlefield as Angel of Mercy
        Permanent clonePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getOriginalCard().getName().equals("Clone"))
                .findFirst().orElse(null);
        assertThat(clonePerm).isNotNull();
        assertThat(clonePerm.getCard().getName()).isEqualTo("Angel of Mercy");

        // The copied Angel of Mercy's ETB "gain 3 life" should be on the stack
        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getDescription().contains("Angel of Mercy"));

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Player 1 should have gained 3 life (20 → 23)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
    }

    @Test
    @DisplayName("Angelic Chorus sees cloned creature's toughness, not 0/0")
    void angelicChorusSeesCopiedToughness() {
        // Angelic Chorus: whenever a creature enters under your control, gain life equal to its toughness
        harness.addToBattlefield(player1, new AngelicChorus());
        // Grizzly Bears is a 2/2
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        harness.handleMayAbilityChosen(player1, true); // accept → inner effect resolves inline

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        GameData gd = harness.getGameData();

        // Angelic Chorus should have triggered with toughness=2 (not 0)
        assertThat(gd.stack).anyMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getDescription().contains("Angelic Chorus"));

        // Resolve the triggered ability
        harness.passBothPriorities();

        // Player 1 should have gained 2 life (20 → 22), proving Angelic Chorus saw toughness=2
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Clone does not put a copied targeted ETB ability on the stack without a legal target")
    void copiedCreatureTargetedETBIsSkippedWithoutLegalTarget() {
        // Treasure Hunter's ETB targets an artifact card in its controller's graveyard.
        harness.addToBattlefield(player2, new TreasureHunter());
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        // First may prompt: Clone's own "you may copy" prompt
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true); // accept → inner effect resolves inline

        // Choose to copy Treasure Hunter
        UUID hunterId = harness.getPermanentId(player2, "Treasure Hunter");
        harness.handlePermanentChosen(player1, hunterId);

        // The copied trigger has no legal target, so it is not put on the stack.
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).noneMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getDescription().contains("Treasure Hunter"));
    }

    @Test
    @DisplayName("Clone copying Treasure Hunter targets an artifact in its controller's graveyard")
    void copiedCreatureTargetedETBOffersArtifactTarget() {
        Spellbook spellbook = new Spellbook();
        harness.setGraveyard(player1, List.of(spellbook));
        harness.addToBattlefield(player2, new TreasureHunter());
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities(); // resolve creature spell → may on stack
        harness.passBothPriorities(); // resolve MayEffect → may prompt

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        UUID hunterId = harness.getPermanentId(player2, "Treasure Hunter");
        harness.handlePermanentChosen(player1, hunterId);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(spellbook.getId());

        harness.handleMultipleCardsChosen(player1, List.of(spellbook.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Spellbook");
        harness.assertNotInGraveyard(player1, "Spellbook");
    }
}

