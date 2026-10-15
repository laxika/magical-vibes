package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AnnihilatingGlare;
import com.github.laxika.magicalvibes.cards.a.AxiomEngraver;
import com.github.laxika.magicalvibes.cards.b.BlueSunsTwilight;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TamiyosImmobilizer;
import com.github.laxika.magicalvibes.cards.t.TamiyosLogbook;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OvikaEnigmaGoliath.class, Divination.class, GrizzlyBears.class,
        AnnihilatingGlare.class, AxiomEngraver.class, BlueSunsTwilight.class,
        TamiyosImmobilizer.class, TamiyosLogbook.class})
class OvikaEnigmaGoliathTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell creates tokens equal to its mana value with haste")
    void noncreatureSpellCreatesManaValueTokensWithHaste() {
        harness.addToBattlefield(player1, new OvikaEnigmaGoliath());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(countPermanents(player1, "Phyrexian Goblin")).isEqualTo(3);
        assertThat(findPermanents(player1, "Phyrexian Goblin"))
                .allSatisfy(token -> assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue());
    }

    @Test
    @DisplayName("Casting a creature spell does not create tokens")
    void creatureSpellDoesNotCreateTokens() {
        harness.addToBattlefield(player1, new OvikaEnigmaGoliath());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(countPermanents(player1, "Phyrexian Goblin")).isZero();
    }

    @Test
    @DisplayName("The tokens lose haste at cleanup")
    void tokenHasteWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new OvikaEnigmaGoliath());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
        Permanent token = findPermanent(player1, "Phyrexian Goblin");
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isFalse();
    }

    @Test
    void artifactSpellCreatesTokensBeforeItResolves() {
        harness.addToBattlefield(player1, new OvikaEnigmaGoliath());
        harness.setHand(player1, List.of(new TamiyosLogbook()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Phyrexian Goblin")).isEqualTo(3);
        harness.assertNotOnBattlefield(player1, "Tamiyo's Logbook");
        assertThat(findPermanents(player1, "Phyrexian Goblin"))
                .allSatisfy(token -> {
                    assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(1);
                    assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(1);
                    assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();
                });
    }

    @Test
    void xInSpellManaCostCountsTowardTokenNumber() {
        harness.addToBattlefield(player1, new OvikaEnigmaGoliath());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AxiomEngraver());
        harness.setHand(player1, List.of(new BlueSunsTwilight()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castAndResolveSorcery(player1, 0, 3, target.getId());

        assertThat(countPermanents(player1, "Phyrexian Goblin")).isEqualTo(5);
        assertThat(countPermanents(player2, "Phyrexian Goblin")).isZero();
    }

    @Test
    void opponentsNoncreatureSpellDoesNotCreateTokens() {
        harness.addToBattlefield(player1, new OvikaEnigmaGoliath());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new TamiyosLogbook()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castArtifact(player2, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Phyrexian Goblin")).isZero();
        assertThat(countPermanents(player2, "Phyrexian Goblin")).isZero();
        harness.assertOnBattlefield(player2, "Tamiyo's Logbook");
    }

    @Test
    void additionalManaCostDoesNotIncreaseTokenNumberAndOwnSpellDoesNotTriggerWard() {
        Permanent ovika = harness.addToBattlefieldAndReturn(player1, new OvikaEnigmaGoliath());
        harness.setHand(player1, List.of(new AnnihilatingGlare()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorceryWithSacrifice(player1, 0, ovika.getId(), null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Phyrexian Goblin")).isEqualTo(1);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Ovika, Enigma Goliath");
        harness.assertLife(player1, 20);
        assertThat(countPermanents(player1, "Phyrexian Goblin")).isEqualTo(1);
    }

    @Test
    void decliningWardCountersOpponentsSpell() {
        Permanent ovika = harness.addToBattlefieldAndReturn(player1, new OvikaEnigmaGoliath());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new AnnihilatingGlare()));
        harness.addMana(player2, ManaColor.BLACK, 8);

        harness.castSorceryWithSacrifice(player2, 0, ovika.getId(), null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        harness.assertOnBattlefield(player1, "Ovika, Enigma Goliath");
        harness.assertInGraveyard(player2, "Annihilating Glare");
        harness.assertLife(player2, 20);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isEqualTo(3);
    }

    @Test
    void payingWardRequiresBothThreeManaAndThreeLife() {
        Permanent ovika = harness.addToBattlefieldAndReturn(player1, new OvikaEnigmaGoliath());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new AnnihilatingGlare()));
        harness.addMana(player2, ManaColor.BLACK, 8);

        harness.castSorceryWithSacrifice(player2, 0, ovika.getId(), null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertLife(player2, 17);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Ovika, Enigma Goliath");
        harness.assertInGraveyard(player2, "Annihilating Glare");
    }

    @Test
    void wardAlsoCountersOpponentsActivatedAbility() {
        Permanent ovika = harness.addToBattlefieldAndReturn(player1, new OvikaEnigmaGoliath());
        Permanent immobilizer = harness.addToBattlefieldAndReturn(player2, new TamiyosImmobilizer());
        immobilizer.setCounterCount(CounterType.OIL, 1);
        harness.forceActivePlayer(player2);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.activateAbility(player2, 0, null, ovika.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(ovika.isTapped()).isFalse();
        assertThat(immobilizer.isTapped()).isTrue();
        assertThat(immobilizer.getCounterCount(CounterType.OIL)).isZero();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
        assertThat(countPermanents(player1, "Phyrexian Goblin")).isZero();
    }

    @Test
    void sacrificingOvikaAsCastingCostDoesNotTriggerItsAbility() {
        Permanent ovika = harness.addToBattlefieldAndReturn(player1, new OvikaEnigmaGoliath());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AxiomEngraver());
        harness.setHand(player1, List.of(new AnnihilatingGlare()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorceryWithSacrifice(player1, 0, target.getId(), ovika.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ovika, Enigma Goliath");
        harness.assertNotOnBattlefield(player2, "Axiom Engraver");
        assertThat(countPermanents(player1, "Phyrexian Goblin")).isZero();
    }
}
