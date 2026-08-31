package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.f.FalconerAdept;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Knightfisher.class, AvenFisher.class, FalconerAdept.class, GrizzlyBears.class})
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
        Permanent falconer = harness.addToBattlefieldAndReturn(player1, new FalconerAdept());
        falconer.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player1, List.of(1));
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
}
