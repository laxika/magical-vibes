package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConclaveGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("First ability gives trample to creatures you control, including itself")
    void firstAbilityGrantsTrampleToOwnCreatures() {
        Permanent guildmage = addReady(player1, new ConclaveGuildmage());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, guildmage, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("First ability wears off at end of turn")
    void trampleWearsOffAtEndOfTurn() {
        Permanent guildmage = addReady(player1, new ConclaveGuildmage());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, guildmage, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, guildmage, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Second ability creates a vigilant 2/2 green and white Elf Knight token")
    void secondAbilityCreatesElfKnightToken() {
        addReady(player1, new ConclaveGuildmage());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Elf Knight");
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.GREEN, CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ELF, CardSubtype.KNIGHT);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
