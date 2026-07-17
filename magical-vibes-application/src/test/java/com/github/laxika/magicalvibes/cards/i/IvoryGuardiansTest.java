package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IvoryGuardiansTest extends BaseCardTest {

    private static Card createCreature(String name, int power, int toughness, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    // ===== Conditional boost =====

    @Test
    @DisplayName("Base 3/3 when no opponent controls a nontoken red permanent")
    void baseWithoutRedPermanent() {
        harness.addToBattlefield(player1, new IvoryGuardians());

        Permanent guardians = findPermanent(player1, "Ivory Guardians");
        assertThat(gqs.getEffectivePower(gd, guardians)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, guardians)).isEqualTo(3);
    }

    @Test
    @DisplayName("Gets +1/+1 (4/4) when an opponent controls a nontoken red permanent")
    void boostWhenOpponentControlsRedPermanent() {
        harness.addToBattlefield(player1, new IvoryGuardians());
        harness.addToBattlefield(player2, createCreature("Fire Elemental", 3, 3, CardColor.RED));

        Permanent guardians = findPermanent(player1, "Ivory Guardians");
        assertThat(gqs.getEffectivePower(gd, guardians)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, guardians)).isEqualTo(4);
    }

    @Test
    @DisplayName("No boost when the opponent's red permanent is a token")
    void noBoostWhenRedPermanentIsToken() {
        harness.addToBattlefield(player1, new IvoryGuardians());
        Card token = createCreature("Goblin Token", 1, 1, CardColor.RED);
        token.setToken(true);
        harness.addToBattlefield(player2, token);

        Permanent guardians = findPermanent(player1, "Ivory Guardians");
        assertThat(gqs.getEffectivePower(gd, guardians)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, guardians)).isEqualTo(3);
    }

    @Test
    @DisplayName("No boost when the opponent's nontoken permanent is not red")
    void noBoostWhenPermanentNotRed() {
        harness.addToBattlefield(player1, new IvoryGuardians());
        harness.addToBattlefield(player2, createCreature("Grizzly Bears", 2, 2, CardColor.GREEN));

        Permanent guardians = findPermanent(player1, "Ivory Guardians");
        assertThat(gqs.getEffectivePower(gd, guardians)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, guardians)).isEqualTo(3);
    }

    @Test
    @DisplayName("The controller's own red permanent does not grant the boost")
    void noBoostFromOwnRedPermanent() {
        harness.addToBattlefield(player1, new IvoryGuardians());
        harness.addToBattlefield(player1, createCreature("Fire Elemental", 3, 3, CardColor.RED));

        Permanent guardians = findPermanent(player1, "Ivory Guardians");
        assertThat(gqs.getEffectivePower(gd, guardians)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, guardians)).isEqualTo(3);
    }

    // ===== Protection from red =====

    @Test
    @DisplayName("Red creature cannot block Ivory Guardians (protection from red)")
    void redCreatureCannotBlock() {
        Permanent attacker = new Permanent(new IvoryGuardians());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        Permanent blocker = new Permanent(createCreature("Goblin Raider", 2, 2, CardColor.RED));
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }
}
