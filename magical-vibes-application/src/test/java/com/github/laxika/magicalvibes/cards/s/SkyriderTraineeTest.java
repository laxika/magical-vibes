package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyriderTrainee.class, Pacifism.class})
class SkyriderTraineeTest extends BaseCardTest {

    @Test
    @DisplayName("Does not have flying while unenchanted")
    void unenchanted() {
        Permanent trainee = addTrainee();

        assertThat(gqs.hasKeyword(gd, trainee, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Has flying while enchanted")
    void enchanted() {
        Permanent trainee = addTrainee();
        attachAura(trainee);

        assertThat(gqs.hasKeyword(gd, trainee, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Loses flying when the Aura is removed")
    void losesFlyingWhenAuraIsRemoved() {
        Permanent trainee = addTrainee();
        Permanent aura = attachAura(trainee);

        assertThat(gqs.hasKeyword(gd, trainee, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(aura);

        assertThat(gqs.hasKeyword(gd, trainee, Keyword.FLYING)).isFalse();
    }

    private Permanent addTrainee() {
        return addCreatureReady(player1, new SkyriderTrainee());
    }

    private Permanent attachAura(Permanent creature) {
        Permanent aura = new Permanent(new Pacifism());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return aura;
    }
}
