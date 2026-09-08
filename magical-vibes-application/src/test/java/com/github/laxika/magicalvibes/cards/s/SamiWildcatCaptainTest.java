package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JhoirasFamiliar;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SamiWildcatCaptain.class, GrizzlyBears.class, JhoirasFamiliar.class, SylvokLifestaff.class})
class SamiWildcatCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Spells you cast cost one less for each artifact you control")
    void reducesSpellsByNumberOfArtifactsControlled() {
        harness.addToBattlefield(player1, new SamiWildcatCaptain());
        harness.addToBattlefield(player1, new SylvokLifestaff());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The reduction scales with additional artifacts")
    void reductionScalesWithArtifacts() {
        harness.addToBattlefield(player1, new SamiWildcatCaptain());
        harness.addToBattlefield(player1, new SylvokLifestaff());
        harness.addToBattlefield(player1, new SylvokLifestaff());
        harness.setHand(player1, List.of(new JhoirasFamiliar()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The reduction does not affect an opponent's spells")
    void doesNotReduceOpponentsSpells() {
        harness.addToBattlefield(player1, new SamiWildcatCaptain());
        harness.addToBattlefield(player1, new SylvokLifestaff());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
