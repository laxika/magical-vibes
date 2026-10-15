package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.l.LazotepPlating;
import com.github.laxika.magicalvibes.cards.n.NarsetParterOfVeils;
import com.github.laxika.magicalvibes.cards.n.NoEscape;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InterplanarBeacon.class, NarsetParterOfVeils.class, LazotepPlating.class, NoEscape.class})
class InterplanarBeaconTest extends BaseCardTest {

    @Test
    void tapsForColorlessMana() {
        harness.addToBattlefieldAndReturn(player1, new InterplanarBeacon());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void differentColorManaCastsPlaneswalkerAndGainsLife() {
        harness.addToBattlefieldAndReturn(player1, new InterplanarBeacon());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        harness.setHand(player1, List.of(new NarsetParterOfVeils()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);

        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Narset, Parter of Veils");
    }

    @Test
    void planeswalkerManaCannotCastNonPlaneswalkerSpell() {
        harness.addToBattlefieldAndReturn(player1, new InterplanarBeacon());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());
        harness.handleListChoice(player1, ManaColor.RED.name());

        harness.setHand(player1, List.of(new LazotepPlating()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void manaAbilityRequiresTwoDifferentColorsAndRejectsColorless() {
        var beacon = harness.addToBattlefieldAndReturn(player1, new InterplanarBeacon());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(beacon.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThatThrownBy(() -> harness.handleListChoice(player1, ManaColor.COLORLESS.name()))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, ManaColor.RED.name());
        assertThatThrownBy(() -> harness.handleListChoice(player1, ManaColor.RED.name()))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getAllManaTotals())
                .containsEntry(ManaColor.RED, 1)
                .containsEntry(ManaColor.BLUE, 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void secondColorManaAlsoCannotCastNonPlaneswalkerSpell() {
        harness.addToBattlefield(player1, new InterplanarBeacon());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.handleListChoice(player1, ManaColor.BLUE.name());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setHand(player1, List.of(new LazotepPlating()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotPayActivationCostUsingTheBeaconBeingActivated() {
        var beacon = harness.addToBattlefieldAndReturn(player1, new InterplanarBeacon());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(beacon.isTapped()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void castingPlaneswalkerTriggersWithoutSpendingBeaconMana() {
        harness.addToBattlefield(player1, new InterplanarBeacon());
        harness.setHand(player1, List.of(new NarsetParterOfVeils()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setLife(player1, 10);

        harness.castPlaneswalker(player1, 0);

        harness.assertLife(player1, 10);
        harness.assertNotOnBattlefield(player1, "Narset, Parter of Veils");
        harness.passBothPriorities();
        harness.assertLife(player1, 11);
        harness.assertNotOnBattlefield(player1, "Narset, Parter of Veils");
    }

    @Test
    void opponentsBeaconDoesNotTriggerForYourPlaneswalker() {
        harness.addToBattlefield(player2, new InterplanarBeacon());
        harness.setHand(player1, List.of(new NarsetParterOfVeils()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setLife(player1, 10);
        harness.setLife(player2, 10);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Narset, Parter of Veils");
        harness.assertLife(player1, 10);
        harness.assertLife(player2, 10);
    }

    @Test
    void nonPlaneswalkerSpellDoesNotGainLife() {
        harness.addToBattlefield(player1, new InterplanarBeacon());
        harness.setHand(player1, List.of(new LazotepPlating()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setLife(player1, 10);

        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player1, 10);
        harness.assertInGraveyard(player1, "Lazotep Plating");
    }

    @Test
    void eachBeaconTriggersEvenWhenTapped() {
        var first = harness.addToBattlefieldAndReturn(player1, new InterplanarBeacon());
        var second = harness.addToBattlefieldAndReturn(player1, new InterplanarBeacon());
        first.tap();
        second.tap();
        harness.setHand(player1, List.of(new NarsetParterOfVeils()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setLife(player1, 10);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();
        harness.assertLife(player1, 11);
        harness.passBothPriorities();
        harness.assertLife(player1, 12);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Narset, Parter of Veils");
    }

    @Test
    void lifeGainIsRetainedWhenPlaneswalkerIsCountered() {
        harness.addToBattlefield(player1, new InterplanarBeacon());
        var narset = new NarsetParterOfVeils();
        harness.setHand(player1, List.of(narset));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setHand(player2, List.of(new NoEscape()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.setLife(player1, 10);

        harness.castPlaneswalker(player1, 0);
        harness.passBothPriorities();
        harness.assertLife(player1, 11);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, narset.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 11);
        harness.assertNotOnBattlefield(player1, "Narset, Parter of Veils");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(narset);
    }
}
