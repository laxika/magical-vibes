package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AphettoAlchemist;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.g.GoblinSledder;
import com.github.laxika.magicalvibes.cards.s.ShepherdOfRot;
import com.github.laxika.magicalvibes.cards.s.SnarlingUndorak;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TribalGolem.class, SnarlingUndorak.class, GoblinSledder.class, GlorySeeker.class,
        AphettoAlchemist.class, ShepherdOfRot.class})
class TribalGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Gains each keyword while its controller has the matching creature type")
    void gainsKeywordsFromControlledCreatureTypes() {
        Permanent golem = addCreatureReady(player1, new TribalGolem());
        addCreatureReady(player1, new SnarlingUndorak());
        addCreatureReady(player1, new GoblinSledder());
        addCreatureReady(player1, new GlorySeeker());
        addCreatureReady(player1, new AphettoAlchemist());
        addCreatureReady(player1, new ShepherdOfRot());

        assertThat(gqs.hasKeyword(gd, golem, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not gain tribal keywords without the matching creature types")
    void doesNotGainKeywordsWithoutMatchingCreatureTypes() {
        Permanent golem = addCreatureReady(player1, new TribalGolem());

        assertThat(gqs.hasKeyword(gd, golem, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Only controlled creature types grant the tribal keywords")
    void opponentCreatureTypesDoNotGrantKeywords() {
        Permanent golem = addCreatureReady(player1, new TribalGolem());
        addCreatureReady(player2, new SnarlingUndorak());
        addCreatureReady(player2, new GoblinSledder());
        addCreatureReady(player2, new GlorySeeker());
        addCreatureReady(player2, new AphettoAlchemist());

        assertThat(gqs.hasKeyword(gd, golem, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Regeneration can be activated while its controller controls a Zombie")
    void regeneratesWithZombie() {
        Permanent golem = addCreatureReady(player1, new TribalGolem());
        addCreatureReady(player1, new ShepherdOfRot());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(golem.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration cannot be activated from an opponent's Zombie")
    void cannotRegenerateWithOpponentsZombie() {
        addCreatureReady(player1, new TribalGolem());
        addCreatureReady(player2, new ShepherdOfRot());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Activate only if you control a Zombie");
    }

    @Test
    @DisplayName("Regeneration requires black mana")
    void cannotRegenerateWithOnlyColorlessMana() {
        addCreatureReady(player1, new TribalGolem());
        addCreatureReady(player1, new ShepherdOfRot());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Regeneration cannot be activated without a Zombie")
    void cannotRegenerateWithoutZombie() {
        addCreatureReady(player1, new TribalGolem());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Activate only if you control a Zombie");
    }
}
