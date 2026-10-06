package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.Assassinate;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KathariRemnant.class, Assassinate.class, Forest.class, GrizzlyBears.class,
        HillGiant.class, LlanowarElves.class, Mountain.class, Plains.class})
class KathariRemnantTest extends BaseCardTest {

    @Test
    @DisplayName("Cascade skips lands and equal/greater-cost nonlands, stopping at the first lesser one")
    void cascadeDigsToFirstLesserNonland() {
        setupCasterTurn();

        // Kathari Remnant is {2}{U}{B} = mana value 4. Dig should skip the land and the MV-4 Hill Giant
        // (equal, not less), stop at Grizzly Bears (MV 2 < 4), and never touch the Elves beneath it.
        LlanowarElves belowHit = new LlanowarElves();
        harness.setLibrary(player1, List.of(
                new Mountain(), new HillGiant(), new GrizzlyBears(), belowHit));

        castKathariRemnant();
        harness.passBothPriorities(); // resolve the cascade trigger

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

        castKathariRemnant();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).anyMatch(se -> se.getCard().getName().equals("Grizzly Bears")
                && se.getEntryType() == StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(belowHit, land, skipped);
    }

    @Test
    @DisplayName("A hit with no legal target isn't cast and is bottomed with the other exiled cards")
    void uncastableHitIsBottomedWithTheRest() {
        setupCasterTurn();

        // Assassinate ({2}{B}, mana value 3 < 4) is a legal cascade hit, but it can only target a
        // tapped creature and the battlefield is empty, so it can't be cast at all (CR 601.2c).
        // CR 702.85a then puts every card exiled this way that wasn't cast on the bottom of the
        // library — the uncast hit included, not off into some other zone.
        Assassinate hit = new Assassinate();
        Mountain land = new Mountain();
        LlanowarElves belowHit = new LlanowarElves();
        harness.setLibrary(player1, List.of(land, hit, belowHit));

        castKathariRemnant();
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Assassinate"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Assassinate"));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(belowHit, land, hit);
    }

    @Test
    @DisplayName("Cascade with no qualifying nonland bottoms everything and prompts nothing")
    void noQualifyingCardBottomsEverything() {
        setupCasterTurn();

        harness.setLibrary(player1, List.of(new Forest(), new Mountain(), new Plains()));

        castKathariRemnant();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Activating {B} grants a regeneration shield")
    void regenerateGrantsShield() {
        addKathariReady(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent kathari = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        assertThat(kathari.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Kathari Remnant from lethal combat damage")
    void regenerationSavesFromLethalCombat() {
        Permanent kathariPerm = addKathariReady(player1);
        kathariPerm.setRegenerationShield(1);
        kathariPerm.setBlocking(true);
        kathariPerm.addBlockingTarget(0);

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kathari Remnant");
        Permanent kathari = findPermanent(player1, "Kathari Remnant");
        assertThat(kathari.isTapped()).isTrue();
        assertThat(kathari.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Without a regeneration shield, lethal combat damage kills Kathari Remnant")
    void diesWithoutShield() {
        Permanent kathariPerm = addKathariReady(player1);
        kathariPerm.setBlocking(true);
        kathariPerm.addBlockingTarget(0);

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Kathari Remnant");
        harness.assertInGraveyard(player1, "Kathari Remnant");
    }

    @Test
    @DisplayName("Declining cascade bottoms the hit and skipped cards beneath the untouched library")
    void decliningCascadeBottomsAllExiledCards() {
        setupCasterTurn();
        Mountain land = new Mountain();
        GrizzlyBears hit = new GrizzlyBears();
        LlanowarElves untouched = new LlanowarElves();
        harness.setLibrary(player1, List.of(land, hit, untouched));

        castKathariRemnant();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(untouched, land, hit);
        assertThat(gd.findExiledCard(land.getId())).isNull();
        assertThat(gd.findExiledCard(hit.getId())).isNull();
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == hit);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Kathari Remnant");
    }

    @Test
    @DisplayName("Regeneration can be activated while tapped and summoning sick and shields accumulate")
    void regenerationDoesNotRequireTappingOrHaste() {
        Permanent kathari = harness.addToBattlefieldAndReturn(player1, new KathariRemnant());
        kathari.tap();
        kathari.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(kathari.getRegenerationShield()).isEqualTo(2);
        assertThat(kathari.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Kathari Remnant");
    }

    @Test
    @DisplayName("An activated regeneration shield replaces destruction from Assassinate")
    void activatedShieldPreventsDestruction() {
        Permanent kathari = addKathariReady(player1);
        kathari.tap();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castSorcery(player2, 0, kathari.getId());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kathari Remnant");
        harness.assertNotInGraveyard(player1, "Kathari Remnant");
        harness.assertInGraveyard(player2, "Assassinate");
        assertThat(kathari.getRegenerationShield()).isZero();
        assertThat(kathari.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cascade with an empty library still lets Kathari Remnant resolve")
    void emptyLibraryDoesNotPreventResolution() {
        setupCasterTurn();
        harness.setLibrary(player1, List.of());
        castKathariRemnant();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kathari Remnant");
    }

    private void setupCasterTurn() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
    }

    private void castKathariRemnant() {
        harness.castFromHand(player1, new KathariRemnant(), "{2}{U}{B}");
    }

    private Permanent addKathariReady(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new KathariRemnant());
        perm.setSummoningSick(false);
        return perm;
    }
}
