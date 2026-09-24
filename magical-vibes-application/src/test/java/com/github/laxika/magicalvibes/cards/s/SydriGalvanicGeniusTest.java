package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ClayStatue;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SydriGalvanicGenius.class, Millstone.class, ClayStatue.class, GrizzlyBears.class})
class SydriGalvanicGeniusTest extends BaseCardTest {

    @Test
    @DisplayName("Animates a noncreature artifact with power and toughness equal to its mana value")
    void animatesNoncreatureArtifact() {
        addCreatureReady(player1, new SydriGalvanicGenius());
        harness.addToBattlefield(player1, new Millstone());
        harness.addMana(player1, ManaColor.BLUE, 1);

        Permanent millstone = findPermanent(player1, "Millstone");
        harness.activateAbility(player1, 0, 0, null, millstone.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, millstone)).isTrue();
        assertThat(gqs.isArtifact(gd, millstone)).isTrue();
        assertThat(gqs.getEffectivePower(gd, millstone)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, millstone)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, millstone)).isFalse();
        assertThat(gqs.isArtifact(gd, millstone)).isTrue();
    }

    @Test
    @DisplayName("Grants deathtouch and lifelink to an artifact creature until end of turn")
    void grantsDeathtouchAndLifelink() {
        addCreatureReady(player1, new SydriGalvanicGenius());
        harness.addToBattlefield(player1, new ClayStatue());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent clayStatue = findPermanent(player1, "Clay Statue");
        harness.activateAbility(player1, 0, 1, null, clayStatue.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, clayStatue, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, clayStatue, Keyword.LIFELINK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, clayStatue, Keyword.DEATHTOUCH)).isFalse();
        assertThat(gqs.hasKeyword(gd, clayStatue, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Restricts each ability to its required artifact type")
    void rejectsIllegalTargets() {
        addCreatureReady(player1, new SydriGalvanicGenius());
        harness.addToBattlefield(player1, new ClayStatue());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        Permanent clayStatue = findPermanent(player1, "Clay Statue");
        Permanent grizzlyBears = findPermanent(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, clayStatue.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a noncreature artifact");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, grizzlyBears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact creature");
    }
}
