package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JasmineBorealOfTheSeven.class, GrizzlyBears.class, LlanowarElves.class, AirElemental.class})
class JasmineBorealOfTheSevenTest extends BaseCardTest {

    @Test
    @DisplayName("Tap ability adds green and white mana restricted to abilityless creature spells")
    void tapAbilityAddsRestrictedMana() {
        addCreatureReady(player1, new JasmineBorealOfTheSeven());

        harness.activateAbility(player1, 0, 0, null, null);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getCreatureSpellWithoutAbilitiesOnlyMana(ManaColor.GREEN)).isEqualTo(1);
        assertThat(pool.getCreatureSpellWithoutAbilitiesOnlyMana(ManaColor.WHITE)).isEqualTo(1);
        assertThat(pool.get(ManaColor.GREEN)).isZero();
        assertThat(pool.get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Restricted mana casts a creature with no abilities")
    void restrictedManaCastsAbilitylessCreature() {
        addJasmineMana();
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellWithoutAbilitiesOnlyManaTotal())
                .isZero();
    }

    @Test
    @DisplayName("Restricted mana cannot cast a creature with an ability")
    void restrictedManaCannotCastCreatureWithAbility() {
        addJasmineMana();
        harness.setHand(player1, List.of(new LlanowarElves()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Abilityless creatures you control cannot be blocked by creatures with abilities")
    void abilitylessCreatureCannotBeBlockedByCreatureWithAbility() {
        harness.addToBattlefield(player1, new JasmineBorealOfTheSeven());
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        addCreatureReady(player2, new AirElemental());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Creatures with abilities can't block creatures you control with no abilities");
    }

    @Test
    @DisplayName("A creature without abilities can block an abilityless creature")
    void abilitylessCreatureCanBlockAbilitylessCreature() {
        harness.addToBattlefield(player1, new JasmineBorealOfTheSeven());
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
    }

    @Test
    @DisplayName("Keyword abilities prevent spending Jasmine's mana")
    void restrictedManaCannotCastCreatureWithKeyword() {
        addJasmineMana();
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new AirElemental()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getCreatureSpellWithoutAbilitiesOnlyManaTotal())
                .isEqualTo(2);
    }

    @Test
    @DisplayName("A blocker with a mana ability cannot block an abilityless creature")
    void manaAbilityPreventsBlocking() {
        harness.addToBattlefield(player1, new JasmineBorealOfTheSeven());
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        addCreatureReady(player2, new LlanowarElves());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Creatures with abilities can't block creatures you control with no abilities");
    }

    @Test
    @DisplayName("Jasmine does not protect attacking creatures with abilities")
    void creatureWithAbilityCanBeBlocked() {
        harness.addToBattlefield(player1, new JasmineBorealOfTheSeven());
        addCreatureReady(player1, new LlanowarElves()).setAttacking(true);
        addCreatureReady(player2, new LlanowarElves());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
    }

    @Test
    @DisplayName("Jasmine does not protect opponents' abilityless creatures")
    void opponentsAbilitylessCreatureCanBeBlocked() {
        harness.addToBattlefield(player2, new JasmineBorealOfTheSeven());
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        addCreatureReady(player2, new LlanowarElves());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
    }

    private void addJasmineMana() {
        addCreatureReady(player1, new JasmineBorealOfTheSeven());
        harness.activateAbility(player1, 0, 0, null, null);
    }
}
