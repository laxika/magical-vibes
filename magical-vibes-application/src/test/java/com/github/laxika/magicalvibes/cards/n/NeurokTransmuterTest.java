package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AuriokGlaivemaster;
import com.github.laxika.magicalvibes.cards.d.DarksteelGargoyle;
import com.github.laxika.magicalvibes.cards.d.DarksteelIngot;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NeurokTransmuter.class, AuriokGlaivemaster.class,
        DarksteelGargoyle.class, DarksteelIngot.class})
class NeurokTransmuterTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability makes a creature an artifact until end of turn")
    void makesCreatureAnArtifact() {
        harness.addToBattlefield(player1, new NeurokTransmuter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AuriokGlaivemaster());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelIngot());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The second ability makes an artifact creature blue and nonartifact")
    void makesArtifactCreatureBlueAndNonartifact() {
        harness.addToBattlefield(player1, new NeurokTransmuter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DarksteelGargoyle());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

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
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AuriokGlaivemaster());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact creature");
    }

    @Test
    @DisplayName("The second ability replaces an existing color")
    void secondAbilityReplacesExistingColor() {
        harness.addToBattlefield(player1, new NeurokTransmuter());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AuriokGlaivemaster());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.isArtifact(gd, target)).isTrue();

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, target)).isTrue();
        assertThat(gqs.isArtifact(gd, target)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.BLUE);

        gd.expireEndOfTurnFloatingEffects();
        target.resetModifiers();

        assertThat(gqs.isArtifact(gd, target)).isFalse();
        assertThat(gqs.getEffectiveColors(gd, target)).containsExactly(CardColor.WHITE);
    }
}
