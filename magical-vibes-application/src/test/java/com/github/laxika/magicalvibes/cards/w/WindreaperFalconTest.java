package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.e.EkunduGriffin;
import com.github.laxika.magicalvibes.cards.i.Incinerate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WindreaperFalcon.class, BayFalcon.class, Boomerang.class, EkunduGriffin.class,
        Incinerate.class})
class WindreaperFalconTest extends BaseCardTest {

    @Test
    @DisplayName("Blue creature cannot block Windreaper Falcon")
    void blueCreatureCannotBlock() {
        addCreatureReady(player1, new WindreaperFalcon());
        addCreatureReady(player2, new BayFalcon());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Non-blue flyer can block Windreaper Falcon")
    void nonBlueFlyerCanBlock() {
        addCreatureReady(player1, new WindreaperFalcon());
        Permanent blocker = addCreatureReady(player2, new EkunduGriffin());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Cannot be targeted by a blue instant")
    void cannotBeTargetedByBlueInstant() {
        Permanent falcon = addCreatureReady(player2, new WindreaperFalcon());
        addCreatureReady(player2, new EkunduGriffin());

        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, falcon.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from blue");
    }

    @Test
    @DisplayName("Can be targeted by a red instant")
    void canBeTargetedByRedInstant() {
        Permanent falcon = addCreatureReady(player1, new WindreaperFalcon());

        harness.setHand(player1, List.of(new Incinerate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, falcon.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Protection prevents combat damage from a blue creature")
    void preventsCombatDamageFromBlueCreature() {
        Permanent falcon = addCreatureReady(player1, new WindreaperFalcon());
        Permanent blueAttacker = addCreatureReady(player2, new BayFalcon());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(falcon.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(falcon);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blueAttacker);
    }
}
