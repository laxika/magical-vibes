package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateTokenPerAttachmentOnSourceEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ValdukTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to BEGINNING_OF_COMBAT, triggers fire
    }

    private Permanent attachEquipment(Player player, LeoninScimitar equipment, UUID attachToId) {
        Permanent equipPerm = new Permanent(equipment);
        equipPerm.setAttachedTo(attachToId);
        gd.playerBattlefields.get(player.getId()).add(equipPerm);
        return equipPerm;
    }

    private Permanent attachAura(Player player, UUID attachToId) {
        VolcanicStrength aura = new VolcanicStrength();
        Permanent auraPerm = new Permanent(aura);
        auraPerm.setAttachedTo(attachToId);
        gd.playerBattlefields.get(player.getId()).add(auraPerm);
        return auraPerm;
    }

    private List<Permanent> getTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Valduk has beginning-of-combat triggered CreateTokenPerAttachmentOnSourceEffect")
    void hasCorrectEffect() {
        Valduk card = new Valduk();

        assertThat(card.getEffects(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED).getFirst())
                .isInstanceOf(CreateTokenPerAttachmentOnSourceEffect.class);

        CreateTokenPerAttachmentOnSourceEffect effect =
                (CreateTokenPerAttachmentOnSourceEffect) card.getEffects(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED).getFirst();
        assertThat(effect.countAuras()).isTrue();
        assertThat(effect.countEquipment()).isTrue();
        assertThat(effect.exileAtEndStep()).isTrue();
        assertThat(effect.tokenName()).isEqualTo("Elemental");
        assertThat(effect.power()).isEqualTo(3);
        assertThat(effect.toughness()).isEqualTo(1);
        assertThat(effect.color()).isEqualTo(CardColor.RED);
        assertThat(effect.keywords()).containsExactlyInAnyOrder(Keyword.TRAMPLE, Keyword.HASTE);
    }

    // ===== No attachments =====

    @Test
    @DisplayName("No tokens created when no Auras or Equipment are attached")
    void noTokensWhenNoAttachments() {
        harness.addToBattlefield(player1, new Valduk());

        advanceToCombat(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(getTokens()).isEmpty();
    }

    // ===== One Equipment attached =====

    @Test
    @DisplayName("Creates one 3/1 red Elemental token when one Equipment is attached")
    void createsOneTokenWithOneEquipment() {
        harness.addToBattlefield(player1, new Valduk());
        UUID valdukId = harness.getPermanentId(player1, "Valduk, Keeper of the Flame");

        attachEquipment(player1, new LeoninScimitar(), valdukId);

        advanceToCombat(player1);
        harness.passBothPriorities(); // resolve trigger

        List<Permanent> tokens = getTokens();
        assertThat(tokens).hasSize(1);

        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getName()).isEqualTo("Elemental");
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getKeywords()).containsExactlyInAnyOrder(Keyword.TRAMPLE, Keyword.HASTE);
    }

    // ===== One Aura attached =====

    @Test
    @DisplayName("Creates one token when one Aura is attached")
    void createsOneTokenWithOneAura() {
        harness.addToBattlefield(player1, new Valduk());
        UUID valdukId = harness.getPermanentId(player1, "Valduk, Keeper of the Flame");

        attachAura(player1, valdukId);

        advanceToCombat(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(getTokens()).hasSize(1);
    }

    // ===== Multiple attachments (mixed) =====

    @Test
    @DisplayName("Creates tokens equal to combined Aura and Equipment count")
    void createsTokensForMixedAttachments() {
        harness.addToBattlefield(player1, new Valduk());
        UUID valdukId = harness.getPermanentId(player1, "Valduk, Keeper of the Flame");

        attachEquipment(player1, new LeoninScimitar(), valdukId);
        attachEquipment(player1, new LeoninScimitar(), valdukId);
        attachAura(player1, valdukId);

        advanceToCombat(player1);
        harness.passBothPriorities(); // resolve trigger

        // 2 Equipment + 1 Aura = 3 tokens
        assertThat(getTokens()).hasSize(3);
    }

    // ===== Equipment on other creatures doesn't count =====

    @Test
    @DisplayName("Equipment attached to other creatures does not count")
    void equipmentOnOtherCreatureDoesNotCount() {
        harness.addToBattlefield(player1, new Valduk());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        attachEquipment(player1, new LeoninScimitar(), bearsId);

        advanceToCombat(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(getTokens()).isEmpty();
    }

    // ===== Does not trigger during opponent's combat =====

    @Test
    @DisplayName("Does not trigger during opponent's combat")
    void doesNotTriggerDuringOpponentCombat() {
        harness.addToBattlefield(player1, new Valduk());
        UUID valdukId = harness.getPermanentId(player1, "Valduk, Keeper of the Flame");

        attachEquipment(player1, new LeoninScimitar(), valdukId);

        advanceToCombat(player2); // opponent's combat
        harness.passBothPriorities();

        assertThat(getTokens()).isEmpty();
    }

    // ===== Tokens are marked for exile at end step =====

    @Test
    @DisplayName("Created tokens are marked for exile at the beginning of the next end step")
    void tokensMarkedForExileAtEndStep() {
        harness.addToBattlefield(player1, new Valduk());
        UUID valdukId = harness.getPermanentId(player1, "Valduk, Keeper of the Flame");

        attachEquipment(player1, new LeoninScimitar(), valdukId);

        advanceToCombat(player1);
        harness.passBothPriorities(); // resolve trigger

        List<Permanent> tokens = getTokens();
        assertThat(tokens).hasSize(1);
        assertThat(gd.pendingTokenExilesAtEndStep).contains(tokens.getFirst().getId());
    }

    // ===== Tokens are exiled at end step =====

    @Test
    @DisplayName("Tokens are exiled at the beginning of the next end step")
    void tokensExiledAtEndStep() {
        // Clear hands so auto-pass can cascade from POSTCOMBAT_MAIN to END_STEP
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        harness.addToBattlefield(player1, new Valduk());
        UUID valdukId = harness.getPermanentId(player1, "Valduk, Keeper of the Flame");

        attachEquipment(player1, new LeoninScimitar(), valdukId);

        advanceToCombat(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(getTokens()).hasSize(1);
        assertThat(gd.pendingTokenExilesAtEndStep).isNotEmpty();

        // Advance to end step to trigger exile (clear interaction from declare-attackers prompt)
        gd.interaction.clearAwaitingInput();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to END_STEP, fires handleEndStepTriggers

        // Tokens should be exiled
        assertThat(getTokens()).isEmpty();
    }
}
