package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RaidingSchemesTest extends BaseCardTest {

    @Test
    @DisplayName("Grants conspire to a noncreature spell")
    void grantsConspireToNoncreatureSpell() {
        harness.addToBattlefield(player1, new RaidingSchemes());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castWithConspire(player1, 0, target.getId(), List.of(conspireA.getId(), conspireB.getId()));

        assertThat(conspireA.isTapped()).isTrue();
        assertThat(conspireB.isTapped()).isTrue();
        assertThat(gd.stack).anyMatch(entry -> entry.getEffectsToResolve().stream()
                .anyMatch(effect -> effect instanceof CopyControllerCastSpellEffect));
    }

    @Test
    @DisplayName("Does not grant conspire to a creature spell")
    void doesNotGrantConspireToCreatureSpell() {
        harness.addToBattlefield(player1, new RaidingSchemes());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castWithConspire(player1, 0, null,
                List.of(conspireA.getId(), conspireB.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(conspireA.isTapped()).isFalse();
        assertThat(conspireB.isTapped()).isFalse();
    }
}
