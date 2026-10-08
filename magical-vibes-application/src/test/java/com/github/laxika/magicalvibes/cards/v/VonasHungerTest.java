package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VonasHunger.class, RaptorCompanion.class, Forest.class})
class VonasHungerTest extends BaseCardTest {

    @Test
    @DisplayName("Without the city's blessing, each opponent sacrifices one creature")
    void eachOpponentSacrificesOneCreatureWithoutBlessing() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        harness.addToBattlefield(player2, new RaptorCompanion());
        castAndResolve();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("With the city's blessing, each opponent sacrifices half their creatures rounded up")
    void eachOpponentSacrificesHalfWithBlessing() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player2, new RaptorCompanion());
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

    @Test
    void opponentChoosesOneCreatureWithoutBlessing() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        castAndResolve();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(first, land);
        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void blessingPersistsBelowTenPermanentsAndEvenCreatureCountIsHalved() {
        for (int i = 0; i < 10; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        castAndResolve();
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        gd.playerBattlefields.get(player1.getId()).clear();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        Permanent fourth = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());

        castAndResolve();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                first.getId(), second.getId(), third.getId(), fourth.getId());
        harness.handleMultiplePermanentsChosen(player2, List.of(second.getId(), fourth.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(land, first, third);
        assertThat(gd.playersWithCityBlessing).contains(player1.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void ninePermanentsDoNotGrantBlessingAndOpponentWithoutCreaturesSacrificesNothing() {
        for (int i = 0; i < 9; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        castAndResolve();

        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(land);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void ascendChecksPermanentCountAtResolutionRatherThanCasting() {
        for (int i = 0; i < 10; i++) {
            harness.addToBattlefield(player1, new Forest());
        }
        harness.setHand(player1, List.of(new VonasHunger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);

        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        gd.playerBattlefields.get(player1.getId()).removeFirst();
        harness.passBothPriorities();

        assertThat(gd.playersWithCityBlessing).doesNotContain(player1.getId());
        assertThat(gd.stack).isEmpty();
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new VonasHunger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);
    }
}
