package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BlinkOfAnEye;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.cards.s.ShortSword;
import com.github.laxika.magicalvibes.cards.d.Dub;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Valduk.class, ShortSword.class, Dub.class, BalothGorger.class, BlinkOfAnEye.class})
class ValdukTest extends BaseCardTest {

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }

    private Permanent attachEquipment(Player player, ShortSword equipment, UUID attachToId) {
        Permanent equipPerm = harness.addToBattlefieldAndReturn(player, equipment);
        equipPerm.setAttachedTo(attachToId);
        return equipPerm;
    }

    private Permanent attachAura(Player player, UUID attachToId) {
        Dub aura = new Dub();
        Permanent auraPerm = harness.addToBattlefieldAndReturn(player, aura);
        auraPerm.setAttachedTo(attachToId);
        return auraPerm;
    }

    private List<Permanent> getTokens() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
    }

    @Test
    @DisplayName("No tokens created when no Auras or Equipment are attached")
    void noTokensWhenNoAttachments() {
        harness.addToBattlefield(player1, new Valduk());

        advanceToCombat(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(getTokens()).isEmpty();
    }

    @Test
    @DisplayName("Creates one 3/1 red Elemental token when one Equipment is attached")
    void createsOneTokenWithOneEquipment() {
        harness.addToBattlefield(player1, new Valduk());
        UUID valdukId = harness.getPermanentId(player1, "Valduk, Keeper of the Flame");

        attachEquipment(player1, new ShortSword(), valdukId);

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
        assertThat(token.isTapped()).isFalse();
        assertThat(token.isAttacking()).isFalse();
    }

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

    @Test
    @DisplayName("Creates tokens equal to combined Aura and Equipment count")
    void createsTokensForMixedAttachments() {
        harness.addToBattlefield(player1, new Valduk());
        UUID valdukId = harness.getPermanentId(player1, "Valduk, Keeper of the Flame");

        attachEquipment(player1, new ShortSword(), valdukId);
        attachEquipment(player1, new ShortSword(), valdukId);
        attachAura(player1, valdukId);

        advanceToCombat(player1);
        harness.passBothPriorities(); // resolve trigger

        // 2 Equipment + 1 Aura = 3 tokens
        assertThat(getTokens()).hasSize(3);
    }

    @Test
    @DisplayName("Equipment attached to other creatures does not count")
    void equipmentOnOtherCreatureDoesNotCount() {
        harness.addToBattlefield(player1, new Valduk());
        harness.addToBattlefield(player1, new BalothGorger());
        UUID gorgerId = harness.getPermanentId(player1, "Baloth Gorger");

        attachEquipment(player1, new ShortSword(), gorgerId);

        advanceToCombat(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(getTokens()).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger during opponent's combat")
    void doesNotTriggerDuringOpponentCombat() {
        harness.addToBattlefield(player1, new Valduk());
        UUID valdukId = harness.getPermanentId(player1, "Valduk, Keeper of the Flame");

        attachEquipment(player1, new ShortSword(), valdukId);

        advanceToCombat(player2); // opponent's combat
        harness.passBothPriorities();

        assertThat(getTokens()).isEmpty();
    }

    @Test
    @DisplayName("Created tokens are marked for exile at the beginning of the next end step")
    void tokensMarkedForExileAtEndStep() {
        harness.addToBattlefield(player1, new Valduk());
        UUID valdukId = harness.getPermanentId(player1, "Valduk, Keeper of the Flame");

        attachEquipment(player1, new ShortSword(), valdukId);

        advanceToCombat(player1);
        harness.passBothPriorities(); // resolve trigger

        List<Permanent> tokens = getTokens();
        assertThat(tokens).hasSize(1);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(getTokens()).hasSize(1);
        assertThat(gd.stack).isNotEmpty();
    }

    @Test
    @DisplayName("Tokens are exiled at the beginning of the next end step")
    void tokensExiledAtEndStep() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        harness.addToBattlefield(player1, new Valduk());
        UUID valdukId = harness.getPermanentId(player1, "Valduk, Keeper of the Flame");

        attachEquipment(player1, new ShortSword(), valdukId);

        advanceToCombat(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(getTokens()).hasSize(1);

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        harness.passBothPriorities(); // resolve the delayed exile trigger

        // Tokens should be exiled
        assertThat(getTokens()).isEmpty();
    }

    @Test
    void countsOpponentControlledAura() {
        Permanent valduk = harness.addToBattlefieldAndReturn(player1, new Valduk());
        attachAura(player2, valduk.getId());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(getTokens()).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().isToken());
    }

    @Test
    void countsAttachmentsAtResolutionRatherThanWhenTriggered() {
        Permanent valduk = harness.addToBattlefieldAndReturn(player1, new Valduk());
        Permanent sword = attachEquipment(player1, new ShortSword(), valduk.getId());
        harness.setHand(player1, List.of(new BlinkOfAnEye()));

        advanceToCombat(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, sword.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Short Sword");
        harness.passBothPriorities();

        assertThat(getTokens()).isEmpty();
    }

    @Test
    void usesLastKnownAttachmentsWhenValdukLeavesBeforeResolution() {
        Permanent valduk = harness.addToBattlefieldAndReturn(player1, new Valduk());
        attachEquipment(player1, new ShortSword(), valduk.getId());
        attachAura(player1, valduk.getId());
        harness.setHand(player1, List.of(new BlinkOfAnEye()));

        advanceToCombat(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, valduk.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Valduk, Keeper of the Flame");
        harness.passBothPriorities();

        assertThat(getTokens()).hasSize(2);
    }

    @Test
    void tokensStillExiledAfterValdukLeavesFollowingResolution() {
        Permanent valduk = harness.addToBattlefieldAndReturn(player1, new Valduk());
        attachEquipment(player1, new ShortSword(), valduk.getId());
        harness.setHand(player1, List.of(new BlinkOfAnEye()));

        advanceToCombat(player1);
        harness.passBothPriorities();
        assertThat(getTokens()).hasSize(1);

        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, valduk.getId());
        harness.assertInHand(player1, "Valduk, Keeper of the Flame");
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(getTokens()).hasSize(1);
        harness.passBothPriorities();

        assertThat(getTokens()).isEmpty();
    }
}
