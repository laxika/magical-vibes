package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KorDirge.class, GrizzlyBears.class, ProdigalPyromancer.class})
class KorDirgeTest extends BaseCardTest {

    @Test
    @DisplayName("Redirects all damage from the chosen source to the other target creature")
    void redirectsDamageToOtherTargetCreature() {
        Permanent protectedCreature = addReadyCreature(player1, new GrizzlyBears());
        Permanent redirectCreature = addReadyCreature(player2, new GrizzlyBears());
        Permanent pyromancer = addReadyCreature(player1, new ProdigalPyromancer());
        castKorDirge(protectedCreature, redirectCreature);

        harness.handlePermanentChosen(player1, pyromancer.getId());
        harness.activateAbility(player1, indexOf(player1, pyromancer), null, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isZero();
        assertThat(redirectCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Damage from another source still reaches the protected creature")
    void doesNotRedirectDamageFromAnotherSource() {
        Permanent protectedCreature = addReadyCreature(player1, new GrizzlyBears());
        Permanent redirectCreature = addReadyCreature(player2, new GrizzlyBears());
        Permanent chosenSource = addReadyCreature(player1, new ProdigalPyromancer());
        Permanent otherSource = addReadyCreature(player1, new ProdigalPyromancer());
        castKorDirge(protectedCreature, redirectCreature);

        harness.handlePermanentChosen(player1, chosenSource.getId());
        harness.activateAbility(player1, indexOf(player1, otherSource), null, protectedCreature.getId());
        harness.passBothPriorities();

        assertThat(protectedCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(redirectCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Requires two different creature targets")
    void requiresDifferentCreatureTargets() {
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new KorDirge()));
        addCastMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castKorDirge(Permanent protectedCreature, Permanent redirectCreature) {
        harness.setHand(player1, List.of(new KorDirge()));
        addCastMana();
        harness.castInstant(player1, 0, List.of(protectedCreature.getId(), redirectCreature.getId()));
        harness.passBothPriorities();
    }

    private void addCastMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private Permanent addReadyCreature(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
