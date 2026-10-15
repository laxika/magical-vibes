package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FeedTheSwarm.class, GrizzlyBears.class, GloriousAnthem.class, Forest.class})
class FeedTheSwarmTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target creature and its caster loses life equal to its mana value")
    void destroysCreatureAndCasterLosesItsManaValue() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int casterLifeBefore = gd.playerLifeTotals.get(player1.getId());

        cast(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(casterLifeBefore - 2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Destroys a target enchantment and its caster loses life equal to its mana value")
    void destroysEnchantmentAndCasterLosesItsManaValue() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GloriousAnthem());
        int casterLifeBefore = gd.playerLifeTotals.get(player1.getId());

        cast(target);

        harness.assertNotOnBattlefield(player2, "Glorious Anthem");
        harness.assertInGraveyard(player2, "Glorious Anthem");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(casterLifeBefore - 3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The caster still loses life when the target is indestructible")
    void casterLosesLifeWhenTargetIsIndestructible() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        int casterLifeBefore = gd.playerLifeTotals.get(player1.getId());

        cast(target);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(casterLifeBefore - 2);
    }

    @Test
    @DisplayName("Rejects targets the caster controls")
    void rejectsTargetTheCasterControls() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        prepareSpell();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls");
    }

    @Test
    @DisplayName("Rejects a target that is neither a creature nor an enchantment")
    void rejectsNonCreatureNonEnchantmentTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareSpell();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature or enchantment");
    }

    @Test
    @DisplayName("The caster still loses life when the target regenerates")
    void casterLosesLifeWhenTargetRegenerates() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.setRegenerationShield(1);

        cast(target);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(target.isTapped()).isTrue();
        assertThat(target.getRegenerationShield()).isZero();
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("No life is lost when the target gains hexproof before resolution")
    void noLifeLostWhenTargetGainsHexproof() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSpell();
        harness.castSorcery(player1, 0, target.getId());
        target.getGrantedKeywords().add(Keyword.HEXPROOF);

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Feed the Swarm");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("No life is lost when the target leaves the battlefield before resolution")
    void noLifeLostWhenTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareSpell();
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Feed the Swarm");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void cast(Permanent target) {
        prepareSpell();
        harness.castAndResolveSorcery(player1, 0, target.getId());
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new FeedTheSwarm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
