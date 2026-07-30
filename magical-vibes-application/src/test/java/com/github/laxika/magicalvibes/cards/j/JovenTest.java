package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.i.IronMyr;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JovenTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a target noncreature artifact")
    void destroysNoncreatureArtifact() {
        addCreatureReady(player1, new Joven());
        harness.addToBattlefield(player2, new Millstone());
        harness.addMana(player1, ManaColor.RED, 3);

        Permanent millstone = findPermanent(player2, "Millstone");
        harness.activateAbility(player1, 0, null, millstone.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Millstone");
    }

    @Test
    @DisplayName("Cannot target an artifact creature")
    void cannotTargetArtifactCreature() {
        addCreatureReady(player1, new Joven());
        harness.addToBattlefield(player2, new IronMyr());
        harness.addMana(player1, ManaColor.RED, 3);

        Permanent myr = findPermanent(player2, "Iron Myr");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, myr.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a noncreature artifact");
    }
}
