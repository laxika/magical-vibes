package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LionHeart.class, GrizzlyBears.class})
class LionHeartTest extends BaseCardTest {

    @Test
    @DisplayName("Lion Heart deals 2 damage to a target creature when it enters")
    void enteringBattlefieldDealsDamageToCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LionHeart()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        Permanent target = findPermanent(player2, "Grizzly Bears");
        harness.castArtifact(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Lion Heart");
    }

    @Test
    @DisplayName("Lion Heart deals 2 damage to a target player when it enters")
    void enteringBattlefieldDealsDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new LionHeart()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Equipped creature gets +2/+1")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1);
        Permanent lionHeart = addLionHeartReady(player1);
        lionHeart.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equip {2} attaches Lion Heart to a target creature")
    void equipAttachesToCreature() {
        Permanent lionHeart = addLionHeartReady(player1);
        Permanent creature = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(lionHeart.getAttachedTo()).isEqualTo(creature.getId());
    }

    private Permanent addLionHeartReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent lionHeart = new Permanent(new LionHeart());
        lionHeart.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(lionHeart);
        return lionHeart;
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }
}
