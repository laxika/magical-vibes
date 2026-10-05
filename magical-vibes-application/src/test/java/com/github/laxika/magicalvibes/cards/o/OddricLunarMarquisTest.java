package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BandingSliver;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.EyelessWatcher;
import com.github.laxika.magicalvibes.cards.d.DryadSophisticate;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.MistIntruder;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OddricLunarMarquis.class, BandingSliver.class, GrizzlyBears.class,
        LlanowarElves.class, MistIntruder.class, EyelessWatcher.class,
        DryadSophisticate.class, OrzhovBasilica.class})
class OddricLunarMarquisTest extends BaseCardTest {

    private OddricLunarMarquis oddric() {
        OddricLunarMarquis oddric = new OddricLunarMarquis();
        oddric.setName("Oddric, Lunar Marquis");
        oddric.setType(CardType.CREATURE);
        oddric.setPower(3);
        oddric.setToughness(3);
        return oddric;
    }

    private void advanceToCombatAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
        resolveAllTriggers();
    }

    @Test
    @DisplayName("Shares the listed keywords at the beginning of each combat")
    void sharesKeywords() {
        Permanent oddric = harness.addToBattlefieldAndReturn(player1, oddric());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent sliver = harness.addToBattlefieldAndReturn(player1, new BandingSliver());
        Permanent ingestCreature = harness.addToBattlefieldAndReturn(player1, new MistIntruder());
        ingestCreature.getGrantedKeywords().add(Keyword.TANTRUM);

        advanceToCombatAndResolve(player1);

        assertThat(gqs.hasKeyword(gd, oddric, Keyword.BANDING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.BANDING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TANTRUM)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INGEST)).isTrue();
        assertThat(gqs.hasKeyword(gd, sliver, Keyword.TANTRUM)).isTrue();
    }

    @Test
    @DisplayName("Shares the sacrifice-for-colorless ability when an Eldrazi Scion has it")
    void sharesColorlessSacrificeAbility() {
        harness.addToBattlefield(player1, oddric());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new EyelessWatcher());
        resolveAllTriggers();

        advanceToCombatAndResolve(player1);

        int bearsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bears);
        harness.activateAbility(player1, bearsIndex, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS))
                .isEqualTo(1);
    }

    @Test
    @DisplayName("A different mana ability does not enable the sacrifice-for-colorless grant")
    void doesNotShareUnrelatedManaAbility() {
        harness.addToBattlefield(player1, oddric());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());

        advanceToCombatAndResolve(player1);

        int bearsIndex = gd.playerBattlefields.get(player1.getId()).indexOf(bears);
        assertThatThrownBy(() -> harness.activateAbility(player1, bearsIndex, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @ParameterizedTest
    @EnumSource(value = Keyword.class, names = {"BANDING", "CHANGELING", "DEVOID", "FEAR",
            "FLANKING", "HORSEMANSHIP", "INGEST", "INTIMIDATE", "FORESTWALK", "MOUNTAINWALK",
            "ISLANDWALK", "SWAMPWALK", "PLAINSWALK", "DESERTWALK", "SHROUD", "TANTRUM", "WITHER"})
    void sharesEachListedKeywordOnOpponentsTurn(Keyword keyword) {
        harness.addToBattlefield(player1, oddric());
        Permanent donor = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        donor.getGrantedKeywords().add(keyword);
        Permanent recipient = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToCombatAndResolve(player2);

        assertThat(gqs.hasKeyword(gd, recipient, keyword)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, keyword)).isFalse();
        if (keyword == Keyword.CHANGELING) {
            assertThat(gqs.hasEffectiveSubtype(gd, recipient, CardSubtype.ELF)).isTrue();
            assertThat(gqs.hasEffectiveSubtype(gd, recipient, CardSubtype.SLIVER)).isTrue();
        }
        if (keyword != Keyword.ISLANDWALK) {
            assertThat(gqs.hasKeyword(gd, recipient, Keyword.ISLANDWALK)).isFalse();
        }
    }

    @Test
    void sharedDevoidMakesColoredCreaturesColorless() {
        harness.addToBattlefield(player1, oddric());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new MistIntruder());

        advanceToCombatAndResolve(player1);

        assertThat(gqs.getEffectiveColors(gd, bears)).isEmpty();
    }

    @Test
    void checksKeywordsAtResolutionAndDoesNotAffectLaterCreatures() {
        harness.addToBattlefield(player1, oddric());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.stack).hasSize(1);
        Permanent donor = harness.addToBattlefieldAndReturn(player1, new MistIntruder());

        resolveAllTriggers();
        gd.playerBattlefields.get(player1.getId()).remove(donor);
        gd.playerGraveyards.get(player1.getId()).add(donor.getCard());
        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.INGEST)).isTrue();
        assertThat(gqs.hasKeyword(gd, lateCreature, Keyword.INGEST)).isFalse();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INGEST)).isFalse();
    }

    @Test
    void grantedIngestExilesOneCardAndStacksWithPrintedIngest() {
        harness.addToBattlefield(player1, oddric());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent intruder = addCreatureReady(player1, new MistIntruder());
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        GrizzlyBears third = new GrizzlyBears();
        GrizzlyBears remaining = new GrizzlyBears();
        harness.setLibrary(player2, List.of(first, second, third, remaining));

        advanceToCombatAndResolve(player1);
        bears.setAttacking(true);
        intruder.setAttacking(true);
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.findExiledCard(first.getId())).isNotNull();
        assertThat(gd.findExiledCard(second.getId())).isNotNull();
        assertThat(gd.findExiledCard(third.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        harness.assertLife(player2, 17);
    }

    @Test
    void sharesNonbasicLandwalk() {
        harness.addToBattlefield(player1, oddric());
        harness.addToBattlefield(player1, new DryadSophisticate());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new OrzhovBasilica());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        advanceToCombatAndResolve(player1);
        bears.setAttacking(true);
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(bears)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }
}
