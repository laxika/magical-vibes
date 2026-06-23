package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSubtypeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerExilesFromHandEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FiendOfTheShadowsTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has combat damage trigger that exiles from hand and grants controller play permission")
    void hasCombatDamageTrigger() {
        FiendOfTheShadows card = new FiendOfTheShadows();

        List<?> effects = card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER);
        assertThat(effects).hasSize(1);
        assertThat(effects.getFirst()).isInstanceOf(TargetPlayerExilesFromHandEffect.class);

        TargetPlayerExilesFromHandEffect effect = (TargetPlayerExilesFromHandEffect) effects.getFirst();
        assertThat(effect.amount()).isEqualTo(1);
        assertThat(effect.controllerMayPlay()).isTrue();
    }

    @Test
    @DisplayName("Has Sacrifice a Human: Regenerate activated ability with no mana cost")
    void hasRegenerateAbility() {
        FiendOfTheShadows card = new FiendOfTheShadows();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(SacrificeSubtypeCreatureCost.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(RegenerateEffect.class);
    }

    // ===== Combat damage trigger =====

    @Test
    @DisplayName("Combat damage to a player prompts that player to exile a card from their hand")
    void combatDamagePromptsExile() {
        addAttackingFiend(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), createForest())));

        resolveCombat();

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.EXILE_FROM_HAND_CHOICE);
        assertThat(gd.interaction.cardChoice().playerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Damaged player chooses the card to exile and it goes to exile, not graveyard")
    void damagedPlayerExilesChosenCard() {
        addAttackingFiend(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), createForest())));

        resolveCombat();
        harness.handleCardChosen(player2, 0); // player2 chooses to exile Grizzly Bears

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Fiend's controller gains permission to play the exiled card")
    void controllerGainsPlayPermission() {
        addAttackingFiend(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        resolveCombat();
        harness.handleCardChosen(player2, 0);

        Card exiled = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears")).findFirst().orElseThrow();
        assertThat(gd.exilePlayPermissions.get(exiled.getId())).isEqualTo(player1.getId());
        // "for as long as it remains exiled" — not impulse, so it must not expire end of turn
        assertThat(gd.exilePlayPermissionsExpireEndOfTurn).doesNotContain(exiled.getId());
    }

    @Test
    @DisplayName("Controller can play the exiled card even after Fiend of the Shadows leaves the battlefield")
    void permissionPersistsAfterFiendLeaves() {
        addAttackingFiend(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        resolveCombat();
        harness.handleCardChosen(player2, 0);

        Card exiled = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears")).findFirst().orElseThrow();

        // Fiend leaves the battlefield — permission persists for as long as the card is exiled
        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Fiend of the Shadows"));

        assertThat(gd.exilePlayPermissions.get(exiled.getId())).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Controller can actually play the exiled card from exile under their control")
    void controllerPlaysExiledCard() {
        addAttackingFiend(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        resolveCombat();
        harness.handleCardChosen(player2, 0);

        Card exiled = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c.getName().equals("Grizzly Bears")).findFirst().orElseThrow();

        // Move to the controller's postcombat main phase with an empty stack to cast the creature
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castFromExile(player1, exiled.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.findExiledCard(exiled.getId())).isNull();
    }

    @Test
    @DisplayName("No prompt when the damaged player's hand is empty")
    void noPromptWhenHandEmpty() {
        addAttackingFiend(player1);
        harness.setHand(player2, new ArrayList<>());

        resolveCombat();

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no cards to exile"));
    }

    @Test
    @DisplayName("No trigger when Fiend is blocked and deals no combat damage to a player")
    void noTriggerWhenBlocked() {
        Permanent fiend = addAttackingFiend(player1);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setHand(player2, new ArrayList<>(List.of(createForest())));

        resolveCombat();

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.EXILE_FROM_HAND_CHOICE);
        // Note: Fiend has flying so a non-flying/reach blocker is illegal, but this verifies that
        // when no combat damage reaches a player, the trigger does not fire.
    }

    // ===== Sacrifice a Human: Regenerate =====

    @Test
    @DisplayName("Sacrificing a Human grants a regeneration shield to Fiend of the Shadows")
    void sacrificeHumanRegenerates() {
        Permanent fiend = addReadyCreature(player1, new FiendOfTheShadows());
        harness.addToBattlefield(player1, createHumanToken());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Human"));
        assertThat(fiend.getRegenerationShield()).isEqualTo(1);
    }

    // ===== Helpers =====

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addAttackingFiend(Player player) {
        Permanent fiend = addReadyCreature(player, new FiendOfTheShadows());
        fiend.setAttacking(true);
        return fiend;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        // Pass through combat damage and the resulting triggered ability resolution.
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Card createForest() {
        Card card = new Card();
        card.setName("Forest");
        card.setType(CardType.LAND);
        card.setSubtypes(List.of(CardSubtype.FOREST));
        return card;
    }

    private Card createHumanToken() {
        Card card = new Card();
        card.setName("Human");
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.WHITE);
        card.setPower(1);
        card.setToughness(1);
        card.setSubtypes(List.of(CardSubtype.HUMAN));
        return card;
    }
}
