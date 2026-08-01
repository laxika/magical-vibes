package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IzzetStaticasterTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability deals 1 damage to target creature")
    void damagesTargetCreature() {
        Permanent staticaster = addReadyStaticaster(player1);
        Permanent target = addCreature(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(staticaster.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Damages all creatures with the same name across both battlefields")
    void damagesAllCreaturesWithSameName() {
        addReadyStaticaster(player1);
        Permanent ownBear = addCreature(player1, new GrizzlyBears());
        Permanent oppBear1 = addCreature(player2, new GrizzlyBears());
        Permanent oppBear2 = addCreature(player2, new GrizzlyBears());
        Permanent elves = addCreature(player2, new LlanowarElves());

        harness.activateAbility(player1, 0, null, oppBear1.getId());
        harness.passBothPriorities();

        assertThat(ownBear.getMarkedDamage()).isEqualTo(1);
        assertThat(oppBear1.getMarkedDamage()).isEqualTo(1);
        assertThat(oppBear2.getMarkedDamage()).isEqualTo(1);
        assertThat(elves.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Same-name hexproof creature is still damaged (not targeted)")
    void damagesSameNameHexproofCreature() {
        addReadyStaticaster(player1);
        Permanent target = addCreature(player2, new GrizzlyBears());
        Permanent hexproof = addCreature(player2, new GrizzlyBears());
        TestCards.mutableCard(hexproof).setKeywords(EnumSet.of(Keyword.HEXPROOF));

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(hexproof.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a hexproof creature directly")
    void cannotTargetHexproofCreature() {
        addReadyStaticaster(player1);
        Permanent hexproof = addCreature(player2, new GrizzlyBears());
        TestCards.mutableCard(hexproof).setKeywords(EnumSet.of(Keyword.HEXPROOF));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, hexproof.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles when target creature is removed before resolution")
    void fizzlesWhenTargetRemoved() {
        addReadyStaticaster(player1);
        Permanent target = addCreature(player2, new GrizzlyBears());
        Permanent other = addCreature(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(target.getId()));
        harness.passBothPriorities();

        assertThat(other.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Haste lets the tap ability activate the turn it enters")
    void hasteAllowsActivationSameTurn() {
        harness.setHand(player1, List.of(new IzzetStaticaster()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        Permanent target = addCreature(player2, new GrizzlyBears());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        int idx = findPermanentIndex(player1, "Izzet Staticaster");
        harness.activateAbility(player1, idx, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a player")
    void cannotTargetPlayer() {
        addReadyStaticaster(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyStaticaster(Player player) {
        Permanent perm = new Permanent(new IzzetStaticaster());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int findPermanentIndex(Player player, String name) {
        List<Permanent> battlefield = gd.playerBattlefields.get(player.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getCard().getName().equals(name)) {
                return i;
            }
        }
        throw new AssertionError("Permanent not found: " + name);
    }
}
