package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DryadSophisticate;
import com.github.laxika.magicalvibes.cards.g.GruulGuildmage;
import com.github.laxika.magicalvibes.cards.g.GruulSignet;
import com.github.laxika.magicalvibes.cards.w.WildCantor;
import com.github.laxika.magicalvibes.cards.w.Willbender;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SanguinePraetor.class, DryadSophisticate.class, GruulGuildmage.class,
        WildCantor.class, GruulSignet.class, Willbender.class})
class SanguinePraetorTest extends BaseCardTest {

    @Test
    @DisplayName("Face-down creatures have mana value zero, not their printed mana value")
    void sparesFaceDownCreatureWhenSacrificingTwoManaCreature() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Willbender()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player2, 0);
        harness.passBothPriorities();
        Permanent faceDown = findPermanent(player2, "Willbender");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        Permanent praetor = harness.addToBattlefieldAndReturn(player1, new SanguinePraetor());
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new DryadSophisticate());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(praetor), null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(faceDown);
        harness.assertInGraveyard(player1, "Dryad Sophisticate");
    }

    @Test
    @DisplayName("A tapped summoning-sick Praetor can activate and destroys matching creatures on both sides")
    void activationDoesNotRequireTapOrHasteAndSacrificeIsPaidBeforeResolution() {
        Permanent praetor = harness.addToBattlefieldAndReturn(player1, new SanguinePraetor());
        praetor.setTapped(true);
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new DryadSophisticate());
        harness.addToBattlefield(player1, new GruulGuildmage());
        harness.addToBattlefield(player2, new GruulGuildmage());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(praetor), null, null);
        harness.handlePermanentChosen(player1, sacrificed.getId());

        harness.assertInGraveyard(player1, "Dryad Sophisticate");
        harness.assertOnBattlefield(player1, "Gruul Guildmage");
        harness.assertOnBattlefield(player2, "Gruul Guildmage");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Gruul Guildmage");
        harness.assertInGraveyard(player2, "Gruul Guildmage");
        harness.assertOnBattlefield(player1, "Sanguine Praetor");
    }

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
