package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.l.LeeringEmblem;
import com.github.laxika.magicalvibes.cards.l.LeylineOfSanctity;
import com.github.laxika.magicalvibes.cards.n.NettleSentinel;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SootImp.class, NettleSentinel.class, LeeringEmblem.class,
        SmolderingButcher.class, StillmoonCavalier.class, LeylineOfSanctity.class})
class SootImpTest extends BaseCardTest {

    @Test
    @DisplayName("Controller casting a nonblack spell makes that controller lose 1 life")
    void controllerCastsNonblackSpell() {
        harness.addToBattlefield(player1, new SootImp());
        harness.setHand(player1, List.of(new NettleSentinel()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);

        // Mandatory trigger sits on the stack above the creature spell.
        GameData gd = harness.getGameData();
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Soot Imp"));

        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore - 1);
    }

    @Test
    @DisplayName("The casting opponent — not Soot Imp's controller — loses the life")
    void opponentCastsNonblackSpell() {
        harness.addToBattlefield(player1, new SootImp());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new NettleSentinel()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        int controllerLifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        int casterLifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        harness.assertLife(player2, casterLifeBefore - 1);
        harness.assertLife(player1, controllerLifeBefore);
    }

    @Test
    @DisplayName("A colorless spell is nonblack and still triggers the life loss")
    void colorlessSpellTriggers() {
        harness.addToBattlefield(player1, new SootImp());
        harness.setHand(player1, List.of(new LeeringEmblem()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore - 1);
    }

    @Test
    @DisplayName("Casting a black spell does not trigger Soot Imp")
    void blackSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new SootImp());
        harness.setHand(player1, List.of(new SmolderingButcher()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Soot Imp"));

        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
    }

    @Test
    @DisplayName("A multicolored spell that includes black does not trigger Soot Imp")
    void multicoloredBlackSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new SootImp());
        harness.setHand(player1, List.of(new StillmoonCavalier()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Soot Imp"));

        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
    }
    @Test
    @DisplayName("Player hexproof does not prevent the non-targeting life loss")
    void opponentWithHexproofStillLosesLife() {
        harness.addToBattlefield(player2, new SootImp());
        harness.addToBattlefield(player1, new LeylineOfSanctity());
        harness.setHand(player1, List.of(new NettleSentinel()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore - 1);
        harness.assertOnBattlefield(player1, "Nettle Sentinel");
    }

    @Test
    @DisplayName("Each Soot Imp triggers independently for the same nonblack spell")
    void multipleImpsEachCauseLifeLoss() {
        harness.addToBattlefield(player1, new SootImp());
        harness.addToBattlefield(player2, new SootImp());
        harness.setHand(player1, List.of(new NettleSentinel()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int casterLifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, casterLifeBefore - 2);
        harness.assertLife(player2, opponentLifeBefore);
        harness.assertOnBattlefield(player1, "Nettle Sentinel");
    }

    @Test
    @DisplayName("Casting Soot Imp itself does not cause life loss")
    void castingSootImpDoesNotTrigger() {
        harness.setHand(player1, List.of(new SootImp()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
        harness.assertOnBattlefield(player1, "Soot Imp");
    }

    @Test
    @DisplayName("A white-and-black hybrid spell remains black when paid for with white mana")
    void blackHybridSpellPaidWithWhiteDoesNotTrigger() {
        harness.addToBattlefield(player1, new SootImp());
        harness.setHand(player1, List.of(new StillmoonCavalier()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore);
        harness.assertOnBattlefield(player1, "Stillmoon Cavalier");
    }
}
