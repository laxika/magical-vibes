package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DreadReturn.class, GrizzlyBears.class, HolyDay.class, Mountain.class})
class DreadReturnTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature from your graveyard to the battlefield")
    void returnsCreatureFromGraveyardToBattlefield() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DreadReturn()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature card in your graveyard")
    void cannotTargetNoncreatureCard() {
        Card noncreature = new HolyDay();
        harness.setGraveyard(player1, List.of(noncreature));
        harness.setHand(player1, List.of(new DreadReturn()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, noncreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flashback sacrifices three creatures, returns the target, and exiles the spell")
    void flashbackSacrificesThreeCreatures() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent third = addCreatureReady(player1, new GrizzlyBears());
        Card creature = new GrizzlyBears();
        DreadReturn spell = new DreadReturn();
        harness.setGraveyard(player1, List.of(spell, creature));

        harness.castFromGraveyardWithSacrifices(player1, 0, creature.getId(),
                List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> List.of(first.getId(), second.getId(), third.getId())
                        .contains(permanent.getId()));
        assertThat(gameData.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getCard().getId(), second.getCard().getId(), third.getCard().getId());
        assertThat(gameData.findExiledCard(spell.getId())).isNotNull();
    }

    @Test
    @DisplayName("Flashback requires exactly three creatures to sacrifice")
    void flashbackRequiresThreeCreatures() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Card creature = new GrizzlyBears();
        DreadReturn spell = new DreadReturn();
        harness.setGraveyard(player1, List.of(spell, creature));

        assertThatThrownBy(() -> harness.castFromGraveyardWithSacrifices(player1, 0, creature.getId(),
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Dread Return");
    }

    @Test
    @DisplayName("Flashback cannot sacrifice a noncreature permanent")
    void flashbackRequiresCreatures() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(new DreadReturn(), creature));

        assertThatThrownBy(() -> harness.castFromGraveyardWithSacrifices(player1, 0, creature.getId(),
                List.of(first.getId(), second.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
