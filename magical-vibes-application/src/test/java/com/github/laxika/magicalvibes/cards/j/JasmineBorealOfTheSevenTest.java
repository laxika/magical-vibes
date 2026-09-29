package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.GameLogEntry;
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
        harness.addToBattlefield(player1, new JasmineBorealOfTheSeven());
        gd.playerBattlefields.get(player1.getId()).getFirst().setSummoningSick(false);

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

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
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

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("declares 1 blocker"));
    }

    private void addJasmineMana() {
        harness.addToBattlefield(player1, new JasmineBorealOfTheSeven());
        gd.playerBattlefields.get(player1.getId()).getFirst().setSummoningSick(false);
        harness.activateAbility(player1, 0, 0, null, null);
    }
}
