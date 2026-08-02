package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HomingLightningTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to target creature")
    void damagesTargetCreature() {
        Permanent target = addCreature(player2, new AvatarOfMight());
        castHomingLightning(target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Damages every other creature with the same name across both battlefields")
    void damagesAllCreaturesWithSameName() {
        Permanent ownAvatar = addCreature(player1, new AvatarOfMight());
        Permanent oppAvatar1 = addCreature(player2, new AvatarOfMight());
        Permanent oppAvatar2 = addCreature(player2, new AvatarOfMight());
        Permanent elves = addCreature(player2, new LlanowarElves());

        castHomingLightning(oppAvatar1.getId());

        assertThat(ownAvatar.getMarkedDamage()).isEqualTo(4);
        assertThat(oppAvatar1.getMarkedDamage()).isEqualTo(4);
        assertThat(oppAvatar2.getMarkedDamage()).isEqualTo(4);
        assertThat(elves.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Lethal damage kills every creature with the same name")
    void killsAllCreaturesWithSameName() {
        addCreature(player1, new GrizzlyBears());
        Permanent target = addCreature(player2, new GrizzlyBears());
        addCreature(player2, new LlanowarElves());

        castHomingLightning(target.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Fizzles when the target creature is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        Permanent target = addCreature(player2, new AvatarOfMight());
        Permanent other = addCreature(player2, new AvatarOfMight());

        harness.setHand(player1, List.of(new HomingLightning()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(target.getId()));
        harness.passBothPriorities();

        assertThat(other.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new HomingLightning()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castHomingLightning(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new HomingLightning()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }

    private Permanent addCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
