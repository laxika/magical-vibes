package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AxebaneBeast;
import com.github.laxika.magicalvibes.cards.f.FootlightFiend;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.cards.s.SenateGuildmage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TomeOfTheGuildpact.class, SenateGuildmage.class, AxebaneBeast.class, FootlightFiend.class})
class TomeOfTheGuildpactTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a multicolored spell draws a card")
    void multicoloredSpellDrawsCard() {
        harness.addToBattlefield(player1, new TomeOfTheGuildpact());
        harness.castFromHand(player1, new SenateGuildmage(), "{W}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Casting a monocolored spell does not draw a card")
    void monocoloredSpellDoesNotDrawCard() {
        harness.addToBattlefield(player1, new TomeOfTheGuildpact());
        harness.castFromHand(player1, new AxebaneBeast(), "{3}{G}");

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The mana ability adds one mana of the chosen color")
    void addsManaOfChosenColor() {
        harness.addToBattlefield(player1, new TomeOfTheGuildpact());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("A hybrid spell triggers even when paid for with only one color")
    void hybridSpellDrawsCard() {
        harness.addToBattlefield(player1, new TomeOfTheGuildpact());
        harness.setLibrary(player1, List.of(new AxebaneBeast()));
        harness.setHand(player1, List.of(new FootlightFiend()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Axebane Beast");
        harness.assertNotOnBattlefield(player1, "Footlight Fiend");
    }

    @Test
    @DisplayName("Opponent's multicolored spells do not trigger the Tome")
    void opponentSpellDoesNotDrawCard() {
        harness.addToBattlefield(player1, new TomeOfTheGuildpact());
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new SenateGuildmage(), "{W}{U}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A colorless spell does not trigger the Tome even with multiple colors spent")
    void colorlessSpellDoesNotDrawCard() {
        harness.addToBattlefield(player1, new TomeOfTheGuildpact());
        harness.setHand(player1, List.of(new TomeOfTheGuildpact()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castArtifact(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Every multicolored spell triggers, including while the Tome is tapped")
    void repeatedSpellsTriggerTappedTome() {
        harness.addToBattlefield(player1, new TomeOfTheGuildpact());
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");
        harness.setLibrary(player1, List.of(new AxebaneBeast(), new AxebaneBeast()));
        harness.setHand(player1, List.of(new SenateGuildmage(), new SenateGuildmage()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(countPermanents(player1, "Senate Guildmage")).isEqualTo(2);
    }

    @Test
    @CardUsed(MycosynthLattice.class)
    @DisplayName("Mycosynth Lattice makes a normally multicolored spell colorless so it does not trigger")
    void latticePreventsMulticoloredTrigger() {
        harness.addToBattlefield(player1, new TomeOfTheGuildpact());
        harness.addToBattlefield(player2, new MycosynthLattice());

        harness.castFromHand(player1, new SenateGuildmage(), "{W}{U}");

        assertThat(gd.stack).hasSize(1);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLACK", "RED", "GREEN"})
    @DisplayName("The mana ability can produce each remaining color without using the stack")
    void addsOtherManaColorsImmediately(ManaColor color) {
        harness.addToBattlefield(player1, new TomeOfTheGuildpact());

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
