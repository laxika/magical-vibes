package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.d.DwarvenMauler;
import com.github.laxika.magicalvibes.cards.k.KLiTheResourceful;
import com.github.laxika.magicalvibes.cards.p.Persuasion;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FLiThePathfinder.class, FountainOfYouth.class, GrizzlyBears.class,
        DwarvenMauler.class, KLiTheResourceful.class, Persuasion.class})
class FLiThePathfinderTest extends BaseCardTest {

    @Test
    void boostsAllCreaturesAfterEnduringStory() {
        Permanent fili = harness.addToBattlefieldAndReturn(player1, new FLiThePathfinder());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        assertThat(gqs.getEffectivePower(gd, fili)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(2);

        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());

        assertThat(gqs.getEffectivePower(gd, fili)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, fili)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
    }

    @Test
    void ownEntryCreatesDwarfToken() {
        harness.enterBattlefieldAndReturn(player1, new FLiThePathfinder());
        resolveAllTriggers();

        Permanent token = findPermanent(player1, "Dwarf");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.DWARF);
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void nontokenDwarfEntryCreatesDwarfToken() {
        harness.addToBattlefield(player1, new FLiThePathfinder());
        harness.enterBattlefieldAndReturn(player1, dwarfCard(false));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    @Test
    void nonDwarfAndDwarfTokenEntriesDoNotTrigger() {
        harness.addToBattlefield(player1, new FLiThePathfinder());
        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, dwarfCard(true));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Dwarf")).isZero();
    }

    @Test
    void tokenCopyOfFiliCreatesDwarfOnItsOwnEntry() {
        FLiThePathfinder copy = new FLiThePathfinder();
        copy.setToken(true);

        harness.enterBattlefieldAndReturn(player1, copy);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Dwarf")).isEqualTo(1);
    }

    @Test
    void opponentsDwarfEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new FLiThePathfinder());

        harness.enterBattlefieldAndReturn(player2, new DwarvenMauler());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Dwarf")).isZero();
        assertThat(countPermanents(player2, "Dwarf")).isZero();
    }

    @Test
    void enduringStoryPersistsAfterQualifyingPermanentsLeaveAndDoesNotBoostOpponents() {
        Permanent fili = harness.addToBattlefieldAndReturn(player1, new FLiThePathfinder());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent firstArtifact = harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent secondArtifact = harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());

        gd.playerBattlefields.get(player1.getId()).removeAll(List.of(firstArtifact, secondArtifact));
        harness.runStateBasedActions();
        harness.enterBattlefieldAndReturn(player1, new DwarvenMauler());
        resolveAllTriggers();

        Permanent dwarf = findPermanent(player1, "Dwarf");
        assertThat(gqs.getEffectivePower(gd, fili)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, fili)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, dwarf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, dwarf)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponent)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponent)).isEqualTo(2);
    }

    @Test
    void gainingControlOfThirdQualifyingPermanentGrantsEnduringStory() {
        Permanent fili = harness.addToBattlefieldAndReturn(player1, new FLiThePathfinder());
        harness.enterBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent kili = harness.addToBattlefieldAndReturn(player2, new KLiTheResourceful());
        assertThat(gqs.getEffectivePower(gd, fili)).isEqualTo(2);

        harness.setHand(player1, List.of(new Persuasion()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, kili.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kili);
        assertThat(countPermanents(player1, "Dwarf")).isZero();
        assertThat(gqs.getEffectivePower(gd, fili)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, fili)).isEqualTo(3);
    }

    private static DwarvenMauler dwarfCard(boolean token) {
        DwarvenMauler card = new DwarvenMauler();
        card.setToken(token);
        return card;
    }
}
