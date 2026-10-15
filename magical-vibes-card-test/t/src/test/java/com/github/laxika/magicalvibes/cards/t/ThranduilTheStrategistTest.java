package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DwynensElite;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProwessOfTheFair;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThranduilTheStrategist.class, DwynensElite.class, GrizzlyBears.class, Forest.class,
        ProwessOfTheFair.class})
class ThranduilTheStrategistTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall creates a 1/1 green Elf token")
    void landfallCreatesElfToken() {
        harness.addToBattlefield(player1, new ThranduilTheStrategist());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Elf");
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

    @Test
    @DisplayName("Other noncreature Elves can tap for mana immediately")
    void grantsManaAbilityToNoncreatureElf() {
        harness.addToBattlefield(player1, new ThranduilTheStrategist());
        Permanent elfEnchantment = harness.addToBattlefieldAndReturn(player1, new ProwessOfTheFair());

        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(elfEnchantment.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's land does not trigger landfall")
    void opponentsLandDoesNotCreateToken() {
        harness.addToBattlefield(player1, new ThranduilTheStrategist());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Forest()));

        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Elf")).isZero();
        assertThat(countPermanents(player2, "Elf")).isZero();
    }

    @Test
    @DisplayName("Landfall tokens gain the mana ability but must overcome summoning sickness")
    void landfallTokenCanProduceManaOnceReady() {
        harness.addToBattlefield(player1, new ThranduilTheStrategist());
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        Permanent token = findPermanent(player1, "Elf");
        int tokenIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);

        assertThatThrownBy(() ->
                harness.activateAbility(player1, tokenIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        token.setSummoningSick(false);
        harness.activateAbility(player1, tokenIndex, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(token.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
