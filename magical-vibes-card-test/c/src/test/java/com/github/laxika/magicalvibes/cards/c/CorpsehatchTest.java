package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.n.NullChampion;
import com.github.laxika.magicalvibes.cards.o.OnduGiant;
import com.github.laxika.magicalvibes.cards.r.Regress;
import com.github.laxika.magicalvibes.cards.u.UlamogTheInfiniteGyre;
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

@CardUsed({Corpsehatch.class, OnduGiant.class, NullChampion.class, Regress.class, UlamogTheInfiniteGyre.class})
class CorpsehatchTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys a nonblack creature and creates two Eldrazi Spawn tokens")
    void destroysTargetAndCreatesSpawnTokens() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OnduGiant());
        harness.setHand(player1, List.of(new Corpsehatch()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Ondu Giant");
        harness.assertInGraveyard(player2, "Ondu Giant");
        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(2);
    }

    @Test
    @DisplayName("Eldrazi Spawn tokens can be sacrificed for colorless mana")
    void spawnTokensProduceColorlessMana() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OnduGiant());
        harness.setHand(player1, List.of(new Corpsehatch()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        Permanent spawn = findPermanents(player1, "Eldrazi Spawn").getFirst();
        int spawnIndex = gd.playerBattlefields.get(player1.getId()).indexOf(spawn);
        harness.activateAbility(player1, spawnIndex, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a black creature")
    void cannotTargetBlackCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new NullChampion());
        harness.setHand(player1, List.of(new Corpsehatch()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creates Spawn even when an indestructible colorless target survives")
    void createsTokensWhenTargetCannotBeDestroyed() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new UlamogTheInfiniteGyre());
        harness.setHand(player1, List.of(new Corpsehatch()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertOnBattlefield(player2, "Ulamog, the Infinite Gyre");
        harness.assertNotInGraveyard(player2, "Ulamog, the Infinite Gyre");
        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(2);
        assertThat(findPermanents(player2, "Eldrazi Spawn")).isEmpty();
    }

    @Test
    @DisplayName("Creates no Spawn when the only target leaves before resolution")
    void createsNoTokensWhenTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OnduGiant());
        harness.setHand(player1, List.of(new Corpsehatch()));
        harness.setHand(player2, List.of(new Regress()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Ondu Giant");
        harness.assertInGraveyard(player1, "Corpsehatch");
        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
        assertThat(findPermanents(player2, "Eldrazi Spawn")).isEmpty();
    }

    @Test
    @DisplayName("Can destroy your own creature and creates untapped colorless 0/1 Spawn")
    void canTargetOwnCreatureAndCreatesCorrectTokens() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OnduGiant());
        harness.setHand(player1, List.of(new Corpsehatch()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, target.getId());

        harness.assertInGraveyard(player1, "Ondu Giant");
        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(2).allSatisfy(spawn -> {
            assertThat(spawn.getCard().isToken()).isTrue();
            assertThat(gqs.getEffectivePower(gd, spawn)).isZero();
            assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(1);
            assertThat(gqs.getEffectiveColors(gd, spawn)).isEmpty();
            assertThat(spawn.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ELDRAZI, CardSubtype.SPAWN);
            assertThat(spawn.isTapped()).isFalse();
        });
    }
}
