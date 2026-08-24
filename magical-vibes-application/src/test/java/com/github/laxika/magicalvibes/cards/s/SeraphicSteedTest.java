package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeraphicSteed.class, GrizzlyBears.class})
class SeraphicSteedTest extends BaseCardTest {

    @Test
    @DisplayName("Saddle 4 taps other creatures and saddles Seraphic Steed")
    void saddleTapsOtherCreatures() {
        Permanent steed = addCreatureReady(player1, new SeraphicSteed());
        Permanent firstHelper = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondHelper = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(steed.isSaddled()).isTrue();
        assertThat(firstHelper.isTapped()).isTrue();
        assertThat(secondHelper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking while saddled creates a 3/3 flying Angel token")
    void attacksWhileSaddledCreatesAngel() {
        Permanent steed = addCreatureReady(player1, new SeraphicSteed());
        steed.setSaddled(true);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Angel")).singleElement()
                .satisfies(angel -> {
                    assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(3);
                    assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(3);
                    assertThat(gqs.hasKeyword(gd, angel, Keyword.FLYING)).isTrue();
                });
    }

    @Test
    @DisplayName("Attacking while not saddled does not create an Angel token")
    void doesNotCreateAngelWhenNotSaddled() {
        addCreatureReady(player1, new SeraphicSteed());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Angel")).isEmpty();
    }

    @Test
    @DisplayName("The attack trigger checks saddled when attackers are declared")
    void checksSaddledAtDeclaration() {
        Permanent steed = addCreatureReady(player1, new SeraphicSteed());

        declareAttackers(player1, List.of(0));
        steed.setSaddled(true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Angel")).isEmpty();
    }
}
