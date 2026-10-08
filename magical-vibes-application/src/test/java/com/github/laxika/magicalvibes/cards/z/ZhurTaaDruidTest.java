package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ZhurTaaDruid.class})
class ZhurTaaDruidTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for mana adds {G} and deals 1 damage to each opponent")
    void tappingForManaDamagesOpponent() {
        addCreatureReady(player1, new ZhurTaaDruid());
        int startingLife = gd.getLife(player2.getId());

        gs.tapPermanent(gd, player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        // The damage trigger waits until a player would next receive priority.
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);

        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife - 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Tapping to attack does not trigger the damage ability")
    void attackingDoesNotTrigger() {
        Permanent druid = addCreatureReady(player1, new ZhurTaaDruid());
        int startingLife = gd.getLife(player2.getId());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        resolveAllTriggers();

        assertThat(druid.isTapped()).isTrue();
        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingManaAbilityTriggers).isEmpty();
    }

    @Test
    @DisplayName("A summoning-sick druid cannot activate its tap ability")
    void summoningSicknessPreventsManaAndDamage() {
        Permanent druid = harness.addToBattlefieldAndReturn(player1, new ZhurTaaDruid());

        assertThatThrownBy(() -> gs.tapPermanent(gd, player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");

        assertThat(druid.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingManaAbilityTriggers).isEmpty();
    }

    @Test
    @DisplayName("An opponent's druid damages the other player, not its controller")
    void opponentControlledDruidDamagesPlayerOne() {
        addCreatureReady(player2, new ZhurTaaDruid());
        harness.forceActivePlayer(player2);

        harness.tapPermanent(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Each druid activation creates its own damage trigger")
    void twoDruidsProduceTwoManaAndTwoDamage() {
        addCreatureReady(player1, new ZhurTaaDruid());
        addCreatureReady(player1, new ZhurTaaDruid());

        gs.tapPermanent(gd, player1, 0);
        gs.tapPermanent(gd, player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);

        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }
}