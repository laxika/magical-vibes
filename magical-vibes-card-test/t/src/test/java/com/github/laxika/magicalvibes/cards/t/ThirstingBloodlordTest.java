package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.v.VampireOfTheDireMoon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThirstingBloodlord.class, VampireOfTheDireMoon.class, GrizzlyBears.class, Murder.class})
class ThirstingBloodlordTest extends BaseCardTest {

    @Test
    @DisplayName("Multiple Bloodlords buff each other and their bonuses stack")
    void multipleBloodlordsStack() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new ThirstingBloodlord());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ThirstingBloodlord());
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new VampireOfTheDireMoon());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(3);
    }

    @Test
    @DisplayName("A Vampire entering later receives the bonus, which ends when Bloodlord leaves")
    void bonusUpdatesAsCreaturesEnterAndLeave() {
        Permanent bloodlord = harness.addToBattlefieldAndReturn(player1, new ThirstingBloodlord());
        harness.castFromHand(player1, new VampireOfTheDireMoon(), "{B}");
        harness.passBothPriorities();
        Permanent vampire = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof VampireOfTheDireMoon)
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(2);

        harness.setHand(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, bloodlord.getId());

        harness.assertInGraveyard(player1, "Thirsting Bloodlord");
        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(1);
    }

    @Test
    @DisplayName("Other Vampires you control get +1/+1")
    void buffsOtherVampiresYouControl() {
        harness.addToBattlefield(player1, new ThirstingBloodlord());
        Permanent vampire = harness.addToBattlefieldAndReturn(player1, new VampireOfTheDireMoon());

        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not buff itself, non-Vampires, or opposing Vampires")
    void onlyBuffsOtherOwnVampires() {
        Permanent bloodlord = harness.addToBattlefieldAndReturn(player1, new ThirstingBloodlord());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentVampire = harness.addToBattlefieldAndReturn(player2, new VampireOfTheDireMoon());

        assertThat(gqs.getEffectivePower(gd, bloodlord)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bloodlord)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentVampire)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, opponentVampire)).isEqualTo(1);
    }
}
