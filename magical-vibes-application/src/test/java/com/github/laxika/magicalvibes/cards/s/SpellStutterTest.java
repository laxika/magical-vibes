package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RedtoothVanguard;
import com.github.laxika.magicalvibes.cards.t.TorchTheTower;
import com.github.laxika.magicalvibes.cards.w.WitchsMark;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpellStutter.class, RedtoothVanguard.class, SnaremasterSprite.class,
        TorchTheTower.class, WitchsMark.class})
class SpellStutterTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the spell when its controller cannot pay the base {2} cost")
    void countersWhenControllerCannotPayBaseCost() {
        RedtoothVanguard vanguard = castVanguardWithMana(3);
        castSpellStutter(vanguard);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Redtooth Vanguard");
        harness.assertNotOnBattlefield(player1, "Redtooth Vanguard");
    }

    @Test
    @DisplayName("Adds {1} to the cost for each Faerie controlled")
    void faerieIncreasesCost() {
        harness.addToBattlefield(player2, new SnaremasterSprite());

        RedtoothVanguard vanguard = castVanguardWithMana(4);
        castSpellStutter(vanguard);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Redtooth Vanguard");
        harness.assertNotOnBattlefield(player1, "Redtooth Vanguard");
    }

    @Test
    @DisplayName("Allows the spell to resolve when its controller pays {2} plus {1} per Faerie")
    void controllerPaysFaerieAdjustedCost() {
        harness.addToBattlefield(player2, new SnaremasterSprite());

        RedtoothVanguard vanguard = castVanguardWithMana(5);
        castSpellStutter(vanguard);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Redtooth Vanguard");
    }

    @Test
    @DisplayName("Allows payment of the base cost with no Faeries")
    void controllerPaysBaseCost() {
        RedtoothVanguard vanguard = castVanguardWithMana(4);
        castSpellStutter(vanguard);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Redtooth Vanguard");
        harness.assertInGraveyard(player2, "Spell Stutter");
    }

    @Test
    @DisplayName("Counters when the controller declines payment despite having enough mana")
    void controllerDeclinesPayment() {
        RedtoothVanguard vanguard = castVanguardWithMana(4);
        castSpellStutter(vanguard);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Redtooth Vanguard");
        harness.assertNotOnBattlefield(player1, "Redtooth Vanguard");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts every Faerie, including tapped Faeries")
    void multipleFaeriesIncreaseCost() {
        harness.addToBattlefield(player2, new SnaremasterSprite());
        harness.addToBattlefieldAndReturn(player2, new SnaremasterSprite()).tap();

        RedtoothVanguard vanguard = castVanguardWithMana(5);
        castSpellStutter(vanguard);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Redtooth Vanguard");
        harness.assertNotOnBattlefield(player1, "Redtooth Vanguard");
    }

    @Test
    @DisplayName("Does not count the opposing controller's Faeries or its own non-Faeries")
    void ignoresOpposingFaeriesAndNonFaeries() {
        harness.addToBattlefield(player1, new SnaremasterSprite());
        harness.addToBattlefield(player2, new RedtoothVanguard());

        RedtoothVanguard vanguard = castVanguardWithMana(4);
        castSpellStutter(vanguard);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Redtooth Vanguard");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Counts Faeries at resolution after a Faerie is removed in response")
    void faerieRemovedBeforeResolutionReducesCost() {
        var faerie = harness.addToBattlefieldAndReturn(player2, new SnaremasterSprite());
        RedtoothVanguard vanguard = castVanguardWithMana(4);
        castSpellStutter(vanguard);

        harness.setHand(player1, List.of(new TorchTheTower()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.ensurePriority(player1);
        harness.castInstant(player1, 0, faerie.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Snaremaster Sprite");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Redtooth Vanguard");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Does not count Faerie cards in hand or the graveyard")
    void ignoresFaeriesOutsideBattlefield() {
        RedtoothVanguard vanguard = castVanguardWithMana(4);
        harness.setGraveyard(player2, List.of(new SnaremasterSprite()));
        harness.setHand(player2, List.of(new SpellStutter(), new SnaremasterSprite()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, vanguard.getId());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Redtooth Vanguard");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Can counter a noncreature spell")
    void countersNoncreatureSpell() {
        WitchsMark mark = new WitchsMark();
        harness.setHand(player1, List.of(mark));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, List.of());
        harness.passPriority(player1);
        castSpellStutter(mark);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Witch's Mark");
        harness.assertInGraveyard(player2, "Spell Stutter");
    }

    private RedtoothVanguard castVanguardWithMana(int amount) {
        RedtoothVanguard vanguard = new RedtoothVanguard();
        harness.castFromHand(player1, vanguard, "{1}{G}");
        harness.addMana(player1, ManaColor.GREEN, amount - 2);
        harness.passPriority(player1);
        return vanguard;
    }

    private void castSpellStutter(Card spell) {
        harness.setHand(player2, List.of(new SpellStutter()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, spell.getId());
    }
}
