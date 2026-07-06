package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThrabenDoomsayerTest extends BaseCardTest {

    // ===== Tap ability: token creation =====

    @Test
    @DisplayName("Activating ability puts token creation on the stack and taps Doomsayer")
    void activatingAbilityPutsOnStackAndTaps() {
        Permanent doomsayer = addReadyDoomsayer(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Thraben Doomsayer");
        assertThat(doomsayer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving ability creates a 1/1 white Human token")
    void resolvingAbilityCreatesToken() {
        addReadyDoomsayer(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Human"))
                .findFirst().orElseThrow();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.HUMAN);
    }

    // ===== Fateful hour static boost =====

    @Test
    @DisplayName("No boost to other creatures at default 20 life")
    void noBoostAtDefaultLife() {
        harness.addToBattlefield(player1, new ThrabenDoomsayer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boosts other creatures +2/+2 at exactly 5 life")
    void boostAtExactly5Life() {
        gd.playerLifeTotals.put(player1.getId(), 5);
        harness.addToBattlefield(player1, new ThrabenDoomsayer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not boost itself at 5 life")
    void doesNotBoostItself() {
        gd.playerLifeTotals.put(player1.getId(), 5);
        Permanent doomsayer = harness.addToBattlefieldAndReturn(player1, new ThrabenDoomsayer());

        assertThat(gqs.getEffectivePower(gd, doomsayer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, doomsayer)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        gd.playerLifeTotals.put(player1.getId(), 5);
        harness.addToBattlefield(player1, new ThrabenDoomsayer());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Gains and loses the boost as life crosses the threshold")
    void boostIsDynamic() {
        harness.addToBattlefield(player1, new ThrabenDoomsayer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        gd.playerLifeTotals.put(player1.getId(), 5);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);

        gd.playerLifeTotals.put(player1.getId(), 10);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    private Permanent addReadyDoomsayer(Player player) {
        return addCreatureReady(player, new ThrabenDoomsayer());
    }
}
