package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.v.VernadiShieldmate;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ConclaveGuildmage.class, VernadiShieldmate.class})
class ConclaveGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("First ability gives trample to creatures you control, including itself")
    void firstAbilityGrantsTrampleToOwnCreatures() {
        Permanent guildmage = addCreatureReady(player1, new ConclaveGuildmage());
        Permanent ownShieldmate = harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate());
        Permanent opponentShieldmate = harness.addToBattlefieldAndReturn(player2, new VernadiShieldmate());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, guildmage, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownShieldmate, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentShieldmate, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("First ability wears off at end of turn")
    void trampleWearsOffAtEndOfTurn() {
        Permanent guildmage = addCreatureReady(player1, new ConclaveGuildmage());
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
        addCreatureReady(player1, new ConclaveGuildmage());
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

    @Test
    @DisplayName("Trample applies to creatures present at resolution, but not those entering later")
    void trampleUsesCreaturesPresentAtResolution() {
        addCreatureReady(player1, new ConclaveGuildmage());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, 0, null, null);

        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new ConclaveGuildmage());
        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new ConclaveGuildmage());

        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Token ability resolves even after the guildmage leaves the battlefield")
    void tokenAbilityResolvesWithoutSource() {
        Permanent guildmage = addCreatureReady(player1, new ConclaveGuildmage());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(guildmage.isTapped()).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(guildmage);
        gd.playerGraveyards.get(player1.getId()).add(guildmage.getCard());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent token = findPermanent(player1, "Elf Knight");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Both abilities require the guildmage to be free of summoning sickness")
    void bothAbilitiesRequireNoSummoningSickness() {
        Permanent guildmage = harness.addToBattlefieldAndReturn(player1, new ConclaveGuildmage());
        guildmage.setSummoningSick(true);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("summoning sickness");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("summoning sickness");
        assertThat(guildmage.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
