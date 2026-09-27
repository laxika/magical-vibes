package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.i.IvoryGuardians;
import com.github.laxika.magicalvibes.cards.p.Pyrotechnics;
import com.github.laxika.magicalvibes.cards.r.RagingBull;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BeastsOfBogardan.class, IvoryGuardians.class, RagingBull.class, Pyrotechnics.class})
class BeastsOfBogardanTest extends BaseCardTest {

    @Test
    @DisplayName("Base 3/3 when no opponent controls a nontoken white permanent")
    void baseWithoutWhitePermanent() {
        Permanent beasts = harness.addToBattlefieldAndReturn(player1, new BeastsOfBogardan());
        assertThat(gqs.getEffectivePower(gd, beasts)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, beasts)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +1/+1 when an opponent controls a nontoken white permanent")
    void boostWhenOpponentControlsWhitePermanent() {
        Permanent beasts = harness.addToBattlefieldAndReturn(player1, new BeastsOfBogardan());
        harness.addToBattlefield(player2, new IvoryGuardians());

        assertThat(gqs.getEffectivePower(gd, beasts)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, beasts)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not get the boost from a white token")
    void noBoostFromWhiteToken() {
        Permanent beasts = harness.addToBattlefieldAndReturn(player1, new BeastsOfBogardan());
        IvoryGuardians token = new IvoryGuardians();
        token.setToken(true);
        harness.addToBattlefield(player2, token);

        assertThat(gqs.getEffectivePower(gd, beasts)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, beasts)).isEqualTo(3);
    }

    @Test
    @DisplayName("The controller's own white permanent does not grant the boost")
    void noBoostFromOwnWhitePermanent() {
        Permanent beasts = harness.addToBattlefieldAndReturn(player1, new BeastsOfBogardan());
        harness.addToBattlefield(player1, new IvoryGuardians());

        assertThat(gqs.getEffectivePower(gd, beasts)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, beasts)).isEqualTo(3);
    }

    @Test
    @DisplayName("Red creature cannot block Beasts of Bogardan")
    void redCreatureCannotBlock() {
        addCreatureReady(player1, new BeastsOfBogardan());
        addCreatureReady(player2, new RagingBull());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Protection from red prevents combat damage from red creatures")
    void redCombatDamageIsPrevented() {
        Permanent beasts = addCreatureReady(player1, new BeastsOfBogardan());
        addCreatureReady(player2, new RagingBull());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(beasts);
        assertThat(beasts.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A red spell cannot target Beasts of Bogardan")
    void redSpellCannotTargetBeasts() {
        Permanent beasts = addCreatureReady(player1, new BeastsOfBogardan());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Pyrotechnics()));
        harness.addMana(player2, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, Map.of(beasts.getId(), 4)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection from red");
    }
}
