package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpidersilkArmor.class, FreshVolunteers.class, CloudSprite.class, Opalescence.class})
class SpidersilkArmorTest extends BaseCardTest {

    @Test
    @DisplayName("Your creatures get +0/+1 and reach")
    void buffsOwnCreaturesAndGrantsReach() {
        harness.addToBattlefield(player1, new SpidersilkArmor());
        Permanent volunteers = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());

        assertThat(gqs.getEffectivePower(gd, volunteers)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, volunteers)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, volunteers, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Spidersilk Armor does not affect an opponent's creatures")
    void doesNotAffectOpponentsCreatures() {
        harness.addToBattlefield(player1, new SpidersilkArmor());
        Permanent opponentVolunteers = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());

        assertThat(gqs.getEffectivePower(gd, opponentVolunteers)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentVolunteers)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentVolunteers, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("The bonus is removed when Spidersilk Armor leaves the battlefield")
    void bonusIsRemovedWhenArmorLeaves() {
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new SpidersilkArmor());
        Permanent volunteers = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, armor));

        assertThat(gqs.getEffectiveToughness(gd, volunteers)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, volunteers, Keyword.REACH)).isFalse();
    }

    @Test
    @DisplayName("Multiple copies stack their toughness bonuses and retain reach after one leaves")
    void multipleCopiesStack() {
        Permanent firstArmor = harness.addToBattlefieldAndReturn(player1, new SpidersilkArmor());
        harness.addToBattlefield(player1, new SpidersilkArmor());
        Permanent volunteers = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());

        assertThat(gqs.getEffectivePower(gd, volunteers)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, volunteers)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, volunteers, Keyword.REACH)).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, firstArmor));

        assertThat(gqs.getEffectiveToughness(gd, volunteers)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, volunteers, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Granted reach allows a ground creature to block a flying creature")
    void reachAllowsBlockingFlyingCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new CloudSprite());
        Permanent blocker = harness.addToBattlefieldAndReturn(player1, new FreshVolunteers());

        assertThat(bls.canBlockAttacker(gd, blocker, attacker, gd.playerBattlefields.get(player1.getId()))).isFalse();

        Permanent armor = harness.addToBattlefieldAndReturn(player1, new SpidersilkArmor());

        assertThat(bls.canBlockAttacker(gd, blocker, attacker, gd.playerBattlefields.get(player1.getId()))).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, armor));

        assertThat(bls.canBlockAttacker(gd, blocker, attacker, gd.playerBattlefields.get(player1.getId()))).isFalse();
    }

    @Test
    @DisplayName("Spidersilk Armor benefits from its own ability when Opalescence makes it a creature")
    void animatedArmorBuffsItself() {
        Permanent armor = harness.addToBattlefieldAndReturn(player1, new SpidersilkArmor());
        harness.addToBattlefield(player1, new Opalescence());

        assertThat(gqs.isCreature(gd, armor)).isTrue();
        assertThat(gqs.getEffectivePower(gd, armor)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, armor)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, armor, Keyword.REACH)).isTrue();
    }
}
