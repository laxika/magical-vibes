package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.cards.t.TravelingPhilosopher;
import com.github.laxika.magicalvibes.cards.v.VoyagesEnd;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EvangelOfHeliod.class, SuntailHawk.class, GrizzlyBears.class,
        TravelingPhilosopher.class, VoyagesEnd.class})
class EvangelOfHeliodTest extends BaseCardTest {

    private List<Permanent> soldiers() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Soldier"))
                .toList();
    }

    @Test
    @DisplayName("ETB creates Soldiers equal to your devotion to white")
    void etbCreatesSoldiersEqualToWhiteDevotion() {
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player1, new SuntailHawk());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new EvangelOfHeliod()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(soldiers()).hasSize(4);
    }

    @Test
    @DisplayName("Only white mana symbols among your permanents contribute")
    void etbIgnoresNonWhiteManaSymbols() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new EvangelOfHeliod()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(soldiers()).hasSize(2);
    }

    @Test
    @DisplayName("Opponents' white permanents and white cards in hand do not contribute")
    void ignoresOpponentsPermanentsAndCardsInHand() {
        harness.addToBattlefield(player2, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new EvangelOfHeliod(), new TravelingPhilosopher()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(soldiers()).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    @DisplayName("Devotion is evaluated when the ETB trigger resolves")
    void countsDevotionAtResolution() {
        harness.addToBattlefield(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new EvangelOfHeliod()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(new VoyagesEnd()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Traveling Philosopher"));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Traveling Philosopher");
        assertThat(soldiers()).hasSize(2);
    }

    @Test
    @DisplayName("The trigger survives Evangel leaving and can create zero tokens")
    void sourceLeavingReducesDevotionToZero() {
        harness.setHand(player1, List.of(new EvangelOfHeliod()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(new VoyagesEnd()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Evangel of Heliod"));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Evangel of Heliod");
        assertThat(soldiers()).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The trigger creates Soldiers using remaining devotion after Evangel leaves")
    void sourceLeavingStillCreatesSoldiersWithOracleCharacteristics() {
        harness.addToBattlefield(player1, new TravelingPhilosopher());
        harness.setHand(player1, List.of(new EvangelOfHeliod()));
        harness.addMana(player1, ManaColor.WHITE, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(new VoyagesEnd()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Evangel of Heliod"));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Evangel of Heliod");
        assertThat(soldiers()).hasSize(1).allSatisfy(soldier -> {
            assertThat(soldier.getCard().getType()).isEqualTo(CardType.CREATURE);
            assertThat(soldier.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(soldier.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
            assertThat(soldier.getCard().getPower()).isEqualTo(1);
            assertThat(soldier.getCard().getToughness()).isEqualTo(1);
            assertThat(soldier.isTapped()).isFalse();
        });
    }
}
