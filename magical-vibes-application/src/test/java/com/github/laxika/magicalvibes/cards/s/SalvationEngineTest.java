package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SalvationEngineTest extends BaseCardTest {

    @Test
    @DisplayName("Other artifact creatures you control get +2/+2")
    void buffsOtherArtifactCreatures() {
        harness.addToBattlefield(player1, new SalvationEngine());
        harness.addToBattlefield(player1, new Ornithopter());

        Permanent ornithopter = findPermanent(player1, "Ornithopter");

        assertThat(gqs.getEffectivePower(gd, ornithopter)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ornithopter)).isEqualTo(4);
    }

    @Test
    @DisplayName("Nonartifact creatures and opposing artifact creatures are not boosted")
    void onlyBoostsOwnArtifactCreatures() {
        harness.addToBattlefield(player1, new SalvationEngine());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Ornithopter());

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent opponentOrnithopter = findPermanent(player2, "Ornithopter");

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentOrnithopter)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, opponentOrnithopter)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking offers up to one artifact card from your graveyard")
    void attackReturnsArtifactCard() {
        Card spellbook = new Spellbook();
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(spellbook, bears));
        addReadySalvationEngine();
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(spellbook.getId());

        harness.handleMultipleCardsChosen(player1, List.of(spellbook.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the optional artifact return leaves the graveyard unchanged")
    void decliningArtifactReturnDoesNothing() {
        Card spellbook = new Spellbook();
        harness.setGraveyard(player1, List.of(spellbook));
        addReadySalvationEngine();
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertInGraveyard(player1, "Spellbook");
        harness.assertNotOnBattlefield(player1, "Spellbook");
    }

    private Permanent addReadySalvationEngine() {
        Permanent engine = new Permanent(new SalvationEngine());
        engine.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(engine);
        return engine;
    }
}
