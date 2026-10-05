package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.f.FalconerAdept;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.x.Xenograft;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Knightfisher.class, AvenFisher.class, FalconerAdept.class, GrizzlyBears.class, Xenograft.class})
class KnightfisherTest extends BaseCardTest {

    @Test
    @DisplayName("Another nontoken Bird creates a 1/1 blue Fish token")
    void nontokenBirdCreatesFish() {
        harness.addToBattlefield(player1, new Knightfisher());
        harness.setHand(player1, List.of(new AvenFisher()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> fish = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.FISH))
                .toList();
        assertThat(fish).hasSize(1);
        assertThat(fish.getFirst().getCard().getPower()).isEqualTo(1);
        assertThat(fish.getFirst().getCard().getToughness()).isEqualTo(1);
        assertThat(fish.getFirst().getCard().getColor()).isEqualTo(CardColor.BLUE);
    }

    @Test
    @DisplayName("A non-Bird creature does not create a Fish token")
    void nonBirdDoesNotCreateFish() {
        harness.addToBattlefield(player1, new Knightfisher());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.FISH)))
                .isEmpty();
    }

    @Test
    @DisplayName("A Bird token does not create a Fish token")
    void birdTokenDoesNotCreateFish() {
        harness.addToBattlefield(player1, new Knightfisher());
        addCreatureReady(player1, new FalconerAdept());

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.BIRD)))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.FISH)))
                .isEmpty();
    }

    @Test
    @DisplayName("Knightfisher does not trigger from entering the battlefield itself")
    void ownEntryDoesNotCreateFish() {
        harness.setHand(player1, List.of(new Knightfisher()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.FISH)))
                .isEmpty();
    }

    @Test
    @CardUsed(Knightfisher.class)
    @DisplayName("Another Knightfisher creates one Fish without triggering itself")
    void anotherKnightfisherCreatesOneFish() {
        harness.addToBattlefield(player1, new Knightfisher());
        harness.setHand(player1, List.of(new Knightfisher()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Fish")).isEqualTo(1);
        assertThat(countPermanents(player2, "Fish")).isZero();
    }

    @Test
    @CardUsed(Knightfisher.class)
    @DisplayName("An opposing nontoken Bird does not create a Fish")
    void opposingBirdDoesNotCreateFish() {
        harness.addToBattlefield(player1, new Knightfisher());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Knightfisher()));
        harness.addMana(player2, ManaColor.BLUE, 5);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Knightfisher");
        assertThat(countPermanents(player1, "Fish")).isZero();
        assertThat(countPermanents(player2, "Fish")).isZero();
    }

    @Test
    @CardUsed(Knightfisher.class)
    @DisplayName("Each existing Knightfisher creates a Fish when another enters")
    void multipleKnightfishersEachCreateFish() {
        harness.addToBattlefield(player1, new Knightfisher());
        harness.addToBattlefield(player1, new Knightfisher());
        harness.setHand(player1, List.of(new Knightfisher()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Fish")).isEqualTo(2);
        assertThat(countPermanents(player2, "Fish")).isZero();
    }

    @Test
    @CardUsed({Knightfisher.class, Xenograft.class, GrizzlyBears.class})
    @DisplayName("A creature entering as a Bird due to Xenograft creates a Fish")
    void birdTypeGrantedOnBattlefieldCreatesFish() {
        harness.addToBattlefield(player1, new Knightfisher());
        harness.setHand(player1, List.of(new Xenograft(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BIRD");
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(countPermanents(player1, "Fish")).isEqualTo(1);
    }
}
