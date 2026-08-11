package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NeurokTransmuterTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability makes a creature an artifact until end of turn")
    void makesCreatureAnArtifact() {
        harness.addToBattlefield(player1, new NeurokTransmuter());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Grizzly Bears");
        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(gqs.isArtifact(gd, target)).isTrue();

        gd.expireEndOfTurnFloatingEffects();
        target.resetModifiers();

        assertThat(gqs.isArtifact(gd, target)).isFalse();
    }

    @Test
    @DisplayName("The first ability only targets creatures")
    void firstAbilityRequiresCreature() {
        harness.addToBattlefield(player1, new NeurokTransmuter());
        harness.addToBattlefield(player2, new DarksteelIngot());
        UUID targetId = harness.getPermanentId(player2, "Darksteel Ingot");
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The second ability makes an artifact creature blue and nonartifact")
    void makesArtifactCreatureBlueAndNonartifact() {
        harness.addToBattlefield(player1, new NeurokTransmuter());
        harness.addToBattlefield(player2, new IronMyr());
        UUID targetId = harness.getPermanentId(player2, "Iron Myr");
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, targetId);
        harness.passBothPriorities();

        Permanent target = findPermanent(player2, "Iron Myr");
        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(gqs.isArtifact(gd, target)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLUE);

        gd.expireEndOfTurnFloatingEffects();
        target.resetModifiers();

        assertThat(gqs.isArtifact(gd, target)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, target)).doesNotContain(CardColor.BLUE);
    }

    @Test
    @DisplayName("The second ability only targets artifact creatures")
    void secondAbilityRequiresArtifactCreature() {
        harness.addToBattlefield(player1, new NeurokTransmuter());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, targetId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact creature");
    }
}
