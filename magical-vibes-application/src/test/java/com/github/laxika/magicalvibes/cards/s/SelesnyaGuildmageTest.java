package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SelesnyaGuildmage.class})
class SelesnyaGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("First ability creates a 1/1 green Saproling token")
    void firstAbilityCreatesSaprolingToken() {
        addReadyGuildmage(player1);
        addMana(ManaColor.COLORLESS, 3);
        addMana(ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        List<Permanent> tokens = findPermanents(player1, "Saproling");
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(tokens.getFirst().getCard().getColors()).containsExactly(CardColor.GREEN);
        assertThat(tokens.getFirst().getCard().getSubtypes()).containsExactly(CardSubtype.SAPROLING);
        assertThat(tokens.getFirst().getEffectivePower()).isEqualTo(1);
        assertThat(tokens.getFirst().getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A summoning-sick tapped Guildmage can repeatedly create tokens")
    void tokenAbilityDoesNotRequireTappingOrHaste() {
        Permanent guildmage = addReadyGuildmage(player1);
        guildmage.setSummoningSick(true);
        guildmage.setTapped(true);
        addMana(ManaColor.COLORLESS, 6);
        addMana(ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Saproling")).hasSize(2);
        assertThat(findPermanents(player2, "Saproling")).isEmpty();
    }

    @Test
    @DisplayName("Boost includes tokens created in response and repeated boosts accumulate")
    void boostUsesCreaturesPresentOnResolutionAndStacks() {
        Permanent guildmage = addReadyGuildmage(player1);
        addMana(ManaColor.COLORLESS, 9);
        addMana(ManaColor.GREEN, 1);
        addMana(ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Saproling");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, guildmage)).isEqualTo(2);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, guildmage)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, guildmage)).isEqualTo(4);
    }

    @Test
    @DisplayName("Second ability boosts creatures you control until end of turn")
    void secondAbilityBoostsOwnCreaturesUntilEndOfTurn() {
        addReadyGuildmage(player1);
        Permanent ownGuildmage = addReadyGuildmage(player1);
        Permanent opposingGuildmage = addReadyGuildmage(player2);
        addMana(ManaColor.COLORLESS, 3);
        addMana(ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownGuildmage)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownGuildmage)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opposingGuildmage)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingGuildmage)).isEqualTo(2);

        Permanent laterGuildmage = addReadyGuildmage(player1);
        assertThat(gqs.getEffectivePower(gd, laterGuildmage)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterGuildmage)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownGuildmage)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownGuildmage)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opposingGuildmage)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingGuildmage)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, laterGuildmage)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterGuildmage)).isEqualTo(2);
    }

    private Permanent addReadyGuildmage(Player player) {
        return addCreatureReady(player, new SelesnyaGuildmage());
    }

    private void addMana(ManaColor color, int amount) {
        harness.addMana(player1, color, amount);
    }
}
