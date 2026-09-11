package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IronManArmor.class, GrizzlyBears.class, Spellbook.class})
class IronManArmorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB attaches Iron Man Armor and boosts the equipped creature")
    void etbAttachesAndBoostsCreature() {
        Permanent bears = addReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new IronManArmor()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent armor = findPermanent(player1, "Iron Man Armor");
        assertThat(armor.getAttachedTo()).isEqualTo(bears.getId());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Animation makes Iron Man Armor a flying Construct Hero with artifact-count power and toughness")
    void animationMakesArtifactCreature() {
        Permanent armor = addReady(player1, new IronManArmor());
        Permanent bears = addReady(player1, new GrizzlyBears());
        armor.setAttachedTo(bears.getId());
        addReady(player1, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, armor)).isTrue();
        assertThat(gqs.isArtifact(armor)).isTrue();
        assertThat(armor.getAttachedTo()).isNull();
        assertThat(gqs.getEffectivePower(gd, armor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, armor)).isEqualTo(2);
        assertThat(armor.getTransientSubtypes()).contains(CardSubtype.CONSTRUCT, CardSubtype.HERO);
        assertThat(gqs.hasKeyword(gd, armor, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Animation wears off at end of turn")
    void animationWearsOffAtEndOfTurn() {
        Permanent armor = addReady(player1, new IronManArmor());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, armor)).isFalse();
        assertThat(armor.getTransientSubtypes()).isEmpty();
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
