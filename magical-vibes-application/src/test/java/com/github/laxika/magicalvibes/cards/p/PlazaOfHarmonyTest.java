package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AzoriusGuildgate;
import com.github.laxika.magicalvibes.cards.b.BorosGuildgate;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PlazaOfHarmonyTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 3 life when it enters with two controlled Gates")
    void gainsLifeWithTwoControlledGates() {
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.addToBattlefield(player1, new BorosGuildgate());
        int before = gd.playerLifeTotals.get(player1.getId());

        playLand(new PlazaOfHarmony());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(before + 3);
    }

    @Test
    @DisplayName("Does not gain life when it enters with fewer than two controlled Gates")
    void doesNotGainLifeWithFewerThanTwoControlledGates() {
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        int before = gd.playerLifeTotals.get(player1.getId());

        playLand(new PlazaOfHarmony());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(before);
    }

    @Test
    @DisplayName("Adds one colorless mana")
    void addsColorlessMana() {
        Permanent plaza = addPlazaReady(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(plaza.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Adds mana of a type a controlled Gate could produce")
    void addsManaOfTypeControlledGateCouldProduce() {
        addPlazaReady(player1);
        harness.addToBattlefield(player1, new AzoriusGuildgate());
        harness.addToBattlefield(player1, new BorosGuildgate());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    private void playLand(com.github.laxika.magicalvibes.model.Card land) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(land));
        harness.playLand(player1, 0);
    }

    private Permanent addPlazaReady(Player player) {
        Permanent plaza = harness.addToBattlefieldAndReturn(player, new PlazaOfHarmony());
        plaza.setSummoningSick(false);
        return plaza;
    }
}
