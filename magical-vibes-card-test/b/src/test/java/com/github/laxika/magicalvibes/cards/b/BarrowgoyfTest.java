package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LavaSpike;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Barrowgoyf.class, Forest.class, GrizzlyBears.class, LavaSpike.class, Millstone.class,
        Ornithopter.class, Shock.class})
class BarrowgoyfTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness count distinct card types in all graveyards")
    void countsDistinctCardTypesInAllGraveyards() {
        Permanent barrowgoyf = addCreatureReady(player1, new Barrowgoyf());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock(), new Millstone()));
        harness.setGraveyard(player2, List.of(new LavaSpike(), new Ornithopter()));

        assertThat(gqs.getEffectivePower(gd, barrowgoyf)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, barrowgoyf)).isEqualTo(6);
    }

    @Test
    @DisplayName("Combat damage may mill that many cards and return milled creatures")
    void millsCombatDamageAndReturnsMilledCreatures() {
        Permanent barrowgoyf = addCreatureReady(player1, new Barrowgoyf());
        barrowgoyf.setAttacking(true);
        harness.setGraveyard(player1, List.of(new Forest(), new Shock(), new Millstone()));

        GrizzlyBears milledCreature = new GrizzlyBears();
        Card milledNoncreature = new Shock();
        harness.setLibrary(player1, List.of(milledCreature, milledNoncreature, new Forest()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(milledCreature);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(milledNoncreature)
                .hasSize(5);
    }

    @Test
    @DisplayName("Declining the mill leaves the library unchanged")
    void mayDeclineToMill() {
        Permanent barrowgoyf = addCreatureReady(player1, new Barrowgoyf());
        barrowgoyf.setAttacking(true);
        harness.setGraveyard(player1, List.of(new Forest(), new Shock(), new Millstone()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));

        resolveCombat();
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Shock", "Millstone");
    }
    @Test
    @DisplayName("Power and toughness update as graveyard card types change")
    void updatesAsGraveyardTypesChange() {
        Permanent barrowgoyf = addCreatureReady(player1, new Barrowgoyf());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        assertThat(gqs.getEffectivePower(gd, barrowgoyf)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, barrowgoyf)).isEqualTo(1);

        harness.setGraveyard(player2, List.of(new Ornithopter()));
        assertThat(gqs.getEffectivePower(gd, barrowgoyf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, barrowgoyf)).isEqualTo(3);

        harness.setGraveyard(player2, List.of());
        assertThat(gqs.getEffectivePower(gd, barrowgoyf)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, barrowgoyf)).isEqualTo(1);
    }

    @Test
    @DisplayName("Accepting the mill does not require returning a creature")
    void mayDeclineToReturnMilledCreature() {
        Permanent barrowgoyf = addCreatureReady(player1, new Barrowgoyf());
        barrowgoyf.setAttacking(true);
        harness.setGraveyard(player1, List.of(new Forest(), new Shock(), new Millstone()));
        GrizzlyBears creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature, new Forest(), new Shock()));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature).hasSize(6);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only one creature from this mill can be returned, including artifact creatures")
    void choosesOneOfMultipleMilledCreatures() {
        Permanent barrowgoyf = addCreatureReady(player1, new Barrowgoyf());
        barrowgoyf.setAttacking(true);
        GrizzlyBears oldCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new Forest(), new Shock(), new Millstone(), oldCreature));
        GrizzlyBears firstCreature = new GrizzlyBears();
        Ornithopter chosenCreature = new Ornithopter();
        GrizzlyBears lastCreature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstCreature, chosenCreature, lastCreature, new Forest()));

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .contains(chosenCreature).doesNotContain(firstCreature, lastCreature, oldCreature);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(firstCreature, lastCreature, oldCreature).doesNotContain(chosenCreature).hasSize(7);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Mill amount remembers combat damage rather than the current power")
    void millsDamageAmountAfterGraveyardChanges() {
        Permanent barrowgoyf = addCreatureReady(player1, new Barrowgoyf());
        barrowgoyf.setAttacking(true);
        harness.setGraveyard(player1, List.of(new Forest(), new Shock(), new Millstone()));
        Forest first = new Forest();
        Shock second = new Shock();
        Millstone third = new Millstone();
        GrizzlyBears fourth = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third, fourth));

        resolveCombat();
        harness.passBothPriorities();
        harness.setGraveyard(player1, List.of());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fourth);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
    @Test
    @DisplayName("Zero power deals no combat damage and does not offer milling")
    void zeroPowerDoesNotTriggerMill() {
        Permanent barrowgoyf = addCreatureReady(player1, new Barrowgoyf());
        barrowgoyf.setAttacking(true);
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        Forest card = new Forest();
        harness.setLibrary(player1, List.of(card));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Combat damage gains life even when the mill is declined")
    void lifelinkDoesNotDependOnAcceptingMill() {
        Permanent barrowgoyf = addCreatureReady(player1, new Barrowgoyf());
        barrowgoyf.setAttacking(true);
        harness.setGraveyard(player1, List.of(new Forest(), new Shock(), new Millstone()));
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }
}
