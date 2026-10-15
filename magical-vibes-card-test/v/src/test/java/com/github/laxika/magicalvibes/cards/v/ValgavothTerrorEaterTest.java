package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GremlinTamer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.ThaliaGuardianOfThraben;
import com.github.laxika.magicalvibes.cards.u.UnableToScream;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ValgavothTerrorEater.class, GrizzlyBears.class, Forest.class, Shock.class,
        ThaliaGuardianOfThraben.class, UnableToScream.class, GremlinTamer.class})
class ValgavothTerrorEaterTest extends BaseCardTest {

    @Test
    @DisplayName("Casts a card exiled with Valgavoth by paying its mana value in life")
    void castsExiledSpellByPayingManaValueLife() {
        Permanent valgavoth = harness.addToBattlefieldAndReturn(player1, new ValgavothTerrorEater());
        Card bears = new GrizzlyBears();
        gd.addToExile(player2.getId(), bears, valgavoth.getId());
        prepareMainPhase(player1);

        harness.castFromExile(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("May play a land exiled with Valgavoth")
    void playsExiledLand() {
        Permanent valgavoth = harness.addToBattlefieldAndReturn(player1, new ValgavothTerrorEater());
        Card forest = new Forest();
        gd.addToExile(player2.getId(), forest, valgavoth.getId());
        prepareMainPhase(player1);

        harness.castFromExile(player1, forest.getId());

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Exiles an opponent's card put into their graveyard with Valgavoth")
    void exilesOpponentCardInsteadOfGraveyard() {
        Permanent valgavoth = harness.addToBattlefieldAndReturn(player1, new ValgavothTerrorEater());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(gd.getCardsExiledByPermanent(valgavoth.getId()))
                .anyMatch(card -> card.getId().equals(bears.getCard().getId()));
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Ward can be paid by sacrificing three nonland permanents")
    void wardCanBePaidBySacrificingThreeNonlands() {
        Permanent valgavoth = harness.addToBattlefieldAndReturn(player1, new ValgavothTerrorEater());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, valgavoth.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleMultiplePermanentsChosen(player2, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Valgavoth, Terror Eater");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.getCardsExiledByPermanent(valgavoth.getId()))
                .anyMatch(card -> card.getName().equals("Shock"));
    }

    @Test
    @DisplayName("An opponent-owned instant cast with Valgavoth goes to its owner's graveyard")
    void controlledExiledInstantIsNotExiledAgain() {
        Permanent valgavoth = harness.addToBattlefieldAndReturn(player1, new ValgavothTerrorEater());
        Card shock = new Shock();
        gd.addToExile(player2.getId(), shock, valgavoth.getId());
        prepareMainPhase(player1);

        harness.castFromExile(player1, shock.getId(), player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Shock");
        assertThat(gd.findExiledCard(shock.getId())).isNull();
    }

    @Test
    @DisplayName("An opponent-owned creature controlled by Valgavoth's controller dies normally")
    void controlledExiledCreatureIsNotExiledAgainWhenItDies() {
        Permanent valgavoth = harness.addToBattlefieldAndReturn(player1, new ValgavothTerrorEater());
        Card bears = new GrizzlyBears();
        gd.addToExile(player2.getId(), bears, valgavoth.getId());
        prepareMainPhase(player1);
        harness.castFromExile(player1, bears.getId());
        harness.passBothPriorities();
        Permanent creature = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.findExiledCard(bears.getId())).isNull();
    }

    @Test
    @DisplayName("Valgavoth does not permit casting even instants on an opponent's turn")
    void cannotCastExiledInstantDuringOpponentsTurn() {
        Permanent valgavoth = harness.addToBattlefieldAndReturn(player1, new ValgavothTerrorEater());
        Card shock = new Shock();
        gd.addToExile(player2.getId(), shock, valgavoth.getId());
        prepareMainPhase(player2);

        assertThatThrownBy(() -> harness.castFromExile(player1, shock.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(shock.getId())).isNotNull();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Exiled creatures still require sorcery timing")
    void cannotCastExiledCreatureDuringUpkeep() {
        Permanent valgavoth = harness.addToBattlefieldAndReturn(player1, new ValgavothTerrorEater());
        Card bears = new GrizzlyBears();
        gd.addToExile(player2.getId(), bears, valgavoth.getId());
        prepareMainPhase(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.castFromExile(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Valgavoth cannot play unrelated exiled cards")
    void cannotCastCardNotExiledWithValgavoth() {
        harness.addToBattlefield(player1, new ValgavothTerrorEater());
        Card bears = new GrizzlyBears();
        harness.setExile(player2, List.of(bears));
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.castFromExile(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
    }

    @Test
    @DisplayName("A new Valgavoth cannot play cards exiled with the previous one")
    void newValgavothDoesNotGrantOldExilePermission() {
        Permanent oldValgavoth = harness.addToBattlefieldAndReturn(player1, new ValgavothTerrorEater());
        Card bears = new GrizzlyBears();
        gd.addToExile(player2.getId(), bears, oldValgavoth.getId());
        gd.playerBattlefields.get(player1.getId()).remove(oldValgavoth);
        harness.addToBattlefield(player1, new ValgavothTerrorEater());
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.castFromExile(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
    }

    @Test
    @DisplayName("Mana cannot replace an unaffordable life payment")
    void cannotPayManaInsteadOfLife() {
        Permanent valgavoth = harness.addToBattlefieldAndReturn(player1, new ValgavothTerrorEater());
        Card bears = new GrizzlyBears();
        gd.addToExile(player2.getId(), bears, valgavoth.getId());
        prepareMainPhase(player1);
        harness.setLife(player1, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castFromExile(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
        harness.assertLife(player1, 1);
    }

    @Test
    @DisplayName("Playing a land with Valgavoth uses the normal land play")
    void cannotPlaySecondExiledLand() {
        Permanent valgavoth = harness.addToBattlefieldAndReturn(player1, new ValgavothTerrorEater());
        Card first = new Forest();
        Card second = new Forest();
        gd.addToExile(player2.getId(), first, valgavoth.getId());
        gd.addToExile(player2.getId(), second, valgavoth.getId());
        prepareMainPhase(player1);
        harness.castFromExile(player1, first.getId());

        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(countPermanents(player1, "Forest")).isEqualTo(1);
        assertThat(gd.findExiledCard(second.getId())).isNotNull();
    }

    @Test
    @DisplayName("Ward counters the spell when there are only two nonland permanents")
    void landsCannotCompleteWardPayment() {
        Permanent valgavoth = harness.addToBattlefieldAndReturn(player1, new ValgavothTerrorEater());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Forest());
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, valgavoth.getId());
        resolveAllTriggers();

        assertThat(valgavoth.getMarkedDamage()).isZero();
        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Forest");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getCardsExiledByPermanent(valgavoth.getId()))
                .anyMatch(card -> card.getName().equals("Shock"));
    }

    @Test
    @DisplayName("The opponent may decline the three-permanent ward payment")
    void wardPaymentCanBeDeclined() {
        Permanent valgavoth = harness.addToBattlefieldAndReturn(player1, new ValgavothTerrorEater());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, valgavoth.getId());

        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(valgavoth.getMarkedDamage()).isZero();
        assertThat(countPermanents(player2, "Grizzly Bears")).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ward does not trigger for the controller's own spell")
    void controllerCanTargetValgavothWithoutWardPayment() {
        Permanent valgavoth = harness.addToBattlefieldAndReturn(player1, new ValgavothTerrorEater());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, valgavoth.getId());

        assertThat(valgavoth.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Shock");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({ValgavothTerrorEater.class, Shock.class, ThaliaGuardianOfThraben.class})
    @DisplayName("Casting with Valgavoth still requires mana for Thalia's cost increase")
    void lifeAlternativeDoesNotBypassCostIncreases() {
        Permanent valgavoth = harness.addToBattlefieldAndReturn(player1, new ValgavothTerrorEater());
        harness.addToBattlefield(player2, new ThaliaGuardianOfThraben());
        Card shock = new Shock();
        gd.addToExile(player2.getId(), shock, valgavoth.getId());
        prepareMainPhase(player1);

        assertThatThrownBy(() -> harness.castFromExile(player1, shock.getId(), player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 20);
        assertThat(gd.findExiledCard(shock.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({ValgavothTerrorEater.class, Shock.class, ThaliaGuardianOfThraben.class})
    @DisplayName("A spell cast with Valgavoth pays both life and Thalia's additional mana")
    void lifeAlternativePaysCostIncreaseInMana() {
        Permanent valgavoth = harness.addToBattlefieldAndReturn(player1, new ValgavothTerrorEater());
        harness.addToBattlefield(player2, new ThaliaGuardianOfThraben());
        Card shock = new Shock();
        gd.addToExile(player2.getId(), shock, valgavoth.getId());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castFromExile(player1, shock.getId(), player2.getId());

        harness.assertLife(player1, 19);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    @CardUsed({ValgavothTerrorEater.class, GrizzlyBears.class, UnableToScream.class})
    @DisplayName("Losing all abilities removes Valgavoth's permission to play exiled cards")
    void cannotCastExiledCardAfterValgavothLosesAbilities() {
        Permanent valgavoth = harness.addToBattlefieldAndReturn(player1, new ValgavothTerrorEater());
        Card bears = new GrizzlyBears();
        gd.addToExile(player2.getId(), bears, valgavoth.getId());
        prepareMainPhase(player1);
        harness.setHand(player1, List.of(new UnableToScream()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castEnchantment(player1, 0, valgavoth.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
        harness.assertLife(player1, 20);
    }

    @Test
    @CardUsed({ValgavothTerrorEater.class, GremlinTamer.class, GrizzlyBears.class,
            UnableToScream.class, Shock.class})
    @DisplayName("Valgavoth does not prevent an opponent's creature token from dying")
    void opponentTokenDiesNormally() {
        harness.addToBattlefield(player1, new ValgavothTerrorEater());
        harness.addToBattlefield(player2, new GremlinTamer());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        prepareMainPhase(player2);
        harness.setHand(player2, List.of(new UnableToScream()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castEnchantment(player2, 0, bears.getId());
        resolveAllTriggers();
        Permanent gremlin = findPermanent(player2, "Gremlin");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, gremlin.getId());

        harness.assertNotOnBattlefield(player2, "Gremlin");
        assertThat(gd.creatureDeathCountThisTurn.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }

    private void prepareMainPhase(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
