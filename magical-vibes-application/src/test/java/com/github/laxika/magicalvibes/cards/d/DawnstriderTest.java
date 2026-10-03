package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.k.KrisMage;
import com.github.laxika.magicalvibes.cards.l.LeylineOfPunishment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Dawnstrider.class, KrisMage.class, LeylineOfPunishment.class})
class DawnstriderTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Dawnstrider discards a card, pays green mana, and taps it")
    void activationPaysCosts() {
        Permanent dawnstrider = addCreatureReady(player1, new Dawnstrider());
        harness.setHand(player1, List.of(new Dawnstrider()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(dawnstrider.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.preventAllCombatDamage).isTrue();
    }

    @Test
    @DisplayName("Prevents combat damage after the ability resolves")
    void preventsCombatDamage() {
        addCreatureReady(player1, new Dawnstrider());
        harness.setHand(player1, List.of(new Dawnstrider()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.setLife(player1, 20);
        Permanent attacker = addCreatureReady(player2, new Dawnstrider());
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Prevents combat damage dealt to creatures as well as players")
    void preventsCombatDamageToCreatures() {
        addCreatureReady(player1, new Dawnstrider());
        harness.setHand(player1, List.of(new Dawnstrider()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player2, new Dawnstrider());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player1, new Dawnstrider());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(blocker);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        addCreatureReady(player1, new Dawnstrider());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("discard");
    }

    @Test
    @DisplayName("Cannot activate without green mana")
    void cannotActivateWithoutGreenMana() {
        addCreatureReady(player1, new Dawnstrider());
        harness.setHand(player1, List.of(new Dawnstrider()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    @Test
    @DisplayName("Cannot activate when tapped")
    void cannotActivateWhenTapped() {
        Permanent dawnstrider = addCreatureReady(player1, new Dawnstrider());
        dawnstrider.tap();
        harness.setHand(player1, List.of(new Dawnstrider()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new Dawnstrider());
        harness.setHand(player1, List.of(new Dawnstrider()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Costs are paid before the ability resolves")
    void costsArePaidBeforeResolution() {
        Permanent dawnstrider = addCreatureReady(player1, new Dawnstrider());
        harness.setHand(player1, List.of(new Dawnstrider()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);

        assertThat(dawnstrider.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Dawnstrider");
        assertThat(gd.preventAllCombatDamage).isFalse();

        harness.passBothPriorities();
        assertThat(gd.preventAllCombatDamage).isTrue();
    }

    @Test
    @DisplayName("Combat prevention does not prevent noncombat damage")
    void doesNotPreventNoncombatDamage() {
        addCreatureReady(player1, new Dawnstrider());
        addCreatureReady(player2, new KrisMage());
        harness.setHand(player1, List.of(new Dawnstrider()));
        harness.setHand(player2, List.of(new Dawnstrider()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.activateAbility(player2, 0, null, player1.getId());
        harness.handleCardChosen(player2, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("The ability resolves even if Dawnstrider dies in response")
    void resolvesAfterSourceDies() {
        Permanent dawnstrider = addCreatureReady(player1, new Dawnstrider());
        addCreatureReady(player2, new KrisMage());
        harness.setHand(player1, List.of(new Dawnstrider()));
        harness.setHand(player2, List.of(new Dawnstrider()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.activateAbility(player2, 0, null, dawnstrider.getId());
        harness.handleCardChosen(player2, 0);
        harness.withAutoStop(gd.currentStep, () -> {
            harness.passBothPriorities();
            harness.passBothPriorities();
        });
        harness.assertInGraveyard(player1, "Dawnstrider");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(dawnstrider);

        Permanent attacker = addCreatureReady(player2, new Dawnstrider());
        attacker.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Combat prevention expires at the end of the turn")
    void preventionExpiresAfterTurn() {
        addCreatureReady(player1, new Dawnstrider());
        harness.setHand(player1, List.of(new Dawnstrider()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        Permanent attacker = addCreatureReady(player2, new Dawnstrider());
        attacker.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Leyline of Punishment overrides Dawnstrider's combat prevention")
    void cannotPreventUnpreventableCombatDamage() {
        addCreatureReady(player1, new Dawnstrider());
        harness.addToBattlefield(player2, new LeylineOfPunishment());
        harness.setHand(player1, List.of(new Dawnstrider()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player2, new Dawnstrider());
        attacker.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 19);
    }
}
