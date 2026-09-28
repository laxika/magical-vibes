package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Biotransference.class, GiantGrowth.class, GrizzlyBears.class, SolRing.class})
class BiotransferenceTest extends BaseCardTest {

    @Test
    void creatureSpellTriggersAndCreatesAnArtifactCreatureToken() {
        harness.addToBattlefield(player1, new Biotransference());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int lifeBefore = gd.getLife(player1.getId());
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(gqs.isArtifact(gd, tokens.getFirst())).isTrue();
        assertThat(gqs.isCreature(gd, tokens.getFirst())).isTrue();
        assertThat(gqs.getEffectivePower(gd, tokens.getFirst())).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, tokens.getFirst())).isEqualTo(2);
    }

    @Test
    void artifactSpellTriggers() {
        harness.addToBattlefield(player1, new Biotransference());
        harness.setHand(player1, List.of(new SolRing()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.getLife(player1.getId());
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().isToken()
                        && gqs.getEffectiveCardTypes(gd, permanent).contains(CardType.ARTIFACT));
    }

    @Test
    void nonartifactSpellDoesNotTrigger() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Biotransference());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        int lifeBefore = gd.getLife(player1.getId());
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }
}
