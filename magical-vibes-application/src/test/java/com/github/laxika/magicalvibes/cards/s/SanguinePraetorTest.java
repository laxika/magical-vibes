package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DryadSophisticate;
import com.github.laxika.magicalvibes.cards.g.GruulGuildmage;
import com.github.laxika.magicalvibes.cards.g.GruulSignet;
import com.github.laxika.magicalvibes.cards.w.WildCantor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({SanguinePraetor.class, DryadSophisticate.class, GruulGuildmage.class,
        WildCantor.class, GruulSignet.class})
class SanguinePraetorTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a creature destroys all creatures with the same mana value")
    void destroysCreaturesWithSacrificedCreatureManaValue() {
        Permanent praetor = addCreatureReady(player1, new SanguinePraetor());
        Permanent sacrificed = addCreatureReady(player1, new DryadSophisticate());
        harness.addToBattlefield(player1, new WildCantor());
        harness.addToBattlefield(player2, new GruulGuildmage());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(praetor), null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dryad Sophisticate");
        harness.assertNotOnBattlefield(player1, "Dryad Sophisticate");
        harness.assertOnBattlefield(player1, "Wild Cantor");
        harness.assertNotOnBattlefield(player2, "Gruul Guildmage");
    }

    @Test
    @DisplayName("The source may be sacrificed and its mana value is still used")
    void maySacrificeSource() {
        Permanent source = addCreatureReady(player1, new SanguinePraetor());
        addCreatureReady(player2, new SanguinePraetor());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(source), null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Sanguine Praetor");
        harness.assertNotOnBattlefield(player1, "Sanguine Praetor");
        harness.assertNotOnBattlefield(player2, "Sanguine Praetor");
        harness.assertInGraveyard(player2, "Sanguine Praetor");
    }

    @Test
    @DisplayName("The ability destroys creatures, not noncreature permanents, with that mana value")
    void doesNotDestroyNoncreatureWithMatchingManaValue() {
        Permanent praetor = addCreatureReady(player1, new SanguinePraetor());
        Permanent sacrificed = addCreatureReady(player1, new DryadSophisticate());
        harness.addToBattlefield(player2, new GruulSignet());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(praetor), null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Dryad Sophisticate");
        harness.assertOnBattlefield(player2, "Gruul Signet");
    }
}
