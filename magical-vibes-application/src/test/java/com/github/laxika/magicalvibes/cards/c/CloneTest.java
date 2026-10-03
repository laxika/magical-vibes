package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.AngelOfMercy;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TreasureHunter;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Clone.class, AirElemental.class, AngelOfMercy.class, AngelicChorus.class,
        ChoMannoRevolutionary.class, GloriousAnthem.class, GrizzlyBears.class, Spellbook.class, Shock.class,
        TreasureHunter.class, Unsummon.class})
class CloneTest extends BaseCardTest {

    @Test
    @DisplayName("Clone copies a creature's power and toughness")
    void copiesPowerAndToughness() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Clone(), "{3}{U}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities(); // Resolve the spell up to its entry choice.

        // Should be prompted for may ability
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        // Should be prompted to choose a creature
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player1.getId());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        // Clone should now be on the battlefield with Grizzly Bears' stats
        Permanent clonePerm = findPermanent(player1, "Grizzly Bears");
        assertThat(clonePerm).isNotNull();
        assertThat(clonePerm.getOriginalCard().getName()).isEqualTo("Clone");
        assertThat(clonePerm.getCard().getPower()).isEqualTo(2);
        assertThat(clonePerm.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Clone copies a creature's keywords (e.g., flying)")
    void copiesKeywords() {
        harness.addToBattlefield(player2, new AirElemental());
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities(); // Resolve the spell up to its entry choice.

        harness.handleMayAbilityChosen(player1, true);

        UUID targetId = harness.getPermanentId(player2, "Air Elemental");
        harness.handlePermanentChosen(player1, targetId);

        Permanent clonePerm = findPermanent(player1, "Air Elemental");

        assertThat(clonePerm).isNotNull();
        assertThat(clonePerm.getOriginalCard().getName()).isEqualTo("Clone");
        assertThat(clonePerm.getCard().getName()).isEqualTo("Air Elemental");
        assertThat(clonePerm.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Clone copies a creature's subtypes")
    void copiesSubtypes() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities(); // Resolve the spell up to its entry choice.

        harness.handleMayAbilityChosen(player1, true);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        Permanent clonePerm = findPermanent(player1, "Grizzly Bears");

        assertThat(clonePerm).isNotNull();
        assertThat(clonePerm.getOriginalCard().getName()).isEqualTo("Clone");
        assertThat(clonePerm.getCard().getSubtypes()).containsExactly(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("Clone can copy a creature token's characteristics without becoming a token")
    void copiesCreatureTokenCharacteristics() {
        Card tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        harness.addToBattlefield(player2, tokenCard);
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        UUID tokenId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, tokenId);

        Permanent clonePerm = findPermanent(player1, "Grizzly Bears");

        assertThat(clonePerm).isNotNull();
        assertThat(clonePerm.getOriginalCard().getName()).isEqualTo("Clone");
        assertThat(clonePerm.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(clonePerm.getCard().getPower()).isEqualTo(2);
        assertThat(clonePerm.getCard().getToughness()).isEqualTo(2);
        assertThat(clonePerm.getCard().isToken()).isFalse();
    }

    @Test
    @DisplayName("Clone goes to graveyard as Clone (not the copied name) when destroyed")
    void goesToGraveyardAsClone() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities(); // Resolve the spell up to its entry choice.

        harness.handleMayAbilityChosen(player1, true);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        // Now destroy the Clone (which looks like Grizzly Bears on the battlefield)
        Permanent clonePerm = findPermanent(player1, "Grizzly Bears");
        assertThat(clonePerm).isNotNull();
        assertThat(clonePerm.getOriginalCard().getName()).isEqualTo("Clone");

        // Destroy it through the engine with Shock.
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, clonePerm.getId());

        // In graveyard it should be "Clone", not "Grizzly Bears"
        harness.assertInGraveyard(player1, "Clone");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Clone returns to hand as Clone when bounced")
    void returnsToHandAsClone() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities(); // Resolve the spell up to its entry choice.

        harness.handleMayAbilityChosen(player1, true);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        Permanent clonePerm = findPermanent(player1, "Grizzly Bears");
        assertThat(clonePerm).isNotNull();
        assertThat(clonePerm.getOriginalCard().getName()).isEqualTo("Clone");

        // Bounce it through the engine.
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, clonePerm.getId());

        // In hand it should be "Clone", not "Grizzly Bears"
        harness.assertInHand(player1, "Clone");
        harness.assertNotInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Clone triggers legend rule when copying a legendary creature")
    void triggersLegendRule() {
        ChoMannoRevolutionary choManno = new ChoMannoRevolutionary();
        harness.addToBattlefield(player1, choManno);
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities(); // Resolve the spell up to its entry choice.

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

    @Test
    @DisplayName("Clone enters as 0/0 and dies when player declines to copy")
    void diesWhenPlayerDeclines() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities(); // Resolve the spell up to its entry choice.

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

    @Test
    @DisplayName("Clone does not copy a noncreature permanent")
    void diesWhenOnlyNoncreatureIsOnBattlefield() {
        harness.addToBattlefield(player2, new Spellbook());
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Clone");
        harness.assertOnBattlefield(player2, "Spellbook");
        harness.assertInGraveyard(player1, "Clone");
    }

    @Test
    @DisplayName("Clone copying a creature with mandatory ETB triggers that effect")
    void copiedCreatureMandatoryETBFires() {
        // Angel of Mercy has ETB: gain 3 life
        harness.addToBattlefield(player2, new AngelOfMercy());
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities(); // Resolve the spell up to its entry choice.

        harness.handleMayAbilityChosen(player1, true);

        UUID angelId = harness.getPermanentId(player2, "Angel of Mercy");
        harness.handlePermanentChosen(player1, angelId);

        GameData gd = harness.getGameData();

        // Clone should be on the battlefield as Angel of Mercy
        Permanent clonePerm = findPermanent(player1, "Angel of Mercy");
        assertThat(clonePerm).isNotNull();
        assertThat(clonePerm.getOriginalCard().getName()).isEqualTo("Clone");
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
        harness.passBothPriorities(); // Resolve the spell up to its entry choice.

        harness.handleMayAbilityChosen(player1, true);

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
        harness.passBothPriorities(); // Resolve the spell up to its entry choice.

        // First may prompt: Clone's own "you may copy" prompt
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

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
        harness.passBothPriorities(); // Resolve the spell up to its entry choice.

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

    @Test
    @DisplayName("Clone does not copy counters or tapped status")
    void doesNotCopyCountersOrTappedStatus() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        bears.tap();

        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());

        Permanent copy = findPermanent(player1, "Grizzly Bears");
        assertThat(copy.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(copy.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(2);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Clone copies the characteristics already copied by another Clone")
    void copiesAnotherClonesCopiedCharacteristics() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));
        Permanent firstCopy = findPermanent(player1, "Grizzly Bears");

        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, firstCopy.getId());

        assertThat(findPermanents(player1, "Grizzly Bears")).hasSize(2)
                .allSatisfy(copy -> {
                    assertThat(copy.getOriginalCard().getName()).isEqualTo("Clone");
                    assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(2);
                    assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(2);
                });
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Copying an unmodified Clone offers its newly gained entry replacement")
    void copyingUnmodifiedCloneOffersAnotherCopyChoice() {
        harness.addToBattlefield(player1, new GloriousAnthem());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        Permanent originalClone = findPermanent(player1, "Clone");

        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, originalClone.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        assertThat(countPermanents(player1, "Clone")).isEqualTo(1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));

        assertThat(countPermanents(player1, "Clone")).isEqualTo(1);
        Permanent copy = findPermanent(player1, "Grizzly Bears");
        assertThat(copy.getOriginalCard().getName()).isEqualTo("Clone");
        assertThat(gqs.getEffectivePower(gd, copy)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, copy)).isEqualTo(3);
    }
}
