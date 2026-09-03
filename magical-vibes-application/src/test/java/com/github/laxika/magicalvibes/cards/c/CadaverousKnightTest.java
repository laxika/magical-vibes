package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FemerefScouts;
import com.github.laxika.magicalvibes.cards.z.ZhalfirinKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CadaverousKnight.class, FemerefScouts.class, ZhalfirinKnight.class})
class CadaverousKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the activated ability grants a regeneration shield")
    void resolvingRegenGrantsShield() {
        addKnightReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent knight = findPermanent(player1, "Cadaverous Knight");
        assertThat(knight.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration ability requires two black mana")
    void regenerationRequiresTwoBlackMana() {
        addKnightReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Regeneration shield saves Cadaverous Knight from lethal combat damage")
    void regenSavesFromLethalCombat() {
        addKnightReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player2, 5, 5);
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Cadaverous Knight");
        Permanent knight = findPermanent(player1, "Cadaverous Knight");
        assertThat(knight.isTapped()).isTrue();
        assertThat(knight.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cadaverous Knight dies without a regeneration shield")
    void diesWithoutRegenShield() {
        addKnightReady(player1);

        Permanent attacker = addCreatureReady(player2, 5, 5);
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Cadaverous Knight");
        harness.assertInGraveyard(player1, "Cadaverous Knight");
    }

    @Test
    @DisplayName("Flanking gives a non-flanking blocker -1/-1 until end of turn")
    void flankingShrinksNonFlankingBlocker() {
        Permanent knight = addKnightReady(player1);
        knight.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FemerefScouts());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);

        assertThat(blocker.getEffectivePower()).isZero();
        assertThat(blocker.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Flanking does not affect a blocker that also has flanking")
    void flankingDoesNotShrinkFlankingBlocker() {
        Permanent knight = addKnightReady(player1);
        knight.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new ZhalfirinKnight());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);

        assertThat(blocker.getEffectivePower()).isEqualTo(2);
        assertThat(blocker.getEffectiveToughness()).isEqualTo(2);
    }

    private Permanent addKnightReady(Player player) {
        return addCreatureReady(player, new CadaverousKnight());
    }

    private Permanent addCreatureReady(Player player, int power, int toughness) {
        FemerefScouts card = new FemerefScouts();
        card.setPower(power);
        card.setToughness(toughness);
        return addCreatureReady(player, card);
    }
}
