package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AdarkarWastes;
import com.github.laxika.magicalvibes.cards.c.CabalCoffers;
import com.github.laxika.magicalvibes.cards.c.CityOfBrass;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DrainPower.class, Forest.class, GrizzlyBears.class, Island.class,
        CityOfBrass.class, CabalCoffers.class, Swamp.class, AdarkarWastes.class})
class DrainPowerTest extends BaseCardTest {

    @Test
    @DisplayName("Taps target player's lands and controller adds the mana they produce")
    void drainsLandMana() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Island());
        List<Permanent> battlefield = gd.playerBattlefields.get(player2.getId());

        cast();

        assertThat(battlefield).allMatch(Permanent::isTapped);
        ManaPool controllerPool = gd.playerManaPools.get(player1.getId());
        assertThat(controllerPool.get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(controllerPool.get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Mana the target player already had is also drained to the controller")
    void drainsPreexistingMana() {
        harness.addMana(player2, ManaColor.RED, 3);

        cast();

        ManaPool controllerPool = gd.playerManaPools.get(player1.getId());
        assertThat(controllerPool.get(ManaColor.RED)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Returns land mana to the controller when targeting themself")
    void returnsLandManaWhenTargetingSelf() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        cast(player1.getId());

        assertThat(forest.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Already-tapped lands and non-lands produce nothing")
    void ignoresTappedAndNonLands() {
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        forest.tap();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        cast();

        assertThat(bears.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Preserves spending restrictions on transferred mana")
    void preservesManaRestrictions() {
        ManaPool targetPool = gd.playerManaPools.get(player2.getId());
        targetPool.addAbilityOnlyMana(ManaColor.COLORLESS, 2);

        cast();

        ManaPool controllerPool = gd.playerManaPools.get(player1.getId());
        assertThat(controllerPool.getAbilityOnlyMana(ManaColor.COLORLESS)).isEqualTo(2);
        assertThat(controllerPool.get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @CardUsed({CityOfBrass.class})
    @DisplayName("Lets the target choose the color from an any-color land")
    void promptsTargetForAnyColorLand() {
        harness.addToBattlefield(player2, new CityOfBrass());

        cast();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @CardUsed({CabalCoffers.class, Swamp.class})
    @DisplayName("Activates a land mana ability with a mana activation cost")
    void activatesPaidLandManaAbility() {
        harness.addToBattlefield(player2, new CabalCoffers());
        harness.addToBattlefield(player2, new Swamp());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        cast();

        ManaPool controllerPool = gd.playerManaPools.get(player1.getId());
        assertThat(controllerPool.get(ManaColor.BLACK)).isEqualTo(2);
        assertThat(controllerPool.get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("The target chooses which mana ability to activate on a land with multiple abilities")
    void letsTargetChooseLandManaAbility() {
        harness.addToBattlefield(player2, new AdarkarWastes());

        cast();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        harness.handleListChoice(player2, choice.options().get(1));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerBattlefields.get(player2.getId())).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Spell-only mana retains its restriction when added to a nonempty pool")
    void preservesSpellRestrictionWhenControllerAlreadyHasMana() {
        harness.addMana(player2, ManaColor.RED, 2);
        gd.playerManaPools.get(player2.getId()).addSpellOnlyMana(ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        cast();

        ManaPool controllerPool = gd.playerManaPools.get(player1.getId());
        assertThat(controllerPool.getSpellOnlyMana(ManaColor.RED)).isEqualTo(2);
        assertThat(controllerPool.get(ManaColor.RED)).isEqualTo(2);
        assertThat(controllerPool.get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Transferred mana retains its basic-land source")
    void preservesBasicLandSource() {
        harness.addToBattlefield(player2, new Forest());

        cast();

        ManaPool controllerPool = gd.playerManaPools.get(player1.getId());
        assertThat(controllerPool.get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(controllerPool.getBasicLandMana(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("The target's chosen City of Brass color is transferred to the caster")
    void transfersChosenLandManaColor() {
        harness.addToBattlefield(player2, new CityOfBrass());

        cast();
        harness.handleListChoice(player2, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
    }

    private void cast() {
        cast(player2.getId());
    }

    private void cast(UUID targetPlayerId) {
        harness.setHand(player1, List.of(new DrainPower()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveSorcery(player1, 0, targetPlayerId);
    }
}
