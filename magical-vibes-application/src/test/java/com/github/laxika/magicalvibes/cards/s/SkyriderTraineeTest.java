package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GuardiansMagemark;
import com.github.laxika.magicalvibes.cards.s.SwordOfTheParuns;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyriderTrainee.class, GuardiansMagemark.class, SwordOfTheParuns.class})
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

    @Test
    @DisplayName("Does not have flying when an Equipment is attached")
    void equipmentDoesNotCountAsEnchanted() {
        Permanent trainee = addTrainee();
        Permanent equipment = harness.addToBattlefieldAndReturn(player1, new SwordOfTheParuns());
        equipment.setAttachedTo(trainee.getId());

        assertThat(gqs.hasKeyword(gd, trainee, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Has flying when enchanted by an opponent's Aura")
    void opponentsAuraGrantsFlying() {
        Permanent trainee = addTrainee();
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new GuardiansMagemark());
        aura.setAttachedTo(trainee.getId());

        assertThat(gqs.hasKeyword(gd, trainee, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Keeps flying until the last attached Aura leaves")
    void keepsFlyingUntilLastAuraLeaves() {
        Permanent trainee = addTrainee();
        Permanent firstAura = attachAura(trainee);
        Permanent secondAura = attachAura(trainee);

        assertThat(gqs.hasKeyword(gd, trainee, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(firstAura);

        assertThat(gqs.hasKeyword(gd, trainee, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(secondAura);

        assertThat(gqs.hasKeyword(gd, trainee, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An Aura attached to another creature does not grant flying")
    void auraOnAnotherCreatureDoesNotGrantFlying() {
        Permanent trainee = addTrainee();
        Permanent otherTrainee = addTrainee();
        attachAura(otherTrainee);

        assertThat(gqs.hasKeyword(gd, trainee, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherTrainee, Keyword.FLYING)).isTrue();
    }

    private Permanent addTrainee() {
        return addCreatureReady(player1, new SkyriderTrainee());
    }

    private Permanent attachAura(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new GuardiansMagemark());
        aura.setAttachedTo(creature.getId());
        return aura;
    }
}
