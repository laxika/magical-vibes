package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhantomWarrior;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LordOfTheUnreal.class, PhantomWarrior.class, GrizzlyBears.class, Shock.class})
class LordOfTheUnrealTest extends BaseCardTest {

    @Test
    @DisplayName("Illusion creatures you control get +1/+1 and have hexproof")
    void boostsAndProtectsOwnIllusions() {
        harness.addToBattlefield(player1, new LordOfTheUnreal());
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new PhantomWarrior());

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Non-Illusion creatures you control are unaffected")
    void doesNotAffectNonIllusions() {
        harness.addToBattlefield(player1, new LordOfTheUnreal());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Lord of the Unreal itself is not an Illusion and gets no bonus")
    void doesNotBoostItself() {
        Permanent lord = harness.addToBattlefieldAndReturn(player1, new LordOfTheUnreal());

        assertThat(gqs.getEffectivePower(gd, lord)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lord)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lord, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Opponent's Illusions get no bonus and no hexproof")
    void doesNotAffectOpponentIllusions() {
        harness.addToBattlefield(player1, new LordOfTheUnreal());
        Permanent warrior = harness.addToBattlefieldAndReturn(player2, new PhantomWarrior());

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Opponent cannot target a granted-hexproof Illusion")
    void opponentCannotTargetProtectedIllusion() {
        harness.addToBattlefield(player1, new LordOfTheUnreal());
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new PhantomWarrior());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, warrior.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Illusions lose the bonus when Lord of the Unreal leaves the battlefield")
    void bonusEndsWhenLordLeaves() {
        harness.addToBattlefield(player1, new LordOfTheUnreal());
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new PhantomWarrior());
        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Lord of the Unreal"));

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("The controller can target their protected Illusion")
    void controllerCanTargetProtectedIllusion() {
        harness.addToBattlefield(player1, new LordOfTheUnreal());
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new PhantomWarrior());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, warrior.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(warrior);
        assertThat(warrior.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple Lords stack their bonuses and one remaining Lord still grants hexproof")
    void multipleLordsStackBonuses() {
        Permanent firstLord = harness.addToBattlefieldAndReturn(player1, new LordOfTheUnreal());
        harness.addToBattlefield(player1, new LordOfTheUnreal());
        Permanent warrior = harness.addToBattlefieldAndReturn(player1, new PhantomWarrior());

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.HEXPROOF)).isTrue();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, firstLord.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(firstLord);
        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.HEXPROOF)).isTrue();
    }
}
