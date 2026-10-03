package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BespokeB.class, Forest.class, GrizzlyBears.class})
class BespokeBTest extends BaseCardTest {

    @Test
    void entersAndReturnsAnotherNonlandPermanentToItsOwnersHand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        castBespokeB(target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
    }

    @Test
    void enterAbilityMayChooseNoTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castBespokeB();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    void enterAbilityCannotTargetALand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new BespokeB()));
        addBespokeBMana();

        assertThatThrownBy(() -> harness.castArtifact(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another nonland permanent");
    }

    @Test
    void equippedCreatureGetsBoostAndVigilance() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = addBespokeBReady(player1);
        equipment.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void equipAttachesToCreature() {
        Permanent equipment = addBespokeBReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void canReturnAnotherEquipmentWithTheSameName() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BespokeB());

        castBespokeB(target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target).hasSize(1);
        harness.assertInHand(player1, "Bespoke Bō");
    }

    @Test
    void returnsStolenPermanentToOwnerRatherThanController() {
        GrizzlyBears stolenCard = new GrizzlyBears();
        stolenCard.setOwnerId(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player1, stolenCard);

        castBespokeB(target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void reequippingMovesBoostAndVigilanceToNewCreature() {
        Permanent equipment = addBespokeBReady(player1);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        equipment.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(equipment.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, second, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void equipCannotTargetOpponentsCreature() {
        Permanent equipment = addBespokeBReady(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(equipment.getAttachedTo()).isNull();
    }

    private void castBespokeB() {
        harness.setHand(player1, List.of(new BespokeB()));
        addBespokeBMana();
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void castBespokeB(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new BespokeB()));
        addBespokeBMana();
        harness.castArtifact(player1, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void addBespokeBMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private Permanent addBespokeBReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new BespokeB());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
