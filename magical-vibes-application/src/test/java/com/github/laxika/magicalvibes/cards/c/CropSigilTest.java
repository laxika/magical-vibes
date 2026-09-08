package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CropSigilTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to one creature and up to one land from the graveyard")
    void returnsCreatureAndLand() {
        Permanent cropSigil = addCropSigil();
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setGraveyard(player1, deliriumGraveyard(creature, land));
        addManaForAbility();

        harness.activateAbilityWithGraveyardTargets(player1, cropSigilIndex(cropSigil), 0,
                List.of(creature.getId(), land.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Crop Sigil");
    }

    @Test
    @DisplayName("May activate and sacrifice itself without choosing targets")
    void allowsNoTargets() {
        Permanent cropSigil = addCropSigil();
        harness.setGraveyard(player1, deliriumGraveyard());
        addManaForAbility();

        harness.activateAbilityWithGraveyardTargets(player1, cropSigilIndex(cropSigil), 0, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Crop Sigil");
    }

    @Test
    @DisplayName("Rejects two creature targets")
    void rejectsTwoCreatureTargets() {
        Permanent cropSigil = addCropSigil();
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        harness.setGraveyard(player1, deliriumGraveyard(firstCreature, secondCreature));
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, cropSigilIndex(cropSigil), 0, List.of(firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at most one creature");
    }

    @Test
    @DisplayName("Requires delirium to activate")
    void requiresDelirium() {
        Permanent cropSigil = addCropSigil();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new HolyDay()));
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, cropSigilIndex(cropSigil), 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("four or more card types");
    }

    @Test
    @DisplayName("May mill a card during upkeep")
    void mayMillDuringUpkeep() {
        addCropSigil();
        Card milled = new GrizzlyBears();
        harness.setLibrary(player1, new ArrayList<>(List.of(milled)));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private Permanent addCropSigil() {
        return harness.addToBattlefieldAndReturn(player1, new CropSigil());
    }

    private int cropSigilIndex(Permanent cropSigil) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(cropSigil);
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

    private List<Card> deliriumGraveyard(Card... additionalCards) {
        List<Card> cards = new ArrayList<>(List.of(
                new HolyDay(), new Divination(), new Ornithopter()));
        cards.addAll(List.of(additionalCards));
        return cards;
    }
}
