package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.CurseArtifact;
import com.github.laxika.magicalvibes.cards.d.DarkSphere;
import com.github.laxika.magicalvibes.cards.m.MarshGoblins;
import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Exorcist.class, MarshGoblins.class, Squire.class, CurseArtifact.class, DarkSphere.class})
class ExorcistTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target black creature")
    void destroysBlackCreature() {
        Permanent exorcist = addCreatureReady(player1, new Exorcist());
        Permanent target = addCreatureReady(player2, new MarshGoblins());
        addWhiteAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(exorcist.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Marsh Goblins");
        harness.assertInGraveyard(player2, "Marsh Goblins");
    }

    @Test
    @DisplayName("Cannot target a nonblack creature")
    void cannotTargetNonblackCreature() {
        Permanent exorcist = addCreatureReady(player1, new Exorcist());
        Permanent target = addCreatureReady(player2, new Squire());
        addWhiteAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black creature");
        assertThat(exorcist.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a black noncreature permanent")
    void cannotTargetBlackNoncreaturePermanent() {
        Permanent exorcist = addCreatureReady(player1, new Exorcist());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new DarkSphere());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CurseArtifact());
        target.setAttachedTo(artifact.getId());
        addWhiteAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black creature");
        assertThat(exorcist.isTapped()).isFalse();
    }

    private void addWhiteAbilityMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
