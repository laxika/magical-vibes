package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CentaursHerald.class})
class CentaursHeraldTest extends BaseCardTest {

    @Test
    @DisplayName("Activating sacrifices the Herald and resolving creates a 3/3 Centaur token")
    void createsCentaurToken() {
        addReadyHerald(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Centaur's Herald");
        harness.assertInGraveyard(player1, "Centaur's Herald");

        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Centaur"))
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutEnoughMana() {
        addReadyHerald(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate while summoning sick (no tap cost)")
    void canActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new CentaursHerald());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addReadyHerald(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new CentaursHerald());
        perm.setSummoningSick(false);
        return perm;
    }

    @Test
    @DisplayName("Token is created only on resolution with the specified characteristics")
    void tokenCharacteristicsAndTiming() {
        harness.addToBattlefield(player1, new CentaursHerald());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player1, "Centaur's Herald");

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent token = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.CENTAUR);
        assertThat(token.getCard().getPower()).isEqualTo(3);
        assertThat(token.getCard().getToughness()).isEqualTo(3);
        assertThat(token.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Tapped Herald can still be sacrificed to create a token")
    void canActivateWhileTapped() {
        Permanent herald = harness.addToBattlefieldAndReturn(player1, new CentaursHerald());
        herald.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Centaur's Herald");
        harness.assertOnBattlefield(player1, "Centaur");
    }

    @Test
    @DisplayName("Three mana without green cannot pay the activation cost")
    void requiresGreenMana() {
        harness.addToBattlefield(player1, new CentaursHerald());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Centaur's Herald");
        harness.assertNotInGraveyard(player1, "Centaur's Herald");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The nonactive player can activate and receives the token")
    void nonactivePlayerReceivesToken() {
        harness.addToBattlefield(player2, new CentaursHerald());
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.passPriority(player1);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Centaur's Herald");
        harness.assertOnBattlefield(player2, "Centaur");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }
}
