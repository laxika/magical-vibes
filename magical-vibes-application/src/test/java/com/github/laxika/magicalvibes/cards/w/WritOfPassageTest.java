package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AssaultZeppelid;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.GameStateMessage;
import com.github.laxika.magicalvibes.service.JacksonConfig;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WritOfPassage.class, MistralCharger.class, AssaultZeppelid.class})
class WritOfPassageTest extends BaseCardTest {

    @Test
    @DisplayName("The attack trigger makes an enchanted small creature unblockable")
    void attackTriggerMakesEnchantedSmallCreatureUnblockable() {
        Permanent attacker = addCreatureReady(player2, new MistralCharger());
        addAuraOn(attacker, player1);

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(attacker.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Casting Writ of Passage attaches it to a target creature")
    void castingAttachesAuraToTargetCreature() {
        Permanent target = addCreatureReady(player2, new MistralCharger());
        WritOfPassage writ = new WritOfPassage();
        harness.setHand(player1, List.of(writ));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(aura -> aura.getCard() == writ
                        && aura.isAttached()
                        && aura.getAttachedTo().equals(target.getId()));
    }

    @Test
    @DisplayName("Writ of Passage cannot enchant a noncreature permanent")
    void cannotEnchantNoncreaturePermanent() {
        Permanent noncreature = harness.addToBattlefieldAndReturn(player2, new WritOfPassage());
        harness.setHand(player1, List.of(new WritOfPassage()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The attack trigger ignores a small creature that is not enchanted")
    void attackTriggerOnlyAffectsEnchantedCreature() {
        Permanent enchantedAttacker = addCreatureReady(player2, new MistralCharger());
        Permanent otherAttacker = addCreatureReady(player2, new MistralCharger());
        addAuraOn(enchantedAttacker, player1);

        declareAttackers(player2, List.of(0, 1));

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(enchantedAttacker.isCantBeBlocked()).isTrue();
        assertThat(otherAttacker.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The attack trigger does not trigger for an enchanted creature with power 3")
    void attackTriggerRequiresPowerTwoOrLess() {
        Permanent attacker = addCreatureReady(player2, new AssaultZeppelid());
        addAuraOn(attacker, player1);

        declareAttackers(player2, List.of(0));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The attack trigger checks power again when it resolves")
    void attackTriggerRechecksPowerOnResolution() {
        Permanent attacker = addCreatureReady(player2, new MistralCharger());
        addAuraOn(attacker, player1);

        declareAttackers(player2, List.of(0));
        attacker.setPowerModifier(1);
        harness.passBothPriorities();

        assertThat(attacker.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The attack trigger rechecks power using the Aura's last-known attachment")
    void attackTriggerRechecksPowerAfterAuraLeaves() {
        Permanent attacker = addCreatureReady(player2, new MistralCharger());
        Permanent aura = addAuraOn(attacker, player1);

        declareAttackers(player2, List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        attacker.setPowerModifier(1);
        harness.passBothPriorities();

        assertThat(attacker.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("The attack trigger's unblockable effect wears off at end of turn")
    void attackTriggerUnblockableWearsOffAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player2, new MistralCharger());
        addAuraOn(attacker, player1);

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        assertThat(attacker.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Forecast makes a target small creature unblockable and keeps the card in hand")
    void forecastMakesTargetUnblockableAndKeepsSourceInHand() {
        Permanent target = addCreatureReady(player2, new MistralCharger());
        WritOfPassage writ = new WritOfPassage();
        harness.setHand(player1, List.of(writ));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, target.getId());

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(writ);
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Forecast can be activated only once during its controller's upkeep")
    void forecastIsLimitedToOncePerTurn() {
        Permanent firstTarget = addCreatureReady(player2, new MistralCharger());
        Permanent secondTarget = addCreatureReady(player2, new MistralCharger());
        harness.setHand(player1, List.of(new WritOfPassage()));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, firstTarget.getId());

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, secondTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }

    @Test
    @DisplayName("Forecast requires a creature with power 2 or less during the controller's upkeep")
    void forecastRequiresSmallCreatureAndUpkeep() {
        Permanent smallTarget = addCreatureReady(player2, new MistralCharger());
        Permanent largeTarget = addCreatureReady(player2, new AssaultZeppelid());
        WritOfPassage writ = new WritOfPassage();
        harness.setHand(player1, List.of(writ));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, largeTarget.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, smallTarget.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only be activated during your upkeep");
    }

    @Test
    void forecastTargetBecomingTooLargeBeforeResolutionIsIllegal() {
        Permanent target = addCreatureReady(player2, new MistralCharger());
        harness.setHand(player1, List.of(new WritOfPassage()));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        target.setPowerModifier(1);
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void forecastCannotBeActivatedDuringOpponentsUpkeep() {
        Permanent target = addCreatureReady(player1, new MistralCharger());
        harness.setHand(player1, List.of(new WritOfPassage()));
        advanceToUpkeep(player2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only be activated during your upkeep");
    }

    @Test
    void forecastUnblockablePersistsAfterPowerIncreaseAndExpiresAtEndOfTurn() {
        Permanent target = addCreatureReady(player1, new MistralCharger());
        harness.setHand(player1, List.of(new WritOfPassage()));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();
        target.setPowerModifier(1);
        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    void forecastCardRemainsRevealedThroughoutUpkeep() throws Exception {
        Permanent target = addCreatureReady(player1, new MistralCharger());
        WritOfPassage writ = new WritOfPassage();
        harness.setHand(player1, List.of(writ));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.clearMessages();
        harness.publishState();

        var mapper = new JacksonConfig().objectMapper();
        GameStateMessage state = mapper.readValue(harness.getConn2()
                .getMessagesContaining("\"type\":\"GAME_STATE\"").getLast(), GameStateMessage.class);
        assertThat(state.opponentHand()).extracting(card -> card.id()).containsExactly(writ.getId());

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearMessages();
        harness.publishState();
        GameStateMessage mainPhaseState = mapper.readValue(harness.getConn2()
                .getMessagesContaining("\"type\":\"GAME_STATE\"").getLast(), GameStateMessage.class);
        assertThat(mainPhaseState.opponentHand()).isEmpty();
    }

    @Test
    void attackTriggerStillResolvesAfterAuraLeavesWithPowerTwo() {
        Permanent attacker = addCreatureReady(player2, new MistralCharger());
        Permanent aura = addAuraOn(attacker, player1);

        declareAttackers(player2, List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.passBothPriorities();

        assertThat(attacker.isCantBeBlocked()).isTrue();
    }

    private Permanent addAuraOn(Permanent host, Player controller) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new WritOfPassage());
        aura.setAttachedTo(host.getId());
        return aura;
    }
}
