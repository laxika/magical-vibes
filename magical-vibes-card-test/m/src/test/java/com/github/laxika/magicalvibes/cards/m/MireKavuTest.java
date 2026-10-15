package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MireKavu.class, Forest.class, Swamp.class})
class MireKavuTest extends BaseCardTest {

    @Test
    void noBoostWithoutSwamp() {
        Permanent mireKavu = harness.addToBattlefieldAndReturn(player1, new MireKavu());
        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, mireKavu)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mireKavu)).isEqualTo(2);
    }

    @Test
    void getsBoostWithSwamp() {
        Permanent mireKavu = harness.addToBattlefieldAndReturn(player1, new MireKavu());
        harness.addToBattlefield(player1, new Swamp());

        assertThat(gqs.getEffectivePower(gd, mireKavu)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mireKavu)).isEqualTo(3);
    }

    @Test
    void opponentSwampDoesNotGrantBoost() {
        Permanent mireKavu = harness.addToBattlefieldAndReturn(player1, new MireKavu());
        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectivePower(gd, mireKavu)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mireKavu)).isEqualTo(2);
    }

    @Test
    void losesBoostWhenSwampLeaves() {
        Permanent mireKavu = harness.addToBattlefieldAndReturn(player1, new MireKavu());
        harness.addToBattlefield(player1, new Swamp());

        assertThat(gqs.getEffectivePower(gd, mireKavu)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mireKavu)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(permanent -> permanent.getCard().getName().equals("Swamp"));

        assertThat(gqs.getEffectivePower(gd, mireKavu)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mireKavu)).isEqualTo(2);
    }

    @Test
    void gainsBoostWhenSwampEntersLater() {
        Permanent mireKavu = harness.addToBattlefieldAndReturn(player1, new MireKavu());

        assertThat(gqs.getEffectivePower(gd, mireKavu)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mireKavu)).isEqualTo(2);

        harness.addToBattlefield(player1, new Swamp());

        assertThat(gqs.getEffectivePower(gd, mireKavu)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mireKavu)).isEqualTo(3);
    }

    @Test
    void multipleSwampsGrantOnlyOneBoostAndOneRemainingSwampKeepsIt() {
        Permanent mireKavu = harness.addToBattlefieldAndReturn(player1, new MireKavu());
        Permanent firstSwamp = harness.addToBattlefieldAndReturn(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());

        assertThat(gqs.getEffectivePower(gd, mireKavu)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mireKavu)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(firstSwamp);

        assertThat(gqs.getEffectivePower(gd, mireKavu)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, mireKavu)).isEqualTo(3);
    }

    @Test
    void swampInHandOrGraveyardDoesNotGrantBoost() {
        Permanent mireKavu = harness.addToBattlefieldAndReturn(player1, new MireKavu());
        harness.setHand(player1, List.of(new Swamp()));
        harness.setGraveyard(player1, List.of(new Swamp()));

        assertThat(gqs.getEffectivePower(gd, mireKavu)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, mireKavu)).isEqualTo(2);
    }
}
