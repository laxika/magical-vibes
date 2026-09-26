package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AlloyGolem;
import com.github.laxika.magicalvibes.cards.d.DrakeSkullCameo;
import com.github.laxika.magicalvibes.cards.q.QuirionElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Exclude.class, DrakeSkullCameo.class, QuirionElves.class, AlloyGolem.class})
class ExcludeTest extends BaseCardTest {

    @Test
    void cannotTargetANonCreatureSpell() {
        DrakeSkullCameo cameo = new DrakeSkullCameo();
        harness.setHand(player1, List.of(cameo));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.setHand(player2, List.of(new Exclude()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castArtifact(player1, 0);
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, cameo.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countersCreatureSpellAndDrawsACard() {
        QuirionElves elves = new QuirionElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 2);

        QuirionElves drawnCard = new QuirionElves();
        harness.setLibrary(player2, List.of(drawnCard));
        harness.setHand(player2, List.of(new Exclude()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elves.getId());

        harness.assertInGraveyard(player1, "Quirion Elves");
        harness.assertNotOnBattlefield(player1, "Quirion Elves");
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawnCard);
    }

    @Test
    void goesToGraveyardAfterResolving() {
        QuirionElves elves = new QuirionElves();
        harness.setHand(player1, List.of(elves));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Exclude()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, elves.getId());

        harness.assertInGraveyard(player2, "Exclude");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canTargetAnArtifactCreatureSpell() {
        AlloyGolem golem = new AlloyGolem();
        harness.setHand(player1, List.of(golem));
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.setLibrary(player2, List.of(new QuirionElves()));
        harness.setHand(player2, List.of(new Exclude()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, golem.getId());

        harness.assertInGraveyard(player1, "Alloy Golem");
        harness.assertNotOnBattlefield(player1, "Alloy Golem");
    }
}
