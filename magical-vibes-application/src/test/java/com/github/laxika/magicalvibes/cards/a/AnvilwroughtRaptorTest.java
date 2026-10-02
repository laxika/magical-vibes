package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.v.Vaporkin;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnvilwroughtRaptor.class, BronzeSable.class, Vaporkin.class})
class AnvilwroughtRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("A ground creature cannot block it")
    void groundCreatureCannotBlockIt() {
        Permanent attacker = addCreatureReady(player1, new AnvilwroughtRaptor());
        attacker.setAttacking(true);
        addCreatureReady(player2, new BronzeSable());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("First strike defeats a flying blocker before regular damage")
    void firstStrikeDefeatsSmallerBlockerBeforeRegularDamage() {
        Permanent attacker = addCreatureReady(player1, new AnvilwroughtRaptor());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new Vaporkin());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
    }

    @Test
    @DisplayName("First strike kills an attacker before it can damage the blocking Raptor")
    void firstStrikeWorksWhileBlocking() {
        Permanent attacker = addCreatureReady(player1, new BronzeSable());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new AnvilwroughtRaptor());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(attacker.getCard());
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An unblocked Raptor deals combat damage only once")
    void unblockedRaptorDealsDamageOnlyOnce() {
        addCreatureReady(player1, new AnvilwroughtRaptor());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 18);
    }
}
