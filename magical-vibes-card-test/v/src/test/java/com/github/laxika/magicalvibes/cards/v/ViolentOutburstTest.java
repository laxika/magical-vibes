package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BoneSplinters;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ViolentOutburst.class, Forest.class, GrizzlyBears.class, HillGiant.class,
        LlanowarElves.class, Mountain.class, BoneSplinters.class})
class ViolentOutburstTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control get +1/+0; opponents' creatures unaffected")
    void pumpsOnlyOwnCreatures() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());   // 2/2
        Permanent theirs = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()); // 2/2

        castViolentOutburst();
        harness.passBothPriorities(); // resolve the cascade trigger (no hit, bottoms lands)
        harness.passBothPriorities(); // resolve the spell -> pump

        assertThat(mine.getEffectivePower()).isEqualTo(3);
        assertThat(mine.getEffectiveToughness()).isEqualTo(2);
        assertThat(theirs.getEffectivePower()).isEqualTo(2);
        assertThat(theirs.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castViolentOutburst();
        harness.passBothPriorities(); // cascade trigger
        harness.passBothPriorities(); // spell
        assertThat(mine.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(mine.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cascade digs past a land and a costlier nonland to the first nonland with lesser mana value")
    void cascadeDigsToFirstLesserNonland() {
        Permanent mine = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        // Violent Outburst is {1}{R}{G} = mana value 3. Dig skips the Mountain and the MV-4 Hill Giant,
        // stops at Grizzly Bears (MV 2 < 3), and never touches the Llanowar Elves beneath it.
        LlanowarElves belowHit = new LlanowarElves();
        Mountain land = new Mountain();
        HillGiant skipped = new HillGiant();
        harness.setLibrary(player1, List.of(land, skipped, new GrizzlyBears(), belowHit));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new ViolentOutburst(), "{1}{R}{G}");
        harness.passBothPriorities(); // resolve the cascade trigger

        // The single castable card offered is Grizzly Bears (the qualifying hit).
        List<String> castable = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
        assertThat(castable).containsExactly("Grizzly Bears");

        // Cast the offered hit for free.
        harness.handleCardChosen(player1, 0);
        assertThat(gd.stack).anyMatch(se -> se.getCard().getName().equals("Grizzly Bears")
                && se.getEntryType() == StackEntryType.CREATURE_SPELL);

        // Non-hit exiled cards (land + Hill Giant) go to the bottom; the below-hit card remains.
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(belowHit, land, skipped);

        // The pump still applies to the caster's creature once everything resolves.
        harness.passBothPriorities(); // resolve Grizzly Bears
        harness.passBothPriorities(); // resolve Violent Outburst
        assertThat(mine.getEffectivePower()).isEqualTo(3);
    }

    @Test
    @DisplayName("Declining cascade bottoms the hit and skipped cards without touching the remaining top")
    void mayDeclineCascade() {
        castViolentOutburst();
        Mountain skipped = new Mountain();
        GrizzlyBears hit = new GrizzlyBears();
        LlanowarElves untouched = new LlanowarElves();
        harness.setLibrary(player1, List.of(skipped, hit, untouched));

        harness.passBothPriorities();
        assertThat(gd.findExiledCard(skipped.getId())).isNotNull();
        assertThat(gd.findExiledCard(hit.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);

        harness.handleCardChosen(player1, -1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(untouched, skipped, hit);
        assertThat(gd.findExiledCard(skipped.getId())).isNull();
        assertThat(gd.findExiledCard(hit.getId())).isNull();
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Violent Outburst");
    }

    @Test
    @DisplayName("The cascaded creature resolves before the boost, and later creatures are unaffected")
    void boostsCascadedCreatureButNotLaterCreatures() {
        castViolentOutburst();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        Permanent cascaded = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(cascaded.getEffectivePower()).isEqualTo(2);
        harness.passBothPriorities();
        assertThat(cascaded.getEffectivePower()).isEqualTo(3);
        assertThat(cascaded.getEffectiveToughness()).isEqualTo(2);

        Permanent later = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(later.getEffectivePower()).isEqualTo(2);
        assertThat(later.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cascade skips another spell of equal mana value")
    void skipsEqualManaValue() {
        castViolentOutburst();
        ViolentOutburst equal = new ViolentOutburst();
        GrizzlyBears hit = new GrizzlyBears();
        harness.setLibrary(player1, List.of(equal, hit));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(hit);
        harness.handleCardChosen(player1, -1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(equal, hit);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Casting Bone Splinters through cascade still requires sacrificing a creature")
    void cascadeMustPayMandatorySacrificeCost() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castViolentOutburst();
        harness.setLibrary(player1, List.of(new BoneSplinters()));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, sacrifice.getId());
        }

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrifice);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Hill Giant");
        harness.passBothPriorities();
    }
    private void castViolentOutburst() {
        // Library holds only lands so cascade finds no hit and prompts nothing.
        harness.setLibrary(player1, List.of(new Mountain(), new Forest()));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new ViolentOutburst(), "{1}{R}{G}");
    }
}
