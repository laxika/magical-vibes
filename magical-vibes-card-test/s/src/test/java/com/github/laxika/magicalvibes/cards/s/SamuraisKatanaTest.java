package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.TownGreeter;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SamuraisKatana.class, TownGreeter.class})
class SamuraisKatanaTest extends BaseCardTest {

    @Test
    @DisplayName("Job select creates and equips a Hero, then grants the equipped creature's abilities")
    void jobSelectCreatesAndEquipsHero() {
        harness.castFromHand(player1, new SamuraisKatana(), "{2}{R}");
        resolveAllTriggers();

        Permanent katana = findPermanent(player1, "Samurai's Katana");
        Permanent hero = findPermanent(player1, "Hero");

        assertThat(katana.getAttachedTo()).isEqualTo(hero.getId());
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, hero, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, hero, Keyword.HASTE)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero))
                .contains(CardSubtype.HERO, CardSubtype.SAMURAI);
    }

    @Test
    @DisplayName("Equip moves Samurai's Katana and its grants to another creature")
    void equipMovesKatana() {
        Permanent katana = addCreatureReady(player1, new SamuraisKatana());
        Permanent first = addCreatureReady(player1, new TownGreeter());
        Permanent second = addCreatureReady(player1, new TownGreeter());
        katana.setAttachedTo(first.getId());

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(katana.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, first, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, first)).doesNotContain(CardSubtype.SAMURAI);
        assertThat(gqs.effectiveCreatureSubtypes(gd, second)).contains(CardSubtype.SAMURAI);
    }

    @Test
    @DisplayName("Job select still creates a Hero when the Equipment leaves before resolution")
    void jobSelectCreatesHeroWithoutEquipment() {
        harness.castFromHand(player1, new SamuraisKatana(), "{2}{R}");
        harness.passBothPriorities();
        Permanent katana = findPermanent(player1, "Samurai's Katana");
        assertThat(countPermanents(player1, "Hero")).isZero();

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, katana);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Hero")).isEqualTo(1);
        Permanent hero = findPermanent(player1, "Hero");
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, hero, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, hero, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero))
                .contains(CardSubtype.HERO).doesNotContain(CardSubtype.SAMURAI);
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void equipRejectsOpponentsCreature() {
        Permanent katana = harness.addToBattlefieldAndReturn(player1, new SamuraisKatana());
        Permanent creature = addCreatureReady(player2, new TownGreeter());
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
        assertThat(katana.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void equipRequiresSorceryTiming() {
        harness.addToBattlefield(player1, new SamuraisKatana());
        Permanent creature = addCreatureReady(player1, new TownGreeter());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }
}
