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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

        harness.assertLife(player2, 18);
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

    @Test
    @DisplayName("Re-equipping moves the boost from the old creature to the new creature")
    void reEquippingMovesBoost() {
        Permanent lionHeart = addLionHeartReady(player1);
        Permanent first = addCreatureReady(player1);
        Permanent second = addCreatureReady(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, null, first.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(lionHeart.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        Permanent lionHeart = addLionHeartReady(player1);
        Permanent creature = addCreatureReady(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");

        assertThat(lionHeart.getAttachedTo()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Entering damage can target Lion Heart's controller")
    void enteringDamageCanTargetController() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new LionHeart()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertOnBattlefield(player1, "Lion Heart");
    }

    private Permanent addLionHeartReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent lionHeart = harness.addToBattlefieldAndReturn(player, new LionHeart());
        lionHeart.setSummoningSick(false);
        return lionHeart;
    }

    private Permanent addCreatureReady(com.github.laxika.magicalvibes.model.Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        return creature;
    }
}
