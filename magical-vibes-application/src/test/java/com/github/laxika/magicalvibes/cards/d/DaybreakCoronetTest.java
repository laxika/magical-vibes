package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BoundInSilence;
import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.Lifelink;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DaybreakCoronet.class, Lifelink.class, GrizzlyBears.class,
        BoundInSilence.class, BlindPhantasm.class})
class DaybreakCoronetTest extends BaseCardTest {

    @Test
    void grantsBoostAndKeywordsToCreatureWithAnotherAura() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Lifelink(), new DaybreakCoronet()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void cannotEnchantCreatureWithoutAnotherAura() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new DaybreakCoronet()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an enchanted creature");
    }

    @Test
    void bonusesDisappearWhenCoronetLeavesBattlefield() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Lifelink());
        aura.setAttachedTo(creature.getId());

        Permanent coronet = harness.addToBattlefieldAndReturn(player1, new DaybreakCoronet());
        coronet.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(coronet);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void anotherAuraControlledByOpponentAllowsCoronetToEnchant() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BlindPhantasm());
        Permanent opponentAura = harness.addToBattlefieldAndReturn(player2, new BoundInSilence());
        opponentAura.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new DaybreakCoronet()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void grantsLifelinkWhenAnotherAuraDoesNot() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BlindPhantasm());

        harness.setHand(player1, List.of(new BoundInSilence(), new DaybreakCoronet()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    void coronetBecomesIllegalWhenTheOtherAuraLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BlindPhantasm());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new BoundInSilence());
        aura.setAttachedTo(creature.getId());
        Permanent coronet = harness.addToBattlefieldAndReturn(player1, new DaybreakCoronet());
        coronet.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(coronet);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(coronet.getOriginalCard());
    }
}
