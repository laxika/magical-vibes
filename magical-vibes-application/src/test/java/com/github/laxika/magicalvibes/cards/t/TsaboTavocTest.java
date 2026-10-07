package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CaptainSisay;
import com.github.laxika.magicalvibes.cards.k.KavuTitan;
import com.github.laxika.magicalvibes.cards.k.KeldonNecropolis;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TsaboTavoc.class, CaptainSisay.class, KavuTitan.class, KeldonNecropolis.class})
class TsaboTavocTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Tsabo Tavoc destroys a target legendary creature")
    void destroysTargetLegendaryCreature() {
        Permanent tsaboTavoc = addTsaboTavoc();
        Permanent captainSisay = harness.addToBattlefieldAndReturn(player2, new CaptainSisay());
        captainSisay.setRegenerationShield(1);
        addBlackMana();

        harness.activateAbility(player1, 0, null, captainSisay.getId());
        assertThat(tsaboTavoc.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Captain Sisay");
        harness.assertInGraveyard(player2, "Captain Sisay");
    }

    @Test
    @DisplayName("Cannot target a nonlegendary creature")
    void cannotTargetNonlegendaryCreature() {
        addTsaboTavoc();
        Permanent kavuTitan = harness.addToBattlefieldAndReturn(player2, new KavuTitan());
        addBlackMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, kavuTitan.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
    }

    @Test
    @DisplayName("Cannot target a legendary noncreature permanent")
    void cannotTargetLegendaryNoncreaturePermanent() {
        addTsaboTavoc();
        Permanent keldonNecropolis = harness.addToBattlefieldAndReturn(player2, new KeldonNecropolis());
        addBlackMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, keldonNecropolis.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
    }

    @Test
    @DisplayName("Legendary creatures cannot block Tsabo Tavoc")
    void legendaryCreatureCannotBlock() {
        Permanent tsabo = addTsaboTavoc();
        tsabo.setAttacking(true);
        addCreatureReady(player2, new CaptainSisay());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("A nonlegendary blocker dies before dealing damage to Tsabo Tavoc")
    void firstStrikeKillsNonlegendaryBlockerBeforeNormalDamage() {
        Permanent tsabo = addTsaboTavoc();
        addCreatureReady(player2, new KavuTitan());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player2, "Kavu Titan");
        harness.assertOnBattlefield(player1, "Tsabo Tavoc");
        assertThat(tsabo.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Protection prevents combat damage from a legendary creature")
    void preventsDamageFromLegendaryCreature() {
        Permanent sisay = addCreatureReady(player2, new CaptainSisay());
        sisay.getCounters().put(CounterType.PLUS_ONE_PLUS_ONE, 6);
        Permanent tsabo = addTsaboTavoc();

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Tsabo Tavoc");
        harness.assertOnBattlefield(player2, "Captain Sisay");
        assertThat(tsabo.getMarkedDamage()).isZero();
        assertThat(sisay.getMarkedDamage()).isEqualTo(7);
    }

    @Test
    @DisplayName("Tsabo Tavoc cannot target itself because its source is a legendary creature")
    void cannotTargetItself() {
        Permanent tsabo = addTsaboTavoc();
        addBlackMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, tsabo.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Tsabo Tavoc can destroy another legendary creature its controller owns")
    void destroysOwnLegendaryCreature() {
        addTsaboTavoc();
        Permanent sisay = harness.addToBattlefieldAndReturn(player1, new CaptainSisay());
        addBlackMana();

        harness.activateAbility(player1, 0, null, sisay.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Captain Sisay");
        harness.assertInGraveyard(player1, "Captain Sisay");
    }

    private void addBlackMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
    }

    private Permanent addTsaboTavoc() {
        return addCreatureReady(player1, new TsaboTavoc());
    }
}
