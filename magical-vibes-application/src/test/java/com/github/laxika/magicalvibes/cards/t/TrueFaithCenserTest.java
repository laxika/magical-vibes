package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
