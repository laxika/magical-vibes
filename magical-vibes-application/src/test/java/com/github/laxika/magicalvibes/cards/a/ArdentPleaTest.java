package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RuleOfLaw;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArdentPlea.class, Forest.class, GrizzlyBears.class, HillGiant.class,
        LlanowarElves.class, Mountain.class, Plains.class, RuleOfLaw.class})
class ArdentPleaTest extends BaseCardTest {

    @Test
    @DisplayName("Cascade exiles past lands and higher-cost nonlands to the first nonland with lesser mana value")
    void cascadeDigsToFirstLesserNonland() {
        setupCasterTurn();

        // Ardent Plea is {1}{W}{U} = mana value 3. Dig should skip the land and the MV-4 Hill Giant,
        // stop at Grizzly Bears (MV 2 < 3), and never touch the Llanowar Elves beneath it.
        LlanowarElves belowHit = new LlanowarElves();
        harness.setLibrary(player1, List.of(
                new Mountain(), new HillGiant(), new GrizzlyBears(), belowHit));

        castArdentPlea();
        harness.passBothPriorities(); // resolve the cascade trigger

        // The single castable card offered is Grizzly Bears (the qualifying hit).
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        List<String> castable = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
        assertThat(castable).containsExactly("Grizzly Bears");

        // The dig stopped at the hit — the card beneath it stays on the library.
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(belowHit);
    }

    @Test
    @DisplayName("Casting the cascade hit puts it on the stack for free; the rest go to the bottom")
    void castingHitPutsItOnStack() {
        setupCasterTurn();

        LlanowarElves belowHit = new LlanowarElves();
        Mountain land = new Mountain();
        HillGiant skipped = new HillGiant();
        harness.setLibrary(player1, List.of(land, skipped, new GrizzlyBears(), belowHit));

        castArdentPlea();
        harness.passBothPriorities();

        // Choose the offered hit (index 0) — cast it without paying its mana cost.
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).anyMatch(se -> se.getCard().getName().equals("Grizzly Bears")
                && se.getEntryType() == StackEntryType.CREATURE_SPELL);

        // Non-hit exiled cards (land + Hill Giant) are put on the bottom; the below-hit card remains.
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(belowHit, land, skipped);
    }

    @Test
    @DisplayName("Declining the cascade cast puts every exiled card on the bottom")
    void decliningBottomsAllExiled() {
        setupCasterTurn();

        Mountain land = new Mountain();
        GrizzlyBears hit = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, hit));

        castArdentPlea();
        harness.passBothPriorities();

        // Decline (fail to find) — nothing is cast, both exiled cards go to the bottom.
        harness.handleCardChosen(player1, -1);

        assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Grizzly Bears")
                && se.getEntryType() == StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, hit);
    }

    @Test
    @DisplayName("Cascade with no qualifying nonland bottoms everything and prompts nothing")
    void noQualifyingCardBottomsEverything() {
        setupCasterTurn();

        // All lands — the dig never finds a nonland with lesser mana value and empties the library.
        harness.setLibrary(player1, List.of(new Forest(), new Mountain(), new Plains()));

        castArdentPlea();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Exalted: a creature attacking alone gets +1/+1")
    void exaltedBoostsLoneAttacker() {
        harness.addToBattlefield(player1, new ArdentPlea());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1)); // Grizzly Bears attacks alone (enchantment is index 0)
        harness.passBothPriorities(); // resolve exalted trigger

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(3);
    }

    @Test
    @DisplayName("Exalted does not trigger when more than one creature attacks")
    void exaltedNoBoostWhenNotAlone() {
        harness.addToBattlefield(player1, new ArdentPlea());
        Permanent one = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1, 2)); // two attackers — not alone

        assertThat(gqs.getEffectivePower(gd, one)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, one)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cascade puts skipped cards and the hit into exile until casting is decided")
    void cascadeCardsAreInExileDuringChoice() {
        setupCasterTurn();
        Mountain land = new Mountain();
        HillGiant skipped = new HillGiant();
        GrizzlyBears hit = new GrizzlyBears();
        harness.setLibrary(player1, List.of(land, skipped, hit));

        castArdentPlea();
        harness.passBothPriorities();

        assertThat(gd.exiledCards).extracting(ExiledCardEntry::card)
                .containsExactlyInAnyOrder(land, skipped, hit);
        harness.handleCardChosen(player1, -1);
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, skipped, hit);
    }

    @Test
    @DisplayName("The cascade hit is cast from exile rather than the graveyard")
    void cascadeHitIsCastFromExile() {
        setupCasterTurn();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castArdentPlea();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getSpellsCastThisTurnCount(player1.getId(), Zone.EXILE)).isEqualTo(1);
        assertThat(gd.getSpellsCastThisTurnCount(player1.getId(), Zone.GRAVEYARD)).isZero();
    }

    @Test
    @DisplayName("Rule of Law prevents casting the cascade hit as the second spell")
    void cascadeRespectsRuleOfLaw() {
        setupCasterTurn();
        harness.addToBattlefield(player2, new RuleOfLaw());
        GrizzlyBears hit = new GrizzlyBears();
        harness.setLibrary(player1, List.of(hit));
        castArdentPlea();
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.LibrarySearch) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(hit.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(hit);
        assertThat(gd.getSpellsCastThisTurnCount(player1.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Cascade skips a nonland with equal mana value")
    void cascadeSkipsEqualManaValue() {
        setupCasterTurn();
        ArdentPlea equal = new ArdentPlea();
        GrizzlyBears hit = new GrizzlyBears();
        harness.setLibrary(player1, List.of(equal, hit));
        castArdentPlea();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(hit);
        harness.handleCardChosen(player1, -1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(equal, hit);
    }

    @Test
    @DisplayName("Putting Ardent Plea onto the battlefield does not trigger cascade")
    void enteringWithoutCastingDoesNotCascade() {
        GrizzlyBears top = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top));
        harness.addToBattlefield(player1, new ArdentPlea());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
    }

    @Test
    @DisplayName("Multiple Ardent Pleas each boost a lone attacker until end of turn")
    void exaltedStacksAndExpires() {
        harness.addToBattlefield(player1, new ArdentPlea());
        harness.addToBattlefield(player1, new ArdentPlea());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(player1, List.of(2));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Ardent Plea does not boost an opponent's lone attacker")
    void exaltedDoesNotBoostOpponent() {
        harness.addToBattlefield(player1, new ArdentPlea());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    private void setupCasterTurn() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
    }

    private void castArdentPlea() {
        harness.castFromHand(player1, new ArdentPlea(), "{1}{W}{U}");
    }
}
