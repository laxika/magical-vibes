package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AshnodsAltar.class, GrizzlyBears.class})
class AshnodsAltarTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature adds two colorless mana")
    void sacrificeAddsTwoColorlessMana() {
        harness.addToBattlefield(player1, new AshnodsAltar());
        Permanent sacrificedBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrificedBear.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();

        // One Grizzly Bears is sacrificed to the graveyard
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Grizzly Bears"))
                .hasSize(1);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Automatically sacrifices the only creature available")
    void automaticallySacrificesOnlyCreatureAvailable() {
        harness.addToBattlefield(player1, new AshnodsAltar());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bear);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bear.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without a creature controlled by the activator")
    void cannotActivateWithoutControllerCreature() {
        harness.addToBattlefield(player1, new AshnodsAltar());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
