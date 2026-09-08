package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhyrexianDragonEngine;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MishraClaimedByGixTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with two creatures drains each opponent for two and gains two life")
    void attacksDrainForNumberOfAttackers() {
        Permanent mishra = addReady(player1, new MishraClaimedByGix());
        Permanent bear = addReady(player1, new GrizzlyBears());
        addReady(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(mishra),
                gd.playerBattlefields.get(player1.getId()).indexOf(bear)));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Melds attacking Mishra with an attacking Phyrexian Dragon Engine")
    void meldsWhenBothPartsAttack() {
        Permanent mishra = addReady(player1, new MishraClaimedByGix());
        Permanent dragonEngine = addReady(player1, new PhyrexianDragonEngine());
        Permanent ownCreature = addReady(player1, new GrizzlyBears());
        addReady(player2, new GrizzlyBears());

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(mishra),
                gd.playerBattlefields.get(player1.getId()).indexOf(dragonEngine)));
        resolveUntilInputOrStackEmpty();
        harness.handleListChoice(player1, "Mishra deals 3 damage to any target");
        harness.handleListChoice(player1,
                "Creatures you control gain menace and trample until end of turn");
        harness.handleListChoice(player1, "Create two tapped Powerstone tokens");

        Permanent melded = findPermanent(player1, "Mishra, Lost to Phyrexia");
        assertThat(melded.getMeldComponentCards()).hasSize(2);
        assertThat(melded.isTapped()).isTrue();
        assertThat(melded.isAttacking()).isTrue();

        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(findPermanents(player1, "Powerstone")).hasSize(2);
    }

    @Test
    @DisplayName("Mishra's back face resolves exactly three distinct modes")
    void backFaceChoosesThreeDistinctModes() {
        Permanent ownCreature = addReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addReady(player2, new GrizzlyBears());
        Permanent mishra = addReady(player1, new MishraLostToPhyrexia());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(mishra)));
        resolveUntilInputOrStackEmpty();
        chooseNonTargetingBackModes();

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(ownCreature.getPowerModifier()).isZero();
        assertThat(opposingCreature.getPowerModifier()).isEqualTo(-1);
        assertThat(findPermanents(player1, "Powerstone")).hasSize(2);
        assertThat(findPermanents(player1, "Powerstone")).allMatch(Permanent::isTapped);
    }

    private void chooseNonTargetingBackModes() {
        harness.handleListChoice(player1,
                "Creatures you control gain menace and trample until end of turn");
        harness.handleListChoice(player1,
                "Creatures you don't control get -1/-1 until end of turn");
        harness.handleListChoice(player1, "Create two tapped Powerstone tokens");
    }

    private void resolveUntilInputOrStackEmpty() {
        while (!gd.interaction.isAwaitingInput() && !gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Player player,
            com.github.laxika.magicalvibes.model.Card card) {
        return addCreatureReady(player, card);
    }
}
