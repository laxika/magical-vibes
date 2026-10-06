package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SalvationEngine.class, GrizzlyBears.class, Ornithopter.class, Spellbook.class})
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
        addCreatureReady(player1, new SalvationEngine());
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
        addCreatureReady(player1, new SalvationEngine());
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

    @Test
    @DisplayName("Crew uses boosted artifact creature power and permits summoning-sick crew")
    void boostedArtifactCreaturesCanCrewWithoutBoostingEngineItself() {
        Permanent engine = addCreatureReady(player1, new SalvationEngine());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Ornithopter());
        Permanent third = harness.addToBattlefieldAndReturn(player1, new Ornithopter());

        assertThat(gqs.isCreature(gd, engine)).isFalse();
        harness.activateAbility(player1, 0, null, null);

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, engine)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, engine)).isTrue();
        assertThat(engine.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, engine)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, engine)).isEqualTo(10);
    }

    @Test
    @DisplayName("Crew cannot be paid with total power below six")
    void insufficientPowerCannotCrew() {
        Permanent engine = harness.addToBattlefieldAndReturn(player1, new SalvationEngine());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, engine)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returned artifact creatures enter untapped, are boosted, and are not attacking")
    void attackReturnsArtifactCreature() {
        Card ornithopter = new Ornithopter();
        Card opposingArtifact = new Spellbook();
        harness.setGraveyard(player1, List.of(ornithopter));
        harness.setGraveyard(player2, List.of(opposingArtifact));
        addCreatureReady(player1, new SalvationEngine());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(ornithopter.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ornithopter.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Ornithopter");
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.isAttacking()).isFalse();
        assertThat(returned.isSummoningSick()).isTrue();
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(4);
        harness.assertNotInGraveyard(player1, "Ornithopter");
        harness.assertInGraveyard(player2, "Spellbook");
    }

    @Test
    @DisplayName("An artifact target that leaves the graveyard before resolution is not returned")
    void removedGraveyardTargetIsNotReturned() {
        Card spellbook = new Spellbook();
        harness.setGraveyard(player1, List.of(spellbook));
        addCreatureReady(player1, new SalvationEngine());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(spellbook.getId()));

        assertThat(gd.stack).hasSize(1);
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(spellbook));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spellbook");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spellbook);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Attacking with no artifact in the graveyard requires no choice")
    void attackWithNoEligibleArtifact() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        addCreatureReady(player1, new SalvationEngine());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(3);
    }
}
