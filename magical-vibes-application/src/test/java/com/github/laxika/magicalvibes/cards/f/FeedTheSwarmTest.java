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

    private void cast(Permanent target) {
        prepareSpell();
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new FeedTheSwarm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
