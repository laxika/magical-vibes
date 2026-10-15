package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AphettoAlchemist;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.g.GoblinSledder;
import com.github.laxika.magicalvibes.cards.q.QuicksilverElemental;
import com.github.laxika.magicalvibes.cards.s.ShepherdOfRot;
import com.github.laxika.magicalvibes.cards.s.Smother;
import com.github.laxika.magicalvibes.cards.s.SnarlingUndorak;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TribalGolem.class, SnarlingUndorak.class, GoblinSledder.class, GlorySeeker.class,
        AphettoAlchemist.class, ShepherdOfRot.class, Smother.class, QuicksilverElemental.class})
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

    @Test
    @DisplayName("Each tribal keyword is lost independently when its matching permanent leaves")
    void losesKeywordsAsMatchingPermanentsLeave() {
        Permanent golem = addCreatureReady(player1, new TribalGolem());
        Permanent beast = addCreatureReady(player1, new SnarlingUndorak());
        Permanent goblin = addCreatureReady(player1, new GoblinSledder());
        Permanent soldier = addCreatureReady(player1, new GlorySeeker());
        Permanent wizard = addCreatureReady(player1, new AphettoAlchemist());
        List<Permanent> supporters = List.of(beast, goblin, soldier, wizard);
        List<Keyword> keywords = List.of(Keyword.TRAMPLE, Keyword.HASTE, Keyword.FIRST_STRIKE, Keyword.FLYING);

        for (int i = 0; i < supporters.size(); i++) {
            gd.playerBattlefields.get(player1.getId()).remove(supporters.get(i));
            for (int j = 0; j < keywords.size(); j++) {
                assertThat(gqs.hasKeyword(gd, golem, keywords.get(j))).isEqualTo(j > i);
            }
        }
    }

    @Test
    @DisplayName("Losing the last Zombie does not stop an already activated regeneration ability")
    void regenerationResolvesAfterLastZombieDies() {
        Permanent golem = addCreatureReady(player1, new TribalGolem());
        Permanent zombie = addCreatureReady(player1, new ShepherdOfRot());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.setHand(player2, List.of(new Smother()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, zombie.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Shepherd of Rot");
        assertThat(golem.getRegenerationShield()).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A copied regeneration ability does not require the copying creature's controller to have a Zombie")
    void copiedRegenerationHasNoZombieActivationRestriction() {
        Permanent elemental = addCreatureReady(player1, new QuicksilverElemental());
        Permanent golem = addCreatureReady(player2, new TribalGolem());
        addCreatureReady(player2, new ShepherdOfRot());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, golem.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(elemental.getRegenerationShield()).isEqualTo(1);
        assertThat(golem.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("A Golem without a Zombie has no regeneration ability for another creature to gain")
    void cannotCopyRegenerationFromGolemWithoutZombie() {
        addCreatureReady(player1, new QuicksilverElemental());
        addCreatureReady(player1, new ShepherdOfRot());
        Permanent golem = addCreatureReady(player2, new TribalGolem());
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, golem.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
