package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyStrength;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaceOfDivinity.class, GrizzlyBears.class, HolyStrength.class})
class FaceOfDivinityTest extends BaseCardTest {

    @Test
    @DisplayName("Gives +2/+2 without the bonus keywords when it is the only Aura")
    void onlyAuraProvidesBoost() {
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        Permanent face = harness.addToBattlefieldAndReturn(player1, new FaceOfDivinity());
        face.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Grants first strike and lifelink while another Aura is attached")
    void anotherAuraProvidesKeywords() {
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        Permanent face = harness.addToBattlefieldAndReturn(player1, new FaceOfDivinity());
        face.setAttachedTo(creature.getId());
        Permanent otherAura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        otherAura.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Loses the bonus keywords when the other Aura leaves")
    void keywordsDisappearWhenOtherAuraLeaves() {
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        Permanent face = harness.addToBattlefieldAndReturn(player1, new FaceOfDivinity());
        face.setAttachedTo(creature.getId());
        Permanent otherAura = harness.addToBattlefieldAndReturn(player1, new HolyStrength());
        otherAura.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(otherAura);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player,
                                       com.github.laxika.magicalvibes.model.Card card) {
        Permanent creature = new Permanent(card);
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }
}
