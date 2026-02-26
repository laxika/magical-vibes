package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfImprintedCardEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MimicVatTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has imprint trigger and activated ability")
    void hasCorrectStructure() {
        MimicVat card = new MimicVat();

        assertThat(card.getEffects(EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_ANY_NONTOKEN_CREATURE_DIES).getFirst();
        assertThat(may.wrapped()).isInstanceOf(ImprintDyingCreatureEffect.class);

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().isRequiresTap()).isTrue();
        assertThat(card.getActivatedAbilities().getFirst().getEffects())
                .hasSize(1)
                .anyMatch(e -> e instanceof CreateTokenCopyOfImprintedCardEffect);
    }

    // ===== Imprint trigger =====

    @Test
    @DisplayName("Imprint triggers when a creature dies and offers may ability")
    void imprintTriggersOnCreatureDeath() {
        harness.addToBattlefield(player1, new MimicVat());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Player1 kills player2's creature with Cruel Edict
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Cruel Edict

        // Mimic Vat's imprint trigger should present a may ability
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Accepting imprint exiles the dying card and imprints it")
    void acceptingImprintExilesAndImprints() {
        harness.addToBattlefield(player1, new MimicVat());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Cruel Edict → creature dies → may trigger

        // Accept the imprint
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // Resolve the imprint effect

        GameData gd = harness.getGameData();

        // Grizzly Bears should no longer be in graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));

        // Grizzly Bears should be exiled (in its owner's exile zone)
        assertThat(gd.playerExiledCards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Mimic Vat should have Grizzly Bears imprinted
        Permanent vat = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mimic Vat"))
                .findFirst().orElseThrow();
        assertThat(vat.getCard().getImprintedCard()).isNotNull();
        assertThat(vat.getCard().getImprintedCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Declining imprint leaves card in graveyard")
    void decliningImprintLeavesCardInGraveyard() {
        harness.addToBattlefield(player1, new MimicVat());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Cruel Edict

        // Decline the imprint
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();

        // Grizzly Bears should remain in graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Mimic Vat should have nothing imprinted
        Permanent vat = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mimic Vat"))
                .findFirst().orElseThrow();
        assertThat(vat.getCard().getImprintedCard()).isNull();
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
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Cruel Edict → player2 prompted to choose

        // Player2 chooses to sacrifice Grizzly Bears
        harness.handlePermanentChosen(player2, bearsId);

        // Accept imprint of Grizzly Bears
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        Permanent vat = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mimic Vat"))
                .findFirst().orElseThrow();
        assertThat(vat.getCard().getImprintedCard().getName()).isEqualTo("Grizzly Bears");

        // Kill second creature (Giant Spider): now player2 has only one, auto-sacrificed
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Cruel Edict → auto-sacrifice Giant Spider

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
        assertThat(vat.getCard().getImprintedCard()).isNotNull();
        assertThat(vat.getCard().getImprintedCard().getName()).isEqualTo("Giant Spider");
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
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // Resolve Cruel Edict
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
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
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
        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Should get a may ability prompt
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Triggers when controller's own creature dies")
    void triggersWhenOwnCreatureDies() {
        harness.addToBattlefield(player1, new MimicVat());
        harness.addToBattlefield(player1, new GrizzlyBears());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    // ===== Helpers =====

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
