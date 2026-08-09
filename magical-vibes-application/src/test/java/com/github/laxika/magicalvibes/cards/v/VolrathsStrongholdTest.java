package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
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

class VolrathsStrongholdTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds one colorless mana")
    void tapsForColorless() {
        Permanent stronghold = addStronghold();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(stronghold.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Puts a target creature card from the graveyard on top of the library")
    void putsTargetCreatureOnTopOfLibrary() {
        int strongholdIndex = addStrongholdIndex();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(creature)));
        harness.setLibrary(player1, new ArrayList<>(List.of(new HolyDay())));

        harness.activateAbilityWithGraveyardTargets(player1, strongholdIndex, 1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Only creature cards in your graveyard are legal targets")
    void rejectsInvalidGraveyardTargets() {
        int strongholdIndex = addStrongholdIndex();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        Card nonCreature = new HolyDay();
        Card opponentCreature = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(nonCreature)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(opponentCreature)));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, strongholdIndex, 1, List.of(nonCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, strongholdIndex, 1, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addStronghold() {
        Permanent stronghold = addStrongholdAndReturn();
        stronghold.setSummoningSick(false);
        return stronghold;
    }

    private int addStrongholdIndex() {
        addStronghold();
        return gd.playerBattlefields.get(player1.getId()).indexOf(findPermanent(player1, "Volrath's Stronghold"));
    }

    private Permanent addStrongholdAndReturn() {
        return harness.addToBattlefieldAndReturn(player1, new VolrathsStronghold());
    }
}
