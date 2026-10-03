package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BearerOfSilence;
import com.github.laxika.magicalvibes.cards.b.BalothPup;
import com.github.laxika.magicalvibes.cards.e.EldraziMimic;
import com.github.laxika.magicalvibes.cards.f.FlayingTendrils;
import com.github.laxika.magicalvibes.cards.s.SeersLantern;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CorruptedCrossroads.class, BearerOfSilence.class, BalothPup.class,
        EldraziMimic.class, FlayingTendrils.class, SeersLantern.class})
class CorruptedCrossroadsTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability adds one colorless mana")
    void addsColorlessMana() {
        Permanent crossroads = addReadyCrossroads(player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(crossroads.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    @DisplayName("The second ability adds one chosen color restricted to spells with devoid")
    void addsDevoidRestrictedMana(ManaColor color) {
        Permanent crossroads = addReadyCrossroads(player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).getDevoidSpellOnlyMana(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(crossroads.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Devoid-restricted mana can pay the colored cost of a devoid creature spell")
    void restrictedManaOnlyCastsDevoidSpells() {
        addReadyCrossroads(player1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.BLACK.name());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.setHand(player1, List.of(new BearerOfSilence()));
        harness.castCreature(player1, 0, player2.getId());
        assertThat(gd.playerManaPools.get(player1.getId()).getDevoidSpellOnlyMana(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Devoid-restricted mana cannot cast an ordinary spell")
    void cannotCastOrdinarySpell() {
        addReadyCrossroads(player1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new BalothPup()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.playerManaPools.get(player1.getId()).getDevoidSpellOnlyMana(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Restricted mana can pay the generic cost of a devoid sorcery")
    void paysGenericCostOfDevoidSorcery() {
        addReadyCrossroads(player1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setHand(player1, List.of(new FlayingTendrils()));

        harness.castSorcery(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getDevoidSpellOnlyMana(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Being colorless is insufficient without devoid")
    void cannotCastColorlessSpellWithoutDevoid() {
        addReadyCrossroads(player1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new EldraziMimic()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
        assertThat(gd.playerManaPools.get(player1.getId()).getDevoidSpellOnlyMana(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Restricted mana cannot pay an activated ability's generic cost")
    void cannotPayActivatedAbilityCost() {
        addReadyCrossroads(player1);
        harness.addToBattlefield(player1, new SeersLantern());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getDevoidSpellOnlyMana(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).get(1).isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Crossroads cannot pay life to produce more mana")
    void tappedLandCannotActivateAgain() {
        addReadyCrossroads(player1);
        harness.activateAbility(player1, 0, 0, null, null);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerManaPools.get(player1.getId()).getDevoidSpellOnlyManaTotal()).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    private Permanent addReadyCrossroads(Player player) {
        Permanent perm = addCreatureReady(player, new CorruptedCrossroads());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return perm;
    }
}
