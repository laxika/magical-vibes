package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.m.MoxDiamond;
import com.github.laxika.magicalvibes.cards.t.TidalWarrior;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VolrathsShapeshifter.class, Clone.class, MoxDiamond.class, TidalWarrior.class,
        VolrathsStronghold.class, VenerableMonk.class})
class VolrathsShapeshifterTest extends BaseCardTest {

    @Test
    @DisplayName("Its discard ability works when the graveyard top is not a creature")
    void discardAbilityWorksWithNonCreatureOnTop() {
        Permanent shapeshifter = addCreatureReady(player1, new VolrathsShapeshifter());
        Card nonCreatureTop = new MoxDiamond();
        Card discarded = new MoxDiamond();
        harness.setGraveyard(player1, List.of(nonCreatureTop));
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonCreatureTop, discarded);
        assertThat(shapeshifter.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Copies the activated ability of the top creature card")
    void copiesTopCreatureActivatedAbility() {
        addCreatureReady(player1, new VolrathsShapeshifter());
        Permanent stronghold = harness.addToBattlefieldAndReturn(player1, new VolrathsStronghold());
        harness.setGraveyard(player1, List.of(new TidalWarrior()));

        harness.activateAbility(player1, 0, 0, null, stronghold.getId());
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, stronghold)).containsExactly(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("Retains its discard ability while copying a creature")
    void retainsDiscardAbilityWhileCopyingCreature() {
        addCreatureReady(player1, new VolrathsShapeshifter());
        Card discarded = new MoxDiamond();
        TidalWarrior topCreature = new TidalWarrior();
        harness.setGraveyard(player1, List.of(topCreature));
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(topCreature, discarded);
    }

    @Test
    @DisplayName("Stops copying when the graveyard top changes to a noncreature")
    void stopsCopyingWhenTopChangesToNonCreature() {
        Permanent shapeshifter = addCreatureReady(player1, new VolrathsShapeshifter());
        Permanent stronghold = harness.addToBattlefieldAndReturn(player1, new VolrathsStronghold());
        harness.setGraveyard(player1, List.of(new TidalWarrior()));

        harness.activateAbility(player1, 0, 0, null, stronghold.getId());
        harness.passBothPriorities();
        shapeshifter.untap();
        gd.playerGraveyards.get(player1.getId()).add(new MoxDiamond());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, stronghold.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Uses only its controller's graveyard")
    void ignoresCreatureOnOpponentsGraveyardTop() {
        addCreatureReady(player1, new VolrathsShapeshifter());
        Permanent stronghold = harness.addToBattlefieldAndReturn(player1, new VolrathsStronghold());
        harness.setGraveyard(player1, List.of(new MoxDiamond()));
        harness.setGraveyard(player2, List.of(new TidalWarrior()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, stronghold.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A copy of it retains the graveyard-copy ability")
    void copiedShapeshifterStillCopiesTopCreature() {
        Permanent shapeshifter = addCreatureReady(player1, new VolrathsShapeshifter());
        Permanent stronghold = harness.addToBattlefieldAndReturn(player1, new VolrathsStronghold());
        harness.setGraveyard(player1, List.of(new MoxDiamond()));

        Clone clone = new Clone();
        harness.castFromHand(player1, clone, "{3}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, shapeshifter.getId());

        harness.setGraveyard(player1, List.of(new MoxDiamond(), new TidalWarrior()));

        Permanent copiedShapeshifter = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard().getId().equals(clone.getId()))
                .findFirst().orElseThrow();
        int copiedShapeshifterIndex = gd.playerBattlefields.get(player1.getId()).indexOf(copiedShapeshifter);
        copiedShapeshifter.setSummoningSick(false);
        harness.activateAbility(player1, copiedShapeshifterIndex, 0, null, stronghold.getId());
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, stronghold)).containsExactly(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("Triggers the creature card's enter-the-battlefield ability when it enters")
    void triggersCreatureEnterTheBattlefieldAbilityOnEntry() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, List.of(new VenerableMonk()));
        harness.setHand(player1, List.of(new VolrathsShapeshifter()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.stack).isEmpty();
    }
}
