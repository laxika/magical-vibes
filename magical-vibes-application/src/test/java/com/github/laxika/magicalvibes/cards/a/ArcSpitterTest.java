package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArcSpitter.class, GrizzlyBears.class})
class ArcSpitterTest extends BaseCardTest {

    @Test
    @DisplayName("Equip attaches Arc Spitter to a creature you control")
    void equipAttachesToControlledCreature() {
        Permanent spitter = addArcSpitterReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(spitter.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature can deal 1 damage to a creature blocking it")
    void equippedCreatureDamagesBlocker() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent spitter = addArcSpitterReady(player1);
        spitter.setAttachedTo(creature.getId());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        blockCreature();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Arc Spitter cannot target a creature that is not blocking the equipped creature")
    void cannotTargetNonBlocker() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent spitter = addArcSpitterReady(player1);
        spitter.setAttachedTo(creature.getId());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent bystander = addCreatureReady(player2, new GrizzlyBears());

        blockCreature();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("An unattached Arc Spitter grants no activated ability")
    void unattachedSpitterGrantsNoAbility() {
        addCreatureReady(player1, new GrizzlyBears());
        addArcSpitterReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    private Permanent addArcSpitterReady(Player player) {
        Permanent spitter = new Permanent(new ArcSpitter());
        spitter.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(spitter);
        return spitter;
    }

    private void blockCreature() {
        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }
}
