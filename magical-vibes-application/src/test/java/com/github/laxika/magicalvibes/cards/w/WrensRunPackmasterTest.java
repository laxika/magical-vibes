package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AvianChangeling;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WrensRunPackmasterTest extends BaseCardTest {

    private Permanent wolfOnBattlefield(java.util.UUID playerId) {
        return gd.playerBattlefields.get(playerId).stream()
                .filter(p -> p.getCard().getName().equals("Wolf"))
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("Champion ETB auto-sacrifices when no other Elf is controlled")
    void championAutoSacrificesWithoutElf() {
        harness.setHand(player1, List.of(new WrensRunPackmaster()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> champion ETB on stack
        harness.passBothPriorities(); // resolve champion ETB -> no Elf -> sacrifice

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Wren's Run Packmaster"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Champion ETB prompts a choice when another Elf is controlled")
    void championPromptsChoiceWithElf() {
        harness.addToBattlefield(player1, new AvianChangeling()); // counts as an Elf
        harness.setHand(player1, List.of(new WrensRunPackmaster()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> champion ETB on stack
        harness.passBothPriorities(); // resolve champion ETB -> permanent choice

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Wren's Run Packmaster"));
    }

    @Test
    @DisplayName("Activated ability creates a 2/2 green Wolf token")
    void activatedAbilityCreatesWolfToken() {
        harness.addToBattlefield(player1, new WrensRunPackmaster());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // resolve the token-creation ability

        Permanent wolf = wolfOnBattlefield(player1.getId());
        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(2);
    }

    @Test
    @DisplayName("Wolves the controller controls gain deathtouch, other creatures do not")
    void wolvesGainDeathtouch() {
        Permanent packmaster = harness.addToBattlefieldAndReturn(player1, new WrensRunPackmaster());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent wolf = wolfOnBattlefield(player1.getId());
        assertThat(gqs.hasKeyword(gd, wolf, Keyword.DEATHTOUCH)).isTrue();
        // The Packmaster itself is an Elf, not a Wolf.
        assertThat(gqs.hasKeyword(gd, packmaster, Keyword.DEATHTOUCH)).isFalse();
    }
}
