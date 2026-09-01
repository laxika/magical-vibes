package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SecretTunnel.class, GrizzlyBears.class, LlanowarElves.class})
class SecretTunnelTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Secret Tunnel adds one colorless mana")
    void tapsForColorlessMana() {
        Permanent tunnel = addReadyPermanent(new SecretTunnel());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(tunnel.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Two controlled creatures sharing a type can't be blocked this turn")
    void makesTwoCreaturesUnblockable() {
        addReadyPermanent(new SecretTunnel());
        Permanent first = addReadyPermanent(new GrizzlyBears());
        Permanent second = addReadyPermanent(new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(first.isCantBeBlocked()).isTrue();
        assertThat(second.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Cannot target two controlled creatures that share no creature type")
    void rejectsCreaturesWithoutSharedType() {
        addReadyPermanent(new SecretTunnel());
        Permanent first = addReadyPermanent(new GrizzlyBears());
        Permanent second = addReadyPermanent(new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("share a creature type");
    }

    @Test
    @DisplayName("The Secret Tunnel ability only targets creatures you control")
    void rejectsOpponentCreature() {
        addReadyPermanent(new SecretTunnel());
        Permanent first = addReadyPermanent(new GrizzlyBears());
        Permanent opponent = addReadyPermanent(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(first.getId(), opponent.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    private Permanent addReadyPermanent(Card card) {
        return addReadyPermanent(player1, card);
    }

    private Permanent addReadyPermanent(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
