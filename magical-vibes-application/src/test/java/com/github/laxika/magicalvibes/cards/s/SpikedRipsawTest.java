package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DawnhartDisciple;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpikedRipsaw.class, Forest.class, DawnhartDisciple.class})
class SpikedRipsawTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +3/+3")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new DawnhartDisciple());
        Permanent ripsaw = addCreatureReady(player1, new SpikedRipsaw());
        ripsaw.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Attacking offers sacrificing a Forest")
    void attackingOffersSacrificingForest() {
        Permanent creature = addCreatureReady(player1, new DawnhartDisciple());
        Permanent ripsaw = addCreatureReady(player1, new SpikedRipsaw());
        ripsaw.setAttachedTo(creature.getId());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(forest.getId());
    }

    @Test
    @DisplayName("Sacrificing a Forest gives the equipped creature trample until end of turn")
    void sacrificingForestGrantsTrample() {
        Permanent creature = addCreatureReady(player1, new DawnhartDisciple());
        Permanent ripsaw = addCreatureReady(player1, new SpikedRipsaw());
        ripsaw.setAttachedTo(creature.getId());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest.getCard());
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Declining the Forest sacrifice does not grant trample")
    void decliningSacrificeDoesNothing() {
        Permanent creature = addCreatureReady(player1, new DawnhartDisciple());
        Permanent ripsaw = addCreatureReady(player1, new SpikedRipsaw());
        ripsaw.setAttachedTo(creature.getId());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(forest);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger does not fire while the Equipment is unattached")
    void noTriggerWhenUnattached() {
        addCreatureReady(player1, new DawnhartDisciple());
        addCreatureReady(player1, new SpikedRipsaw());
        harness.addToBattlefieldAndReturn(player1, new Forest());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip attaches to a controlled creature for three mana")
    void equipAttachesToControlledCreature() {
        Permanent creature = addCreatureReady(player1, new DawnhartDisciple());
        Permanent ripsaw = addCreatureReady(player1, new SpikedRipsaw());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(ripsaw.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("A Forest controlled by the opponent cannot pay for trample")
    void cannotSacrificeOpponentsForest() {
        Permanent creature = addCreatureReady(player1, new DawnhartDisciple());
        Permanent ripsaw = addCreatureReady(player1, new SpikedRipsaw());
        ripsaw.setAttachedTo(creature.getId());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(forest);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrificing a Forest grants trample during the original ability's resolution")
    void trampleIsGrantedWithoutAnotherPriorityRound() {
        Permanent creature = addCreatureReady(player1, new DawnhartDisciple());
        Permanent ripsaw = addCreatureReady(player1, new SpikedRipsaw());
        ripsaw.setAttachedTo(creature.getId());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, forest.getId());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Changing the attachment before resolution does not change which creature gains trample")
    void originalAttackerGainsTrampleAfterAttachmentChanges() {
        Permanent attacker = addCreatureReady(player1, new DawnhartDisciple());
        Permanent otherCreature = addCreatureReady(player1, new DawnhartDisciple());
        Permanent ripsaw = addCreatureReady(player1, new SpikedRipsaw());
        ripsaw.setAttachedTo(attacker.getId());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        declareAttackers(List.of(0));
        ripsaw.setAttachedTo(otherCreature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The attacking creature still gains trample if Ripsaw leaves before resolution")
    void attackerGainsTrampleAfterEquipmentLeaves() {
        Permanent creature = addCreatureReady(player1, new DawnhartDisciple());
        Permanent ripsaw = addCreatureReady(player1, new SpikedRipsaw());
        ripsaw.setAttachedTo(creature.getId());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        declareAttackers(List.of(0));
        gd.playerBattlefields.get(player1.getId()).remove(ripsaw);
        gd.playerGraveyards.get(player1.getId()).add(ripsaw.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }
    @Test
    @DisplayName("The trample granted by the sacrifice expires at end of turn")
    void trampleExpiresAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new DawnhartDisciple());
        Permanent ripsaw = addCreatureReady(player1, new SpikedRipsaw());
        ripsaw.setAttachedTo(creature.getId());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, forest.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipCannotTargetOpponentsCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new DawnhartDisciple());
        Permanent ripsaw = addCreatureReady(player1, new SpikedRipsaw());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ripsaw.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip requires sorcery timing")
    void equipCannotBeActivatedDuringCombat() {
        Permanent creature = addCreatureReady(player1, new DawnhartDisciple());
        Permanent ripsaw = addCreatureReady(player1, new SpikedRipsaw());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(ripsaw.getAttachedTo()).isNull();
    }}
