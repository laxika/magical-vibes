package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RakkaMar.class})
class RakkaMarTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts token creation on the stack")
    void activatingAbilityPutsOnStack() {
        addRakkaMarReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving ability creates a 3/1 red Elemental token")
    void resolvingAbilityCreatesToken() {
        addRakkaMarReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent token = token(player1);
        assertThat(gqs.isCreature(gd, token)).isTrue();
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Elemental token has haste")
    void tokenHasHaste() {
        addRakkaMarReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, token(player1), Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Activating the ability taps Rakka Mar")
    void activatingTapsSource() {
        Permanent rakka = addRakkaMarReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(rakka.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate the ability again while tapped")
    void cannotActivateWhileTapped() {
        addRakkaMarReady(player1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addRakkaMarReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Haste allows Rakka Mar to activate immediately after entering")
    void canActivateImmediatelyAfterEntering() {
        Permanent rakka = harness.enterBattlefieldAndReturn(player1, new RakkaMar());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(rakka.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(token(player1).getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Creates exactly one untapped red Elemental without consuming Rakka Mar")
    void createsOneUntappedRedElemental() {
        Permanent rakka = addRakkaMarReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2).contains(rakka);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        Permanent elemental = token(player1);
        assertThat(elemental.getCard().isToken()).isTrue();
        assertThat(elemental.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(elemental.getCard().getSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
        assertThat(elemental.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Second player creates the token under their control during the opponent's turn")
    void secondPlayerCreatesTokenDuringOpponentsTurn() {
        addRakkaMarReady(player2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(2);
        assertThat(token(player2).getCard().isToken()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    private Permanent addRakkaMarReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new RakkaMar());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent token(Player player) {
        return findPermanent(player, "Elemental");
    }
}
