package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BladeSplicer;
import com.github.laxika.magicalvibes.cards.h.HangedExecutioner;
import com.github.laxika.magicalvibes.cards.r.RapaciousDragon;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdrixAndNevTwincasters.class, HangedExecutioner.class, Shock.class, BladeSplicer.class,
        RapaciousDragon.class, TurnToFrog.class})
class AdrixAndNevTwincastersTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles tokens created under its controller's control")
    void doublesTokens() {
        harness.addToBattlefield(player1, new AdrixAndNevTwincasters());
        harness.setHand(player1, List.of(new BladeSplicer()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Phyrexian Golem")).hasSize(2);
    }

    @Test
    @DisplayName("Does not double tokens created under an opponent's control")
    void doesNotDoubleOpponentTokens() {
        harness.addToBattlefield(player1, new AdrixAndNevTwincasters());
        harness.setHand(player2, List.of(new BladeSplicer()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player2, "Phyrexian Golem")).hasSize(1);
    }

    @Test
    @DisplayName("Ward {2} counters an opponent's spell when they do not pay")
    void wardCountersUnpaidSpell() {
        Permanent adrixAndNev = harness.addToBattlefieldAndReturn(player1, new AdrixAndNevTwincasters());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, adrixAndNev.getId());

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Adrix and Nev, Twincasters");
    }

    @Test
    @DisplayName("Ward {2} lets an opponent's spell resolve when they pay")
    void wardCanBePaid() {
        Permanent adrixAndNev = harness.addToBattlefieldAndReturn(player1, new AdrixAndNevTwincasters());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);

        harness.castAndResolveInstant(player2, 0, adrixAndNev.getId());

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(adrixAndNev.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("Doubles noncreature tokens and a batch of multiple tokens")
    void doublesTreasures() {
        harness.addToBattlefield(player1, new AdrixAndNevTwincasters());
        harness.setHand(player1, List.of(new RapaciousDragon()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(4);
    }

    @Test
    @DisplayName("Does not double tokens after losing all abilities")
    void abilityLossDisablesTokenDoubling() {
        Permanent adrixAndNev = harness.addToBattlefieldAndReturn(player1, new AdrixAndNevTwincasters());
        harness.setHand(player1, List.of(new TurnToFrog(), new HangedExecutioner()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castAndResolveInstant(player1, 0, adrixAndNev.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    @DisplayName("Ward does not counter its controller's spell")
    void ownSpellDoesNotTriggerWard() {
        Permanent adrixAndNev = harness.addToBattlefieldAndReturn(player1, new AdrixAndNevTwincasters());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, adrixAndNev.getId());

        harness.assertInGraveyard(player1, "Shock");
        harness.assertInGraveyard(player1, "Adrix and Nev, Twincasters");
    }

    @Test
    @DisplayName("Ward counters an unpaid activated ability even when its source was exiled as a cost")
    void wardCountersUnpaidActivatedAbility() {
        Permanent adrixAndNev = harness.addToBattlefieldAndReturn(player1, new AdrixAndNevTwincasters());
        harness.addToBattlefield(player2, new HangedExecutioner());
        harness.addMana(player2, ManaColor.WHITE, 4);

        harness.activateAbility(player2, 0, null, adrixAndNev.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Adrix and Nev, Twincasters");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName()).contains("Hanged Executioner");
    }

    @Test
    @DisplayName("Ward allows an activated ability to resolve when its controller pays")
    void wardPaymentAllowsActivatedAbility() {
        Permanent adrixAndNev = harness.addToBattlefieldAndReturn(player1, new AdrixAndNevTwincasters());
        harness.addToBattlefield(player2, new HangedExecutioner());
        harness.addMana(player2, ManaColor.WHITE, 6);

        harness.activateAbility(player2, 0, null, adrixAndNev.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(card -> card.getName()).contains("Adrix and Nev, Twincasters");
    }

    @Test
    @DisplayName("Losing all abilities also removes ward")
    void abilityLossDisablesWard() {
        Permanent adrixAndNev = harness.addToBattlefieldAndReturn(player1, new AdrixAndNevTwincasters());
        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, adrixAndNev.getId());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, adrixAndNev.getId());

        harness.assertInGraveyard(player1, "Adrix and Nev, Twincasters");
        harness.assertInGraveyard(player2, "Shock");
    }
}
