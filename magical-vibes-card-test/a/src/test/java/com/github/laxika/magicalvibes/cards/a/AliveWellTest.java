package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.k.KraulWarrior;
import com.github.laxika.magicalvibes.cards.s.StaffOfTheSunMagus;
import com.github.laxika.magicalvibes.cards.s.StaffOfTheWildMagus;
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

@CardUsed({AliveWell.class, KraulWarrior.class, StaffOfTheSunMagus.class, StaffOfTheWildMagus.class})
class AliveWellTest extends BaseCardTest {

    private static final int ALIVE = 0;
    private static final int WELL = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Alive creates a 3/3 green Centaur token")
    void aliveCreatesCentaur() {
        harness.setHand(player1, List.of(new AliveWell()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, ALIVE);

        List<Permanent> centaurs = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getSubtypes().contains(CardSubtype.CENTAUR))
                .toList();
        assertThat(centaurs).hasSize(1);
        assertThat(centaurs.getFirst().getCard().isToken()).isTrue();
        assertThat(centaurs.getFirst().getEffectivePower()).isEqualTo(3);
        assertThat(centaurs.getFirst().getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Well gains 2 life for each creature you control")
    void wellGainsLifeForControlledCreatures() {
        harness.addToBattlefield(player1, new KraulWarrior());
        harness.addToBattlefield(player1, new KraulWarrior());
        harness.addToBattlefield(player2, new KraulWarrior());
        harness.setHand(player1, List.of(new AliveWell()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveSorcery(player1, 0, WELL);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 4);
    }

    @Test
    @DisplayName("Fuse creates the Centaur before counting creatures for Well")
    void fuseResolvesAliveBeforeWell() {
        harness.addToBattlefield(player1, new KraulWarrior());
        harness.addToBattlefield(player1, new KraulWarrior());
        harness.setHand(player1, List.of(new AliveWell()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castModalSorcery(player1, 0, FUSE, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).anySatisfy(permanent ->
                assertThat(permanent.getCard().getSubtypes()).contains(CardSubtype.CENTAUR));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 6);
    }

    @Test
    @DisplayName("Fuse requires the combined cost")
    void fuseRequiresBothHalvesCost() {
        harness.setHand(player1, List.of(new AliveWell()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castModalSorcery(player1, 0, FUSE, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Well gains no life when its controller has no creatures")
    void wellWithNoControlledCreatures() {
        harness.addToBattlefield(player2, new KraulWarrior());
        harness.setHand(player1, List.of(new AliveWell()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveSorcery(player1, 0, WELL);

        harness.assertLife(player1, lifeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Well counts creatures at resolution rather than when cast")
    void wellCountsCreaturesAtResolution() {
        harness.setHand(player1, List.of(new AliveWell()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castSorcery(player1, 0, WELL);
        harness.addToBattlefield(player1, new KraulWarrior());
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 2);
    }

    @Test
    @DisplayName("Fusing on an empty battlefield gains life for the newly created Centaur")
    void fuseOnEmptyBattlefield() {
        harness.setHand(player1, List.of(new AliveWell()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castAndResolveSorcery(player1, 0, FUSE);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, lifeBefore + 2);
    }

    @Test
    @DisplayName("Casting Alive alone is green and does not trigger a white-spell ability")
    void aliveIsOnlyGreenOnStack() {
        harness.addToBattlefield(player1, new StaffOfTheSunMagus());
        harness.addToBattlefield(player1, new StaffOfTheWildMagus());
        harness.setHand(player1, List.of(new AliveWell()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castSorcery(player1, 0, ALIVE);
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore + 1);
        assertThat(countPermanents(player1, "Centaur")).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting Well alone is white and does not trigger a green-spell ability")
    void wellIsOnlyWhiteOnStack() {
        harness.addToBattlefield(player1, new StaffOfTheSunMagus());
        harness.addToBattlefield(player1, new StaffOfTheWildMagus());
        harness.setHand(player1, List.of(new AliveWell()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castSorcery(player1, 0, WELL);
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A fused spell is both green and white")
    void fusedSpellTriggersBothColors() {
        harness.addToBattlefield(player1, new StaffOfTheSunMagus());
        harness.addToBattlefield(player1, new StaffOfTheWildMagus());
        harness.setHand(player1, List.of(new AliveWell()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castSorcery(player1, 0, FUSE);
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore + 4);
        assertThat(countPermanents(player1, "Centaur")).isEqualTo(1);
    }
}
