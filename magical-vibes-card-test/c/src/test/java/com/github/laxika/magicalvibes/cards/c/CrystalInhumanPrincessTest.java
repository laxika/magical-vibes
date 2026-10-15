package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArcaneSignet;
import com.github.laxika.magicalvibes.cards.d.DromarsCharm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrystalInhumanPrincess.class, GrizzlyBears.class, Opt.class, ArcaneSignet.class, DromarsCharm.class})
class CrystalInhumanPrincessTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to the number of colors of a noncreature spell")
    void dealsDamageForEachSpellColor() {
        harness.addToBattlefield(player1, new CrystalInhumanPrincess());
        harness.setHand(player1, List.of(new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Creature spells do not trigger Crystal")
    void creatureSpellsDoNotTrigger() {
        harness.addToBattlefield(player1, new CrystalInhumanPrincess());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLife(player2, 20);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Tapping Crystal adds one mana of a chosen color")
    void tapsForChosenColor() {
        addCreatureReady(player1, new CrystalInhumanPrincess());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A colorless noncreature spell triggers without dealing damage")
    void colorlessSpellDealsNoDamage() {
        harness.addToBattlefield(player1, new CrystalInhumanPrincess());
        harness.setHand(player1, List.of(new ArcaneSignet()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setLife(player2, 20);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("A three-color spell deals three damage only to the opponent")
    void multicoloredSpellCountsDistinctColors() {
        harness.addToBattlefield(player1, new CrystalInhumanPrincess());
        harness.setHand(player1, List.of(new DromarsCharm()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castModalInstant(player1, 0, 0, List.of());
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.passBothPriorities();
        harness.assertLife(player1, 25);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger Crystal")
    void opponentsSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new CrystalInhumanPrincess());
        harness.setHand(player2, List.of(new Opt()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castInstant(player2, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"RED", "GREEN", "WHITE"})
    @DisplayName("The other permitted mana colors resolve immediately and tap Crystal")
    void tapsForOtherPermittedColors(ManaColor color) {
        var crystal = addCreatureReady(player1, new CrystalInhumanPrincess());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(crystal.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
