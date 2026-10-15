package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.r.RunawayBoulder;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinTombRaider.class, RunawayBoulder.class})
class GoblinTombRaiderTest extends BaseCardTest {

    @Test
    void getsPowerAndHasteWhileControllingAnArtifact() {
        Permanent raider = harness.addToBattlefieldAndReturn(player1, new GoblinTombRaider());
        harness.addToBattlefield(player1, new RunawayBoulder());
        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, raider)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, raider, Keyword.HASTE)).isTrue();
    }

    @Test
    void doesNotGetBonusWithoutAnArtifact() {
        Permanent raider = harness.addToBattlefieldAndReturn(player1, new GoblinTombRaider());
        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, raider)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, raider, Keyword.HASTE)).isFalse();
    }

    @Test
    void opponentArtifactDoesNotCount() {
        Permanent raider = harness.addToBattlefieldAndReturn(player1, new GoblinTombRaider());
        harness.addToBattlefield(player2, new RunawayBoulder());
        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, raider, Keyword.HASTE)).isFalse();
    }

    @Test
    void losesBonusWhenArtifactLeaves() {
        Permanent raider = harness.addToBattlefieldAndReturn(player1, new GoblinTombRaider());
        harness.addToBattlefield(player1, new RunawayBoulder());
        assertThat(gqs.hasKeyword(gd, raider, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof RunawayBoulder);

        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, raider, Keyword.HASTE)).isFalse();
    }

    @Test
    void gainsBonusImmediatelyWhenAnArtifactArrives() {
        Permanent raider = harness.addToBattlefieldAndReturn(player1, new GoblinTombRaider());
        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, raider, Keyword.HASTE)).isFalse();

        harness.addToBattlefield(player1, new RunawayBoulder());

        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, raider)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, raider, Keyword.HASTE)).isTrue();
    }

    @Test
    void multipleArtifactsDoNotStackAndOneRemainingArtifactKeepsBonus() {
        Permanent raider = harness.addToBattlefieldAndReturn(player1, new GoblinTombRaider());
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player1, new RunawayBoulder());
        harness.addToBattlefield(player1, new RunawayBoulder());

        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, raider)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, raider, Keyword.HASTE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(firstArtifact);

        assertThat(gqs.getEffectivePower(gd, raider)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, raider, Keyword.HASTE)).isTrue();
    }

    @Test
    void canAttackWithSummoningSicknessOnlyWhileAnArtifactIsControlled() {
        Permanent raider = harness.addToBattlefieldAndReturn(player1, new GoblinTombRaider());
        assertThat(harness.getAttackLegalityService().canAttack(gd, raider, player1.getId())).isFalse();

        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new RunawayBoulder());

        assertThat(harness.getAttackLegalityService().canAttack(gd, raider, player1.getId())).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(artifact);

        assertThat(harness.getAttackLegalityService().canAttack(gd, raider, player1.getId())).isFalse();
    }
}
