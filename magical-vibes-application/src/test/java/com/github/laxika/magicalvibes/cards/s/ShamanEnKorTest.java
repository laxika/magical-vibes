package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShamanEnKorTest extends BaseCardTest {

    @Test
    @DisplayName("The free ability redirects damage to a creature you control")
    void redirectsDamageToControlledCreature() {
        Permanent shaman = addReadyPermanent(player1, new ShamanEnKor());
        Permanent pyromancer = addReadyPermanent(player1, new ProdigalPyromancer());
        Permanent destination = addReadyStats(player1, 3, 3);

        harness.activateAbility(player1, indexOf(player1, shaman), null, destination.getId());
        harness.passBothPriorities();

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, shaman.getId());
        harness.passBothPriorities();

        assertThat(shaman.getMarkedDamage()).isEqualTo(0);
        assertThat(destination.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("The free ability cannot target an opponent's creature as the destination")
    void freeAbilityCannotTargetOpponentsCreature() {
        Permanent shaman = addReadyPermanent(player1, new ShamanEnKor());
        Permanent opponentCreature = addReadyStats(player2, 3, 3);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, indexOf(player1, shaman), null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The paid ability redirects the chosen source's next damage to Shaman en-Kor")
    void redirectsChosenSourceNextDamageToSelf() {
        Permanent shaman = addReadyPermanent(player1, new ShamanEnKor());
        Permanent pyromancer = addReadyPermanent(player1, new ProdigalPyromancer());
        Permanent protectedCreature = addReadyStats(player2, 3, 3);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(player1, shaman), 1, null, protectedCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, pyromancer.getId());

        harness.activateAbility(player1, indexOf(player1, pyromancer), null, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(shaman.getMarkedDamage()).isEqualTo(1);
        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("The paid ability redirects only the chosen source's next damage event")
    void paidAbilityRedirectsOnlyNextDamageEvent() {
        Permanent shaman = addReadyPermanent(player1, new ShamanEnKor());
        Permanent firstPyromancer = addReadyPermanent(player1, new ProdigalPyromancer());
        Permanent secondPyromancer = addReadyPermanent(player1, new ProdigalPyromancer());
        Permanent protectedCreature = addReadyStats(player2, 3, 3);

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(player1, shaman), 1, null, protectedCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, firstPyromancer.getId());

        harness.activateAbility(player1, indexOf(player1, firstPyromancer), null, protectedCreature.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, indexOf(player1, secondPyromancer), null, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(shaman.getMarkedDamage()).isEqualTo(1);
        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(1);
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyStats(Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        return addReadyPermanent(player, card);
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
