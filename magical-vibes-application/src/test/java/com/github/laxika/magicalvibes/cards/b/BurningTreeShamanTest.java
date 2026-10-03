package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.Gelectrode;
import com.github.laxika.magicalvibes.cards.g.GruulSignet;
import com.github.laxika.magicalvibes.cards.o.Ovinize;
import com.github.laxika.magicalvibes.cards.s.SkarrganFirebird;
import com.github.laxika.magicalvibes.cards.s.SkarrganSkybreaker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BurningTreeShaman.class, Gelectrode.class, GruulSignet.class, SkarrganFirebird.class, SkarrganSkybreaker.class, Ovinize.class})
class BurningTreeShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to its controller when they activate a non-mana ability")
    void controllerActivatingNonManaAbilityTakesDamage() {
        addCreatureReady(player1, new BurningTreeShaman());
        addCreatureReady(player1, new Gelectrode());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Deals 1 damage to an opponent who activates a non-mana ability")
    void opponentActivatingNonManaAbilityTakesDamage() {
        addCreatureReady(player1, new BurningTreeShaman());
        addCreatureReady(player2, new Gelectrode());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Does not trigger for a mana ability")
    void manaAbilityDoesNotTrigger() {
        addCreatureReady(player1, new BurningTreeShaman());
        harness.addToBattlefield(player2, new GruulSignet());
        harness.setLife(player2, 20);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.activateAbility(player2, 0, null, null);

        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Burning-Tree Shaman triggers for the same non-mana activation")
    void eachCopyTriggersForOneActivation() {
        addCreatureReady(player1, new BurningTreeShaman());
        addCreatureReady(player1, new BurningTreeShaman());
        addCreatureReady(player2, new Gelectrode());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Deals damage when its controller activates a graveyard ability")
    void controllerGraveyardActivationTakesDamage() {
        harness.addToBattlefield(player1, new BurningTreeShaman());
        harness.setGraveyard(player1, List.of(new SkarrganFirebird()));
        harness.addMana(player1, ManaColor.RED, 3);
        gd.recordDamageToPlayer(player2.getId(), 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateGraveyardAbility(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        harness.assertInHand(player1, "Skarrgan Firebird");
    }

    @Test
    @DisplayName("Deals damage when an opponent activates a graveyard ability")
    void opponentGraveyardActivationTakesDamage() {
        harness.addToBattlefield(player1, new BurningTreeShaman());
        harness.setGraveyard(player2, List.of(new SkarrganFirebird()));
        harness.addMana(player2, ManaColor.RED, 3);
        gd.recordDamageToPlayer(player1.getId(), 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateGraveyardAbility(player2, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
        harness.assertInHand(player2, "Skarrgan Firebird");
    }

    @Test
    @DisplayName("Triggers even when the activated source is sacrificed as a cost")
    void sacrificedAbilitySourceStillTriggersShaman() {
        harness.addToBattlefield(player1, new BurningTreeShaman());
        harness.addToBattlefield(player2, new SkarrganSkybreaker());
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.assertInGraveyard(player2, "Skarrgan Skybreaker");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);

        resolveAllTriggers();

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Does not trigger for its controller's mana ability")
    void controllerManaAbilityDoesNotTrigger() {
        harness.addToBattlefield(player1, new BurningTreeShaman());
        harness.addToBattlefield(player1, new GruulSignet());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, null);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for either player after losing all abilities")
    void lostAbilitiesSuppressActivationTriggers() {
        Permanent shaman = harness.addToBattlefieldAndReturn(player1, new BurningTreeShaman());
        harness.setHand(player1, List.of(new Ovinize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, shaman.getId());
        addCreatureReady(player1, new Gelectrode());
        addCreatureReady(player2, new Gelectrode());
        harness.activateAbility(player1, 1, null, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);

        harness.activateAbility(player2, 0, null, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }
}
