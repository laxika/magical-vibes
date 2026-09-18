package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DwynensElite;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThranduilTheStrategist.class, DwynensElite.class, GrizzlyBears.class, Forest.class})
class ThranduilTheStrategistTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall creates a 1/1 green Elf token")
    void landfallCreatesElfToken() {
        harness.addToBattlefield(player1, new ThranduilTheStrategist());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.ELF);
    }

    @Test
    @DisplayName("Other Elves you control can add green or blue mana")
    void grantsManaAbilityToOtherElves() {
        Permanent firstElf = addCreatureReady(player1, new DwynensElite());
        Permanent secondElf = addCreatureReady(player1, new DwynensElite());
        Permanent thranduil = addCreatureReady(player1, new ThranduilTheStrategist());
        Permanent nonElf = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentsElf = addCreatureReady(player2, new DwynensElite());

        assertThat(gs.getEffectiveActivatedAbilities(gd, firstElf)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, secondElf)).hasSize(1);
        assertThat(gs.getEffectiveActivatedAbilities(gd, thranduil)).isEmpty();
        assertThat(gs.getEffectiveActivatedAbilities(gd, nonElf)).isEmpty();
        assertThat(gs.getEffectiveActivatedAbilities(gd, opponentsElf)).isEmpty();

        int firstElfIndex = gd.playerBattlefields.get(player1.getId()).indexOf(firstElf);
        harness.activateAbility(player1, firstElfIndex, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        int secondElfIndex = gd.playerBattlefields.get(player1.getId()).indexOf(secondElf);
        harness.activateAbility(player1, secondElfIndex, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }
}
