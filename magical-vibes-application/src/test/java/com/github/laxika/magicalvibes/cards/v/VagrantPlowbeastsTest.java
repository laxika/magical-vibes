package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VagrantPlowbeastsTest extends BaseCardTest {

    @Test
    @DisplayName("Activating targets a power-5-or-greater creature and puts ability on stack")
    void activatingTargetsBigCreature() {
        harness.addToBattlefield(player1, new VagrantPlowbeasts());
        Permanent avatar = addReadyCreature(player1, new AvatarOfMight());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, avatar.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(avatar.getId());
    }

    @Test
    @DisplayName("Resolving grants a regeneration shield to the target creature")
    void resolvingGrantsShield() {
        harness.addToBattlefield(player1, new VagrantPlowbeasts());
        Permanent avatar = addReadyCreature(player1, new AvatarOfMight());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, avatar.getId());
        harness.passBothPriorities();

        assertThat(avatar.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can regenerate an opponent's power-5-or-greater creature")
    void canRegenerateOpponentBigCreature() {
        harness.addToBattlefield(player1, new VagrantPlowbeasts());
        Permanent opponentAvatar = addReadyCreature(player2, new AvatarOfMight());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, opponentAvatar.getId());
        harness.passBothPriorities();

        assertThat(opponentAvatar.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature with power less than 5")
    void cannotTargetSmallCreature() {
        harness.addToBattlefield(player1, new VagrantPlowbeasts());
        Permanent bears = addReadyCreature(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 5 or greater");
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new VagrantPlowbeasts());
        Permanent avatar = addReadyCreature(player1, new AvatarOfMight());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, avatar.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
