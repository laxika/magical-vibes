package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhantomWarrior;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LordOfTheUnrealTest extends BaseCardTest {

    @Test
    @DisplayName("Illusion creatures you control get +1/+1 and have hexproof")
    void boostsAndProtectsOwnIllusions() {
        harness.addToBattlefield(player1, new LordOfTheUnreal());
        harness.addToBattlefield(player1, new PhantomWarrior());

        Permanent warrior = findPermanent(player1, "Phantom Warrior");

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, warrior)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Non-Illusion creatures you control are unaffected")
    void doesNotAffectNonIllusions() {
        harness.addToBattlefield(player1, new LordOfTheUnreal());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Lord of the Unreal itself is not an Illusion and gets no bonus")
    void doesNotBoostItself() {
        harness.addToBattlefield(player1, new LordOfTheUnreal());

        Permanent lord = findPermanent(player1, "Lord of the Unreal");

        assertThat(gqs.getEffectivePower(gd, lord)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lord)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, lord, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Opponent's Illusions get no bonus and no hexproof")
    void doesNotAffectOpponentIllusions() {
        harness.addToBattlefield(player1, new LordOfTheUnreal());
        harness.addToBattlefield(player2, new PhantomWarrior());

        Permanent warrior = findPermanent(player2, "Phantom Warrior");

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("Opponent cannot target a granted-hexproof Illusion")
    void opponentCannotTargetProtectedIllusion() {
        harness.addToBattlefield(player1, new LordOfTheUnreal());
        harness.addToBattlefield(player1, new PhantomWarrior());

        Permanent warrior = findPermanent(player1, "Phantom Warrior");

        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.passPriority(player1);

        assertThatThrownBy(() -> gs.playCard(gd, player2, 0, 0, warrior.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Illusions lose the bonus when Lord of the Unreal leaves the battlefield")
    void bonusEndsWhenLordLeaves() {
        harness.addToBattlefield(player1, new LordOfTheUnreal());
        harness.addToBattlefield(player1, new PhantomWarrior());

        Permanent warrior = findPermanent(player1, "Phantom Warrior");
        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Lord of the Unreal"));

        assertThat(gqs.getEffectivePower(gd, warrior)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.HEXPROOF)).isFalse();
    }
}
