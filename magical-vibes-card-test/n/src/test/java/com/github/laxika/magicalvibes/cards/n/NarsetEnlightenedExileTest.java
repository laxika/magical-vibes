package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NarsetEnlightenedExile.class, GrizzlyBears.class, Shock.class,
        CounselOfTheSoratami.class, Forest.class, Unsummon.class})
class NarsetEnlightenedExileTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control have prowess")
    void grantsProwessToAllCreaturesYouControl() {
        Permanent narset = addCreatureReady(player1, new NarsetEnlightenedExile());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, narset)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, narset)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
    }

    @Test
    @DisplayName("Attacking selects a noncreature nonland card below Narset's power and casts its copy for free")
    void attacksExileAndCastCopy() {
        addCreatureReady(player1, new NarsetEnlightenedExile());
        Shock valid = new Shock();
        CounselOfTheSoratami equalToPower = new CounselOfTheSoratami();
        Forest land = new Forest();
        GrizzlyBears creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(valid, equalToPower, land, creature));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        declareAttackers(List.of(0));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(valid.getId());

        harness.handleMultipleCardsChosen(player1, List.of(valid.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.permanentChoiceContext())
                .isInstanceOf(PermanentChoiceContext.ExileCastSpellTarget.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(valid);
    }


    @Test
    void creatureSpellsDoNotTriggerProwess() {
        Permanent narset = addCreatureReady(player1, new NarsetEnlightenedExile());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, narset)).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void mayDeclineCopyFromOwnGraveyard() {
        addCreatureReady(player1, new NarsetEnlightenedExile());
        Shock original = new Shock();
        harness.setGraveyard(player1, List.of(original));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(original.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(original);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void castsSorceryCopyDuringCombatWithoutManaAndTriggersProwess() {
        Permanent narset = addCreatureReady(player1, new NarsetEnlightenedExile());
        narset.getCounters().put(CounterType.PLUS_ONE_PLUS_ONE, 1);
        CounselOfTheSoratami original = new CounselOfTheSoratami();
        harness.setGraveyard(player2, List.of(original));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(original.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gqs.getEffectivePower(gd, narset)).isEqualTo(5);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(original);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void usesLastBattlefieldPowerWhenBoostedNarsetLeavesBeforeResolution() {
        Permanent narset = addCreatureReady(player1, new NarsetEnlightenedExile());
        narset.getCounters().put(CounterType.PLUS_ONE_PLUS_ONE, 1);
        CounselOfTheSoratami original = new CounselOfTheSoratami();
        harness.setGraveyard(player2, List.of(original));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(original.getId()));
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, narset.getId());
        harness.passBothPriorities();
        harness.assertInHand(player1, "Narset, Enlightened Exile");
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(original);
        harness.handleMayAbilityChosen(player1, false);
    }

}
