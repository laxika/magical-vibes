package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.c.CatharCommando;
import com.github.laxika.magicalvibes.cards.p.PrimalAdversary;
import com.github.laxika.magicalvibes.cards.v.VillageWatch;
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

@CardUsed({KatildaDawnhartPrime.class, CatharCommando.class, PrimalAdversary.class, VillageWatch.class})
class KatildaDawnhartPrimeTest extends BaseCardTest {

    @Test
    @DisplayName("Human creatures gain a mana ability limited to their own colors")
    void humanCreaturesAddManaOfTheirColors() {
        Permanent katilda = addCreatureReady(player1, new KatildaDawnhartPrime());
        Permanent human = addCreatureReady(player1, new CatharCommando());

        harness.activateAbility(player1, battlefieldIndex(katilda), 1, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(katilda.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();

        harness.activateAbility(player1, battlefieldIndex(human), 1, null, null);

        assertThat(human.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Non-Human creatures do not gain Katilda's mana ability")
    void nonHumansDoNotGainManaAbility() {
        harness.addToBattlefield(player1, new KatildaDawnhartPrime());
        Permanent wolf = addCreatureReady(player1, new PrimalAdversary());

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(wolf), 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Protection from Werewolves is applied")
    void hasProtectionFromWerewolves() {
        Permanent katilda = addCreatureReady(player1, new KatildaDawnhartPrime());
        addCreatureReady(player2, new VillageWatch());
        katilda.setAttacking(true);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("The activated ability puts a counter on each creature you control")
    void putsCountersOnOwnCreatures() {
        Permanent katilda = addCreatureReady(player1, new KatildaDawnhartPrime());
        Permanent ownCreature = addCreatureReady(player1, new PrimalAdversary());
        Permanent opponentCreature = addCreatureReady(player2, new PrimalAdversary());

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, battlefieldIndex(katilda), 0, null, null);
        harness.passBothPriorities();

        assertThat(katilda.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Opposing Humans do not gain the mana ability")
    void opposingHumansDoNotGainManaAbility() {
        addCreatureReady(player1, new KatildaDawnhartPrime());
        addCreatureReady(player2, new CatharCommando());

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Summoning-sick Humans cannot activate the granted tap ability")
    void summoningSicknessPreventsManaAbility() {
        addCreatureReady(player1, new KatildaDawnhartPrime());
        harness.addToBattlefield(player1, new CatharCommando());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Humans lose the granted mana ability when Katilda leaves")
    void manaAbilityDisappearsWhenKatildaLeaves() {
        Permanent katilda = addCreatureReady(player1, new KatildaDawnhartPrime());
        Permanent human = addCreatureReady(player1, new CatharCommando());
        gd.playerBattlefields.get(player1.getId()).remove(katilda);
        gd.playerGraveyards.get(player1.getId()).add(katilda.getCard());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(human), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Counter ability resolves after Katilda leaves and includes creatures entering before resolution")
    void countersUseBattlefieldAtResolution() {
        Permanent katilda = addCreatureReady(player1, new KatildaDawnhartPrime());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, battlefieldIndex(katilda), 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(katilda);
        gd.playerGraveyards.get(player1.getId()).add(katilda.getCard());
        Permanent lateCreature = addCreatureReady(player1, new PrimalAdversary());

        harness.passBothPriorities();

        assertThat(lateCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Protection prevents combat damage from a Werewolf Katilda blocks")
    void preventsWerewolfCombatDamage() {
        addCreatureReady(player2, new VillageWatch());
        Permanent katilda = addCreatureReady(player1, new KatildaDawnhartPrime());
        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(katilda);
        assertThat(katilda.getMarkedDamage()).isZero();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }

}
