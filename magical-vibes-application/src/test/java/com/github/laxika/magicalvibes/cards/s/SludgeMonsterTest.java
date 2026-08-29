package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DauthiHorror;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SludgeMonster.class, AirElemental.class, DauthiHorror.class, FountainOfYouth.class})
class SludgeMonsterTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a slime counter on another creature and turns it into a 2/2 without abilities")
    void etbSlimesAnotherCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new SludgeMonster()));
        addSludgeMonsterMana();

        harness.castCreature(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.SLIME)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Attack puts a slime counter on another target creature")
    void attackSlimesAnotherCreature() {
        addCreatureReady(player1, new SludgeMonster());
        Permanent target = addCreatureReady(player1, new AirElemental());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.SLIME)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Horrors with slime counters are not affected")
    void horrorsAreNotAffected() {
        Permanent horror = addCreatureReady(player2, new DauthiHorror());
        horror.setCounterCount(CounterType.SLIME, 1);
        int originalPower = gqs.getEffectivePower(gd, horror);
        int originalToughness = gqs.getEffectiveToughness(gd, horror);
        addCreatureReady(player1, new SludgeMonster());

        assertThat(gqs.getEffectivePower(gd, horror)).isEqualTo(originalPower);
        assertThat(gqs.getEffectiveToughness(gd, horror)).isEqualTo(originalToughness);
        assertThat(gqs.hasKeyword(gd, horror, Keyword.SHADOW)).isTrue();
    }

    @Test
    @DisplayName("The static effect ends when Sludge Monster leaves the battlefield")
    void staticEffectEndsWhenSourceLeaves() {
        Permanent sludgeMonster = addCreatureReady(player1, new SludgeMonster());
        Permanent target = addCreatureReady(player2, new AirElemental());
        target.setCounterCount(CounterType.SLIME, 1);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(sludgeMonster);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The ETB trigger cannot target a noncreature permanent")
    void etbRejectsNoncreatureTarget() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new SludgeMonster()));
        addSludgeMonsterMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature");
    }

    private void addSludgeMonsterMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
