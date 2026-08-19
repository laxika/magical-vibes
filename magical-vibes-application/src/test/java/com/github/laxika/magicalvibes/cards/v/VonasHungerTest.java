package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class VonasHungerTest extends BaseCardTest {

    @Test
    @DisplayName("Without the city's blessing, each opponent sacrifices one creature")
    void eachOpponentSacrificesOneCreatureWithoutBlessing() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        castAndResolve();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("With the city's blessing, each opponent sacrifices half their creatures rounded up")
    void eachOpponentSacrificesHalfWithBlessing() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player2, new GrizzlyBears());
        }

        castAndResolve();

        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        List<UUID> chosen = gd.playerBattlefields.get(player2.getId()).stream()
                .map(Permanent::getId)
                .limit(2)
                .toList();
        harness.handleMultiplePermanentsChosen(player2, chosen);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new VonasHunger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
