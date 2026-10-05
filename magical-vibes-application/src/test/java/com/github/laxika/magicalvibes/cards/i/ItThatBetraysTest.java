package com.github.laxika.magicalvibes.cards.i;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.k.KuldothaRebirth;
import com.github.laxika.magicalvibes.cards.k.KozileksPredator;
import com.github.laxika.magicalvibes.cards.e.EmrakulsHatcher;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({ItThatBetrays.class, KuldothaRebirth.class, Spellbook.class,
        KozileksPredator.class, EmrakulsHatcher.class, LeylineOfTheVoid.class})
class ItThatBetraysTest extends BaseCardTest {

    @Test
    @DisplayName("Annihilator sacrifices two permanents and returns both under the attacker's control")
    void annihilatorReturnsBothSacrificedPermanents() {
        Permanent betrayer = addCreatureReady(player1, new ItThatBetrays());
        harness.addToBattlefield(player2, new KozileksPredator());
        harness.addToBattlefield(player2, new EmrakulsHatcher());

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(betrayer)));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Kozilek's Predator");
        harness.assertNotOnBattlefield(player2, "Emrakul's Hatcher");
        harness.assertOnBattlefield(player1, "Kozilek's Predator");
        harness.assertOnBattlefield(player1, "Emrakul's Hatcher");
        harness.assertNotInGraveyard(player2, "Kozilek's Predator");
        harness.assertNotInGraveyard(player2, "Emrakul's Hatcher");
    }

    @Test
    @DisplayName("Returns a sacrificed permanent directly exiled by Leyline of the Void")
    void returnsSacrificedPermanentFromReplacementExile() {
        harness.addToBattlefield(player2, new ItThatBetrays());
        harness.addToBattlefield(player2, new LeylineOfTheVoid());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setHand(player1, List.of(new KuldothaRebirth()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, spellbook.getId());
        assertThat(gd.findExiledCard(spellbook.getCard().getId())).isNotNull();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Spellbook");
        assertThat(gd.findExiledCard(spellbook.getCard().getId())).isNull();
        harness.assertNotInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("Two return triggers cannot return the same sacrificed card twice")
    void competingTriggersReturnCardOnlyOnce() {
        harness.addToBattlefield(player2, new ItThatBetrays());
        harness.addToBattlefield(player2, new ItThatBetrays());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setHand(player1, List.of(new KuldothaRebirth()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, spellbook.getId());
        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Spellbook"))
                .hasSize(1);
        harness.assertNotInGraveyard(player1, "Spellbook");
    }

    @Test
    @DisplayName("Returns an opponent's sacrificed nontoken permanent under its control")
    void returnsOpponentSacrificedPermanent() {
        harness.addToBattlefield(player2, new ItThatBetrays());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setHand(player1, List.of(new KuldothaRebirth()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, spellbook.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Spellbook");
        harness.assertNotInGraveyard(player1, "Spellbook");

        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Does not trigger when its controller sacrifices a permanent")
    void doesNotTriggerForControllerSacrifice() {
        harness.addToBattlefield(player1, new ItThatBetrays());
        Permanent spellbook = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        harness.setHand(player1, List.of(new KuldothaRebirth()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, spellbook.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertNotOnBattlefield(player1, "Spellbook");
    }

    @Test
    @DisplayName("Does not trigger for a sacrificed token")
    void doesNotTriggerForToken() {
        harness.addToBattlefield(player2, new ItThatBetrays());
        Spellbook tokenCard = new Spellbook();
        tokenCard.setToken(true);
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCard);
        harness.setHand(player1, List.of(new KuldothaRebirth()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorceryWithSacrifice(player1, 0, token.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Spellbook");
    }
}
