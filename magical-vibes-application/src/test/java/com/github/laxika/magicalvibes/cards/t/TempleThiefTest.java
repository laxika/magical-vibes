package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AlseidOfLifesBounty;
import com.github.laxika.magicalvibes.cards.a.AqueousForm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SentinelsEyes;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({TempleThief.class, GrizzlyBears.class, AqueousForm.class, AlseidOfLifesBounty.class,
        SentinelsEyes.class})
class TempleThiefTest extends BaseCardTest {

    @Test
    @DisplayName("Temple Thief can't be blocked by an enchanted creature")
    void cannotBeBlockedByEnchantedCreature() {
        Permanent thief = addReady(player1, new TempleThief(), true);
        Permanent blocker = addReady(player2, new GrizzlyBears(), false);
        Permanent aura = new Permanent(new AqueousForm());
        aura.setAttachedTo(blocker.getId());
        gd.playerBattlefields.get(player2.getId()).add(aura);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, thief))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("Temple Thief can't be blocked by an enchantment creature")
    void cannotBeBlockedByEnchantmentCreature() {
        Permanent thief = addReady(player1, new TempleThief(), true);
        Permanent blocker = addReady(player2, new AlseidOfLifesBounty(), false);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, thief))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("Temple Thief can be blocked by an ordinary creature")
    void canBeBlockedByOrdinaryCreature() {
        Permanent thief = addReady(player1, new TempleThief(), true);
        Permanent blocker = addReady(player2, new GrizzlyBears(), false);

        prepareDeclareBlockers();
        declareBlock(blocker, thief);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("An Aura controlled by the attacker still prevents the enchanted creature from blocking")
    void cannotBeBlockedByCreatureEnchantedByOpponentsAura() {
        Permanent thief = addReady(player1, new TempleThief(), true);
        Permanent blocker = addCreatureReady(player2, new TempleThief());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SentinelsEyes());
        aura.setAttachedTo(blocker.getId());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, thief))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("A creature can block Temple Thief after its only Aura leaves the battlefield")
    void canBeBlockedAfterAuraLeavesBattlefield() {
        Permanent thief = addReady(player1, new TempleThief(), true);
        Permanent blocker = addCreatureReady(player2, new TempleThief());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new SentinelsEyes());
        aura.setAttachedTo(blocker.getId());
        gd.playerBattlefields.get(player2.getId()).remove(aura);
        gd.playerGraveyards.get(player2.getId()).add(aura.getCard());

        prepareDeclareBlockers();
        declareBlock(blocker, thief);

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }

    private Permanent addReady(Player player, Card card, boolean attacking) {
        Permanent permanent = addCreatureReady(player, card);
        permanent.setAttacking(attacking);
        return permanent;
    }
}
