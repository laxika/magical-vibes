package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThievesTools.class, GrizzlyBears.class, CrawWurm.class})
class ThievesToolsTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Treasure token when it enters the battlefield")
    void createsTreasureOnEnter() {
        harness.castFromHand(player1, new ThievesTools(), "{1}{B}");
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(
                permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.TREASURE)
        ).hasSize(1);
    }

    @Test
    @DisplayName("Equip attaches Thieves' Tools to a creature you control")
    void equipsCreature() {
        Permanent tools = addToolsReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(tools.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("An equipped creature with power 3 or less can't be blocked")
    void lowPowerEquippedCreatureCantBeBlocked() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent tools = addToolsReady(player1);
        tools.setAttachedTo(attacker.getId());
        addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("An equipped creature with power greater than 3 can be blocked")
    void highPowerEquippedCreatureCanBeBlocked() {
        Permanent attacker = addCreatureReady(player1, new CrawWurm());
        Permanent tools = addToolsReady(player1);
        tools.setAttachedTo(attacker.getId());
        addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("An equipped creature at exactly 3 power cannot be blocked")
    void exactlyThreePowerCantBeBlocked() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setPowerModifier(1);
        Permanent tools = addToolsReady(player1);
        tools.setAttachedTo(attacker.getId());
        addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Increasing equipped creature's power above 3 allows blocking")
    void powerIncreaseAllowsBlocking() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent tools = addToolsReady(player1);
        tools.setAttachedTo(attacker.getId());
        assertThat(harness.getGameQueryService().hasCantBeBlocked(gd, attacker)).isTrue();
        attacker.setPowerModifier(2);
        addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Reducing equipped creature's power to 3 prevents blocking")
    void powerDecreasePreventsBlocking() {
        Permanent attacker = addCreatureReady(player1, new CrawWurm());
        Permanent tools = addToolsReady(player1);
        tools.setAttachedTo(attacker.getId());
        assertThat(harness.getGameQueryService().hasCantBeBlocked(gd, attacker)).isFalse();
        attacker.setPowerModifier(-3);
        addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Unequipped Tools do not prevent blocking a low-power creature")
    void unequippedCreatureCanBeBlocked() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addToolsReady(player1);
        addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        Permanent tools = addToolsReady(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(tools.getAttachedTo()).isNull();
    }

    private Permanent addToolsReady(Player player) {
        Permanent tools = harness.addToBattlefieldAndReturn(player, new ThievesTools());
        tools.setSummoningSick(false);
        return tools;
    }
}
