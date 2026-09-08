package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConjurersBaubleTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself, puts the targeted graveyard card on the bottom, and draws")
    void sacrificesAndTucksTargetThenDraws() {
        Permanent bauble = addBauble();
        Card target = new GrizzlyBears();
        Card draw = new Forest();
        harness.setGraveyard(player1, new ArrayList<>(List.of(target)));
        harness.setLibrary(player1, new ArrayList<>(List.of(draw)));

        harness.activateAbilityWithGraveyardTargets(player1, battlefieldIndex(bauble), 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bauble);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(bauble.getCard().getId())
                .doesNotContain(target.getId());
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(target.getId());
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .contains(draw.getId());
    }

    @Test
    @DisplayName("Allows choosing no graveyard target and still draws")
    void allowsNoTarget() {
        Permanent bauble = addBauble();
        Card draw = new Forest();
        harness.setGraveyard(player1, new ArrayList<>());
        harness.setLibrary(player1, new ArrayList<>(List.of(draw)));

        harness.activateAbilityWithGraveyardTargets(player1, battlefieldIndex(bauble), 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bauble);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(bauble.getCard().getId());
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .contains(draw.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a card in an opponent's graveyard")
    void rejectsOpponentGraveyardTarget() {
        Permanent bauble = addBauble();
        Card target = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(target));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, battlefieldIndex(bauble), 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bauble);
        assertThat(bauble.isTapped()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target);
    }

    private Permanent addBauble() {
        return harness.addToBattlefieldAndReturn(player1, new ConjurersBauble());
    }

    private int battlefieldIndex(Permanent bauble) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(bauble);
    }
}
