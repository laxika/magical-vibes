package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({TrueFaithCenser.class, EliteVanguard.class, GrizzlyBears.class})
class TrueFaithCenserTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+1 and vigilance")
    void equippedCreatureGetsBaseBonus() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent censer = addReady(player1, new TrueFaithCenser());
        censer.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Equipped Human gets an additional +1/+0")
    void equippedHumanGetsAdditionalBonus() {
        Permanent human = addReady(player1, new EliteVanguard());
        Permanent censer = addReady(player1, new TrueFaithCenser());
        censer.setAttachedTo(human.getId());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, human, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Equip {2} attaches the Censer to a creature you control")
    void equipAttachesToCreature() {
        Permanent censer = addReady(player1, new TrueFaithCenser());
        Permanent creature = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(censer.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("The Human bonus is lost when the Censer moves to a non-Human")
    void movingCenserRemovesHumanBonus() {
        Permanent censer = addReady(player1, new TrueFaithCenser());
        Permanent human = addReady(player1, new EliteVanguard());
        Permanent creature = addReady(player1, new GrizzlyBears());
        censer.setAttachedTo(human.getId());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(censer.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("An unattached Censer grants no bonuses")
    void unattachedCenserGrantsNoBonuses() {
        Permanent human = addReady(player1, new EliteVanguard());
        Permanent creature = addReady(player1, new GrizzlyBears());
        addReady(player1, new TrueFaithCenser());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, human, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Bonuses apply to an attached Human controlled by an opponent")
    void bonusesApplyAcrossControllers() {
        Permanent censer = addReady(player1, new TrueFaithCenser());
        Permanent human = addReady(player2, new EliteVanguard());
        censer.setAttachedTo(human.getId());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, human, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Multiple Censers each grant both bonuses to a Human")
    void multipleCensersStackBonuses() {
        Permanent human = addReady(player1, new EliteVanguard());
        Permanent first = addReady(player1, new TrueFaithCenser());
        Permanent second = addReady(player1, new TrueFaithCenser());
        first.setAttachedTo(human.getId());
        second.setAttachedTo(human.getId());

        assertThat(gqs.getEffectivePower(gd, human)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, human)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, human, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        Permanent censer = addReady(player1, new TrueFaithCenser());
        Permanent creature = addReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");

        assertThat(censer.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip can be activated only at sorcery speed")
    void cannotEquipDuringOpponentsTurn() {
        addReady(player1, new TrueFaithCenser());
        Permanent creature = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
