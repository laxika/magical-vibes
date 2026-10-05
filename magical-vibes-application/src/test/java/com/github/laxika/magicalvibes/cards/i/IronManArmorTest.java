package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.Gigantiform;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.t.Threaten;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IronManArmor.class, GrizzlyBears.class, Spellbook.class, Gigantiform.class, Threaten.class})
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

    @Test
    void equipAttachesAndTransfersBonuses() {
        Permanent armor = addReady(player1, new IronManArmor());
        Permanent first = addReady(player1, new GrizzlyBears());
        Permanent second = addReady(player1, new GrizzlyBears());
        armor.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, second.getId());
        harness.passBothPriorities();

        assertThat(armor.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();
    }

    @Test
    void equipCannotTargetOpposingCreature() {
        addReady(player1, new IronManArmor());
        Permanent opposingCreature = addReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, opposingCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void artifactBonusTracksNewArtifactsAndExcludesOpponentsArtifacts() {
        Permanent armor = addReady(player1, new IronManArmor());
        addReady(player2, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, armor)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, armor)).isEqualTo(1);

        harness.setHand(player1, List.of(new Spellbook()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, armor)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, armor)).isEqualTo(2);
    }

    @Test
    void activatingWhileAlreadyCreatureDoesNotGrantAnotherArtifactBonus() {
        Permanent armor = addReady(player1, new IronManArmor());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, armor)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, armor)).isEqualTo(1);
    }

    @Test
    @CardUsed({IronManArmor.class, Spellbook.class, Gigantiform.class})
    void artifactBonusAppliesOnTopOfLaterBasePowerAndToughness() {
        Permanent armor = addReady(player1, new IronManArmor());
        addReady(player1, new Spellbook());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Gigantiform()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, armor.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, armor)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, armor)).isEqualTo(10);
    }

    @Test
    @CardUsed({IronManArmor.class, Spellbook.class, Threaten.class})
    void artifactBonusUsesCurrentControllersArtifacts() {
        Permanent armor = addReady(player2, new IronManArmor());
        addReady(player2, new Spellbook());
        addReady(player2, new Spellbook());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Threaten()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, armor.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(armor);
        assertThat(gqs.getEffectivePower(gd, armor)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, armor)).isEqualTo(1);
    }

    private Permanent addReady(Player player, Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
