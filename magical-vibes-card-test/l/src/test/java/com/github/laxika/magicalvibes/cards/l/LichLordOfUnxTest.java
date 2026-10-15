package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LichLordOfUnx.class})
class LichLordOfUnxTest extends BaseCardTest {

    @Test
    @DisplayName("{U}{B}, {T}: creates a 1/1 blue and black Zombie Wizard token")
    void tokenAbilityCreatesZombieWizard() {
        addLordReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ZOMBIE, CardSubtype.WIZARD);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLUE, CardColor.BLACK);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("{U}{U}{B}{B}: with only the Lord (one Zombie), target loses 1 life and mills 1")
    void drainScalesWithOneZombie() {
        addLordReady(player1);
        harness.setLibrary(player2, List.of(new LichLordOfUnx(), new LichLordOfUnx(), new LichLordOfUnx()));
        addManaForDrain(player1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("{U}{U}{B}{B}: Zombie Wizard tokens raise X — target loses 2 life and mills 2")
    void drainScalesWithTokens() {
        addLordReady(player1);
        harness.setLibrary(player2, List.of(new LichLordOfUnx(), new LichLordOfUnx(), new LichLordOfUnx()));

        // Create one Zombie Wizard token first (Lord + token = two Zombies controlled).
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        addManaForDrain(player1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Drain ability can target its own controller")
    void drainCanTargetSelf() {
        addLordReady(player1);
        harness.setLibrary(player1, List.of(new LichLordOfUnx(), new LichLordOfUnx()));
        addManaForDrain(player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Drain ability requires {U}{U}{B}{B}")
    void drainRequiresMana() {
        addLordReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tokenAbilityCannotBeActivatedWhileSummoningSick() {
        harness.addToBattlefield(player1, new LichLordOfUnx());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void drainWorksWhileSummoningSickAndTappedAndIgnoresOpponentsZombies() {
        Permanent lord = harness.addToBattlefieldAndReturn(player1, new LichLordOfUnx());
        lord.tap();
        harness.addToBattlefield(player2, new LichLordOfUnx());
        harness.setLibrary(player2, List.of(new LichLordOfUnx(), new LichLordOfUnx()));
        addManaForDrain(player1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    void drainCountsZombiesAtResolution() {
        addLordReady(player1);
        harness.setLibrary(player2, List.of(new LichLordOfUnx(), new LichLordOfUnx()));
        addManaForDrain(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void drainWithNoRemainingZombiesDoesNothing() {
        addLordReady(player1);
        harness.setLibrary(player2, List.of(new LichLordOfUnx()));
        addManaForDrain(player1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    void drainLosesFullLifeAmountWhenLibraryIsTooSmall() {
        addLordReady(player1);
        harness.addToBattlefield(player1, new LichLordOfUnx());
        harness.setLibrary(player2, List.of(new LichLordOfUnx()));
        addManaForDrain(player1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, lifeBefore - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    private void addLordReady(Player player) {
        harness.addToBattlefieldAndReturn(player, new LichLordOfUnx()).setSummoningSick(false);
    }

    private void addManaForDrain(Player player) {
        harness.addMana(player, ManaColor.BLUE, 2);
        harness.addMana(player, ManaColor.BLACK, 2);
    }
}
