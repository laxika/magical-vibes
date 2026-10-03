package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Fly;
import com.github.laxika.magicalvibes.cards.l.LeatherArmor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArmoryVeteran.class, LeatherArmor.class, Fly.class})
class ArmoryVeteranTest extends BaseCardTest {

    @Test
    void withoutEquipmentDoesNotHaveMenace() {
        Permanent veteran = addCreatureReady(player1, new ArmoryVeteran());

        assertThat(gqs.hasKeyword(gd, veteran, Keyword.MENACE)).isFalse();
    }

    @Test
    void whileEquippedHasMenace() {
        Permanent veteran = addCreatureReady(player1, new ArmoryVeteran());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeatherArmor());
        equipment.setAttachedTo(veteran.getId());

        assertThat(gqs.hasKeyword(gd, veteran, Keyword.MENACE)).isTrue();
    }

    @Test
    void losesMenaceWhenEquipmentIsDetached() {
        Permanent veteran = addCreatureReady(player1, new ArmoryVeteran());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeatherArmor());
        equipment.setAttachedTo(veteran.getId());

        equipment.setAttachedTo(null);

        assertThat(gqs.hasKeyword(gd, veteran, Keyword.MENACE)).isFalse();
    }

    @Test
    void anAuraAloneDoesNotGrantMenace() {
        Permanent veteran = addCreatureReady(player1, new ArmoryVeteran());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Fly());
        aura.setAttachedTo(veteran.getId());

        assertThat(gqs.hasKeyword(gd, veteran, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, veteran, Keyword.MENACE)).isFalse();
    }

    @Test
    void equipmentControlledByOpponentStillGrantsMenace() {
        Permanent veteran = addCreatureReady(player1, new ArmoryVeteran());
        Permanent equipment = harness.addToBattlefieldAndReturn(player2, new LeatherArmor());
        equipment.setAttachedTo(veteran.getId());

        assertThat(gqs.hasKeyword(gd, veteran, Keyword.MENACE)).isTrue();
    }

    @Test
    void retainsMenaceUntilLastEquipmentIsDetached() {
        Permanent veteran = addCreatureReady(player1, new ArmoryVeteran());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new LeatherArmor());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LeatherArmor());
        first.setAttachedTo(veteran.getId());
        second.setAttachedTo(veteran.getId());

        first.setAttachedTo(null);
        assertThat(gqs.hasKeyword(gd, veteran, Keyword.MENACE)).isTrue();

        second.setAttachedTo(null);
        assertThat(gqs.hasKeyword(gd, veteran, Keyword.MENACE)).isFalse();
    }

    @Test
    void movingEquipmentTransfersConditionalMenace() {
        Permanent first = addCreatureReady(player1, new ArmoryVeteran());
        Permanent second = addCreatureReady(player1, new ArmoryVeteran());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeatherArmor());
        equipment.setAttachedTo(first.getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.MENACE)).isFalse();

        equipment.setAttachedTo(second.getId());

        assertThat(gqs.hasKeyword(gd, first, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.MENACE)).isTrue();
    }

    @Test
    void losesMenaceWhenEquipmentLeavesBattlefield() {
        Permanent veteran = addCreatureReady(player1, new ArmoryVeteran());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeatherArmor());
        equipment.setAttachedTo(veteran.getId());
        assertThat(gqs.hasKeyword(gd, veteran, Keyword.MENACE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(equipment);
        gd.playerGraveyards.get(player1.getId()).add(equipment.getCard());

        assertThat(gqs.hasKeyword(gd, veteran, Keyword.MENACE)).isFalse();
    }

    @Test
    void equippedVeteranCannotBeBlockedByOneCreature() {
        Permanent veteran = addCreatureReady(player1, new ArmoryVeteran());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeatherArmor());
        equipment.setAttachedTo(veteran.getId());
        addCreatureReady(player2, new ArmoryVeteran());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked except by two or more creatures");
    }

    @Test
    void equippedVeteranCanBeBlockedByTwoCreatures() {
        Permanent veteran = addCreatureReady(player1, new ArmoryVeteran());
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new LeatherArmor());
        equipment.setAttachedTo(veteran.getId());
        addCreatureReady(player2, new ArmoryVeteran());
        addCreatureReady(player2, new ArmoryVeteran());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    void unequippedVeteranCanBeBlockedByOneCreature() {
        addCreatureReady(player1, new ArmoryVeteran());
        addCreatureReady(player2, new ArmoryVeteran());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }
}
