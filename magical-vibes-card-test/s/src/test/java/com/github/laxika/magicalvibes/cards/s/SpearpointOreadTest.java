package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpearpointOread.class, GrizzlyBears.class})
class SpearpointOreadTest extends BaseCardTest {

    @Test
    @DisplayName("Spearpoint Oread can be cast normally as a creature")
    void castsNormallyAsCreature() {
        harness.setHand(player1, List.of(new SpearpointOread()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent oread = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gqs.isCreature(gd, oread)).isTrue();
        assertThat(gqs.hasKeyword(gd, oread, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Spearpoint Oread can be cast for bestow and boosts the enchanted creature")
    void castsForBestow() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpearpointOread()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();

        Permanent oread = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Spearpoint Oread"));
        assertThat(gqs.isCreature(gd, oread)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("A bestowed Spearpoint Oread becomes a creature when its host leaves")
    void becomesCreatureWhenHostLeaves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpearpointOread()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castWithAlternateCost(player1, 0, bear.getId());
        harness.passBothPriorities();
        Permanent oread = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Spearpoint Oread"));

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bear));
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(oread);
        assertThat(gqs.isCreature(gd, oread)).isTrue();
        assertThat(oread.isAttached()).isFalse();
    }

    @Test
    @DisplayName("Bestow resolves as a creature when its target leaves before resolution")
    void resolvesAsCreatureWhenTargetLeaves() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpearpointOread()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, host));
        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent oread = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Spearpoint Oread"));
        assertThat(gqs.isCreature(gd, oread)).isTrue();
        assertThat(oread.isAttached()).isFalse();
        assertThat(gqs.hasKeyword(gd, oread, Keyword.FIRST_STRIKE)).isTrue();
        harness.assertNotInGraveyard(player1, "Spearpoint Oread");
    }

    @Test
    @DisplayName("Bestow can enchant an opposing creature and only grants its host the bonuses")
    void bestowsOnOpposingCreature() {
        Permanent host = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpearpointOread()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();

        Permanent oread = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Spearpoint Oread"));
        assertThat(oread.getAttachedTo()).isEqualTo(host.getId());
        assertThat(gqs.isCreature(gd, oread)).isFalse();
        assertThat(gqs.getEffectivePower(gd, host)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, host)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, host, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, other, Keyword.FIRST_STRIKE)).isFalse();
    }
}
