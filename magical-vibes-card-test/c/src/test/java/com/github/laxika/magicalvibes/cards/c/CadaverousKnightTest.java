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
        assertThat(gd.regenerationShieldCount(knight.getId())).isEqualTo(1);
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
        assertThat(gd.regenerationShieldCount(knight.getId())).isEqualTo(0);
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

        assertThat(gqs.getEffectivePower(gd, blocker)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(3);
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

        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Regeneration can be activated while tapped and summoning sick")
    void regenerationDoesNotRequireTappingOrHaste() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new CadaverousKnight());
        knight.setSummoningSick(true);
        knight.tap();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.regenerationShieldCount(knight.getId())).isEqualTo(1);
        assertThat(knight.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Flanking triggers independently for each non-flanking blocker")
    void flankingShrinksEachBlocker() {
        Permanent knight = addKnightReady(player1);
        knight.setAttacking(true);
        Permanent first = addCreatureReady(player2, new FemerefScouts());
        Permanent second = addCreatureReady(player2, new FemerefScouts());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, first)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, second)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
    }

    @Test
    @DisplayName("Flanking does not shrink a creature that the Knight blocks")
    void flankingDoesNotTriggerWhenBlocking() {
        addKnightReady(player1);
        Permanent attacker = addCreatureReady(player2, new FemerefScouts());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Cadaverous Knight");
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
