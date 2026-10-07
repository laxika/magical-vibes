package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LurkingLizards;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiderSuit.class, LurkingLizards.class})
class SpiderSuitTest extends BaseCardTest {

    @Test
    @DisplayName("Equip {3} attaches Spider-Suit to a creature you control")
    void equipAttachesToCreature() {
        Permanent suit = addSuitReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(suit.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature gets +2/+2 and becomes a Spider Hero")
    void equippedCreatureGetsBoostAndSubtypes() {
        Permanent creature = addCreatureReady(player1);
        Permanent suit = addSuitReady(player1);
        suit.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.effectiveCreatureSubtypes(gd, creature))
                .contains(CardSubtype.SPIDER, CardSubtype.HERO);
    }

    @Test
    @DisplayName("Spider-Suit's bonuses disappear when it is unattached")
    void bonusesDisappearWhenUnattached() {
        Permanent creature = addCreatureReady(player1);
        Permanent suit = addSuitReady(player1);
        suit.setAttachedTo(creature.getId());
        suit.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, creature))
                .doesNotContain(CardSubtype.SPIDER, CardSubtype.HERO);
    }

    @Test
    @DisplayName("Equip transfers all bonuses while preserving the creatures' original types")
    void equipTransfersBonusesAndPreservesTypes() {
        Permanent suit = addSuitReady(player1);
        Permanent first = addCreatureReady(player1);
        Permanent second = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(5);
        assertThat(gqs.effectiveCreatureSubtypes(gd, first))
                .contains(CardSubtype.LIZARD, CardSubtype.VILLAIN, CardSubtype.SPIDER, CardSubtype.HERO);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, second))
                .doesNotContain(CardSubtype.SPIDER, CardSubtype.HERO);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(suit.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, first))
                .contains(CardSubtype.LIZARD, CardSubtype.VILLAIN)
                .doesNotContain(CardSubtype.SPIDER, CardSubtype.HERO);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(5);
        assertThat(gqs.effectiveCreatureSubtypes(gd, second))
                .contains(CardSubtype.LIZARD, CardSubtype.VILLAIN, CardSubtype.SPIDER, CardSubtype.HERO);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Multiple Spider-Suits provide cumulative power and toughness bonuses")
    void multipleSuitsStackBonuses() {
        Permanent firstSuit = addSuitReady(player1);
        Permanent secondSuit = addSuitReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, creature.getId());
        harness.passBothPriorities();

        assertThat(firstSuit.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(secondSuit.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(7);
        assertThat(gqs.effectiveCreatureSubtypes(gd, creature))
                .contains(CardSubtype.LIZARD, CardSubtype.VILLAIN, CardSubtype.SPIDER, CardSubtype.HERO);
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipRejectsOpponentsCreature() {
        Permanent suit = addSuitReady(player1);
        Permanent creature = addCreatureReady(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");

        assertThat(suit.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip requires three mana")
    void equipRejectsInsufficientMana() {
        Permanent suit = addSuitReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(suit.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An equip target leaving the battlefield does not detach the current wearer")
    void vanishedEquipTargetLeavesOriginalAttachment() {
        Permanent suit = addSuitReady(player1);
        Permanent original = addCreatureReady(player1);
        Permanent target = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.activateAbility(player1, 0, null, original.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(suit.getAttachedTo()).isEqualTo(original.getId());
        assertThat(gqs.getEffectivePower(gd, original)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, original)).isEqualTo(5);
        assertThat(gqs.effectiveCreatureSubtypes(gd, original))
                .contains(CardSubtype.SPIDER, CardSubtype.HERO);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void equipRejectsCombatTiming() {
        addSuitReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
    }

    @Test
    @DisplayName("Equip cannot be activated during an opponent's turn")
    void equipRejectsOpponentsTurn() {
        addSuitReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("Equip requires an empty stack and grants bonuses only on resolution")
    void equipWaitsForResolutionAndRequiresEmptyStack() {
        Permanent suit = addSuitReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, creature.getId());

        assertThat(suit.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, creature))
                .doesNotContain(CardSubtype.SPIDER, CardSubtype.HERO);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");

        harness.passBothPriorities();

        assertThat(suit.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.effectiveCreatureSubtypes(gd, creature))
                .contains(CardSubtype.SPIDER, CardSubtype.HERO);
    }

    private Permanent addSuitReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new SpiderSuit());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addCreatureReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new LurkingLizards());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
