package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YoureInCommand.class, GrizzlyBears.class})
class YoureInCommandTest extends BaseCardTest {

    @Test
    @DisplayName("Makes the target creature the only commander and starts its tax at zero")
    void makesTargetTheOnlyCommander() {
        Card previousCommander = new GrizzlyBears();
        gd.playerCommanders.put(player1.getId(), new ArrayList<>(List.of(previousCommander)));
        gd.commanderTaxByCardId.put(previousCommander.getId(), 3);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        castYoureInCommand(target);

        assertThat(gd.playerCommanders.get(player1.getId())).containsExactly(target.getCard());
        assertThat(gd.commanderTaxByCardId).containsEntry(target.getCard().getId(), 0);
        assertThat(gd.commanderTaxByCardId).doesNotContainKey(previousCommander.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentsCreature() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new YoureInCommand()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you own and control");
    }

    @Test
    @DisplayName("Cannot target a creature the player does not own")
    void cannotTargetUnownedCreature() {
        Card targetCard = new GrizzlyBears();
        targetCard.setOwnerId(player2.getId());
        Permanent target = new Permanent(targetCard);
        target.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.setHand(player1, List.of(new YoureInCommand()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you own and control");
    }

    private void castYoureInCommand(Permanent target) {
        harness.setHand(player1, List.of(new YoureInCommand()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
