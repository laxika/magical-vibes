package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MeteoriteTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 2 damage to a target creature, killing a 2/2")
    void etbDealsTwoDamageToCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Meteorite()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castArtifact(player1, 0, targetId);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Meteorite");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB deals 2 damage to a target player")
    void etbDealsTwoDamageToPlayer() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Meteorite()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castArtifact(player1, 0, player2.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Tap ability prompts for a color and adds one mana of it")
    void manaAbilityAddsChosenColor() {
        harness.addToBattlefield(player1, new Meteorite());
        Permanent meteorite = gd.playerBattlefields.get(player1.getId()).getFirst();
        meteorite.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(meteorite.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
