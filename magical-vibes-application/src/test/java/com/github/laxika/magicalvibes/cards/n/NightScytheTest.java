package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NightScythe.class, GrizzlyBears.class})
class NightScytheTest extends BaseCardTest {

    @Test
    void entersAndCreatesAnArtifactNecronWarriorToken() {
        harness.setHand(player1, List.of(new NightScythe()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getCard().getName()).isEqualTo("Necron Warrior");
        assertThat(gqs.getEffectivePower(gd, tokens.getFirst())).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tokens.getFirst())).isEqualTo(2);
        assertThat(gqs.getEffectiveCardTypes(gd, tokens.getFirst())).contains(CardType.ARTIFACT);
    }

    @Test
    void crewsWithTwoPower() {
        Permanent scythe = addCreatureReady(player1, new NightScythe());
        Permanent crew = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(scythe), null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, scythe)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }
}
