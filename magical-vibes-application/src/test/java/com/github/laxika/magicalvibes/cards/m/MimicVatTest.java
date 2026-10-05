package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.p.PullFromEternity;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MimicVat.class, CruelEdict.class, GrizzlyBears.class, GiantSpider.class})
class MimicVatTest extends BaseCardTest {

    // ===== Imprint trigger =====

    @Test
    @DisplayName("Imprint triggers when a creature dies and offers may ability")
    void imprintTriggersOnCreatureDeath() {
        harness.addToBattlefield(player1, new MimicVat());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Player1 kills player2's creature with Cruel Edict
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Mimic Vat's imprint trigger should present a may ability
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting imprint exiles the dying card and imprints it")
    void acceptingImprintExilesAndImprints() {
        harness.addToBattlefield(player1, new MimicVat());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Accept the imprint
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // Resolve the imprint effect

        GameData gd = harness.getGameData();

        // Grizzly Bears should no longer be in graveyard
        harness.assertNotInGraveyard(player2, "Grizzly Bears");

        // Grizzly Bears should be exiled (in its owner's exile zone)
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Mimic Vat should have Grizzly Bears imprinted
        Permanent vat = findPermanent(player1, "Mimic Vat");
        assertThat(gd.getImprintedCard(vat.getCard())).isNotNull();
        assertThat(gd.getImprintedCard(vat.getCard()).getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Declining imprint leaves card in graveyard")
    void decliningImprintLeavesCardInGraveyard() {
        harness.addToBattlefield(player1, new MimicVat());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Decline the imprint
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();

        // Grizzly Bears should remain in graveyard
        harness.assertInGraveyard(player2, "Grizzly Bears");

        // Mimic Vat should have nothing imprinted
        Permanent vat = findPermanent(player1, "Mimic Vat");
        assertThat(gd.getImprintedCard(vat.getCard())).isNull();
    }

    @Test
    @DisplayName("Imprint set in an AI simulation copy does not leak into the real game")
    void simulatedImprintDoesNotLeakIntoRealGame() {
        harness.addToBattlefield(player1, new MimicVat());
        Permanent vat = findPermanent(player1, "Mimic Vat");

        GameData simCopy = gd.simulationCopy();
        simCopy.setImprintedCard(vat.getCard(), new GrizzlyBears());

        assertThat(simCopy.getImprintedCard(vat.getCard())).isNotNull();
        assertThat(gd.getImprintedCard(vat.getCard())).isNull();
    }

    // ===== Imprint replacement =====

    @Test
    @DisplayName("New imprint replaces old imprint, returning old card to graveyard")
    void newImprintReplacesOld() {
        harness.addToBattlefield(player1, new MimicVat());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GiantSpider());

        // Kill first creature (Grizzly Bears): player2 has two creatures so must choose
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Player2 chooses to sacrifice Grizzly Bears
        harness.handlePermanentChosen(player2, bearsId);

        // Accept imprint of Grizzly Bears
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent vat = findPermanent(player1, "Mimic Vat");
        assertThat(gd.getImprintedCard(vat.getCard()).getName()).isEqualTo("Grizzly Bears");

        // Kill second creature (Giant Spider): now player2 has only one, auto-sacrificed
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Accept second imprint
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        // Grizzly Bears (previously imprinted) should now be in a graveyard
        boolean oldCardInGraveyard = false;
        for (UUID pid : gd.orderedPlayerIds) {
            if (gd.playerGraveyards.get(pid).stream().anyMatch(c -> c.getName().equals("Grizzly Bears"))) {
                oldCardInGraveyard = true;
                break;
            }
        }
        assertThat(oldCardInGraveyard).isTrue();

        // Giant Spider should now be imprinted
        assertThat(gd.getImprintedCard(vat.getCard())).isNotNull();
        assertThat(gd.getImprintedCard(vat.getCard()).getName()).isEqualTo("Giant Spider");
    }

    // ===== Token creation =====

    @Test
    @DisplayName("Activated ability creates a token copy of the imprinted card with haste")
    void activatedAbilityCreatesTokenCopy() {
        harness.addToBattlefield(player1, new MimicVat());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Imprint a creature
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // Resolve imprint

        // Activate Mimic Vat's token-making ability
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // Resolve activated ability

        GameData gd = harness.getGameData();

        // A token copy of Grizzly Bears should be on the battlefield
        Permanent tokenBears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken())
                .findFirst().orElse(null);
        assertThat(tokenBears).isNotNull();
        assertThat(tokenBears.getCard().getPower()).isEqualTo(2);
        assertThat(tokenBears.getCard().getToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, tokenBears, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("No token created when nothing is imprinted")
    void noTokenWhenNothingImprinted() {
        harness.addToBattlefield(player1, new MimicVat());

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // No token should be created
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().isToken());
    }

    // ===== Token exile at end step =====

    @Test
    @DisplayName("Token is exiled at beginning of next end step")
    void tokenExiledAtEndStep() {
        harness.addToBattlefield(player1, new MimicVat());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Imprint a creature
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        // Create token
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();

        // Token should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken());

        // Advance to end step
        advanceToEndStep();
        harness.passBothPriorities(); // Resolve the delayed exile trigger.

        // Token should be exiled
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken());
    }

    // ===== Triggers on opponent's creatures =====

    @Test
    @DisplayName("Triggers when opponent's creature dies")
    void triggersWhenOpponentCreatureDies() {
        harness.addToBattlefield(player1, new MimicVat());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Should get a may ability prompt
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Triggers when controller's own creature dies")
    void triggersWhenOwnCreatureDies() {
        harness.addToBattlefield(player1, new MimicVat());
        harness.addToBattlefield(player1, new GrizzlyBears());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player2, 0, player1.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @CardUsed({Naturalize.class})
    @DisplayName("Imprint still exiles the dying card after Mimic Vat is destroyed")
    void imprintResolvesAfterVatLeaves() {
        imprintBears();
        harness.addToBattlefield(player2, new GiantSpider());
        harness.setHand(player1, List.of(new CruelEdict(), new Naturalize()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Mimic Vat"));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Giant Spider");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Giant Spider"));
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @CardUsed({Naturalize.class})
    @DisplayName("Token creation still resolves after Mimic Vat is destroyed")
    void tokenCreationResolvesAfterVatLeaves() {
        imprintBears();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player1, List.of(new Naturalize()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Mimic Vat"));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @CardUsed({PullFromEternity.class})
    @DisplayName("A card that leaves exile cannot be copied by Mimic Vat")
    void noTokenAfterImprintedCardLeavesExile() {
        imprintBears();
        UUID exiledId = gd.getPlayerExiledCards(player2.getId()).getFirst().getId();
        harness.setHand(player1, List.of(new PullFromEternity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, exiledId);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @CardUsed({Clone.class})
    @DisplayName("Copying a Mimic Vat token does not copy its granted haste")
    void grantedHasteIsNotCopiable() {
        imprintBears();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Grizzly Bears");
        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, token.getId());

        Permanent clone = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof Clone)
                .findFirst().orElseThrow();
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, clone, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("An imprinted manifested sorcery cannot produce a token")
    void noTokenCopyOfManifestedSorcery() {
        harness.addToBattlefield(player1, new MimicVat());
        harness.addToBattlefield(player2, new CruelEdict());
        Permanent manifested = findPermanent(player2, "Cruel Edict");
        manifested.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        manifested.setManifested(true);
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Cruel Edict"));

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("A token dying does not trigger imprint or replace the exiled card")
    void tokenDeathDoesNotTriggerImprint() {
        imprintBears();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        setupPlayer2Active();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player2, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction())
                .isNotInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    private void imprintBears() {
        harness.addToBattlefield(player1, new MimicVat());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
    }
}
