package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirbendingLesson;
import com.github.laxika.magicalvibes.cards.c.CrescentIslandTemple;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WhiteLotusHideout.class, AirbendingLesson.class, CrescentIslandTemple.class, GrizzlyBears.class})
class WhiteLotusHideoutTest extends BaseCardTest {

    @Test
    void firstAbilityAddsColorlessMana() {
        Permanent hideout = addReadyHideout();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(hideout.isTapped()).isTrue();
    }

    @Test
    void secondAbilityAddsLessonAndShrineOnlyMana() {
        Permanent hideout = addReadyHideout();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "BLUE");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.BLUE)).isZero();
        assertThat(pool.getSubtypeSpellOnlyManaForColor(
                Set.of(CardSubtype.LESSON, CardSubtype.SHRINE), ManaColor.BLUE)).isEqualTo(1);
        assertThat(hideout.isTapped()).isTrue();
    }

    @Test
    void thirdAbilityPaysOneAndAddsAnyColorMana() {
        Permanent hideout = addReadyHideout();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.handleListChoice(player1, "RED");

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.COLORLESS)).isZero();
        assertThat(pool.get(ManaColor.RED)).isEqualTo(1);
        assertThat(hideout.isTapped()).isTrue();
    }

    @Test
    void restrictedManaCanCastLessonSpell() {
        addReadyHideout();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "WHITE");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new AirbendingLesson()));
        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getSubtypeSpellOnlyManaTotal(Set.of(CardSubtype.LESSON, CardSubtype.SHRINE))).isZero();
    }

    @Test
    void restrictedManaCanCastShrineSpell() {
        addReadyHideout();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");
        harness.addMana(player1, ManaColor.RED, 3);
        harness.setHand(player1, List.of(new CrescentIslandTemple()));

        harness.castEnchantment(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getSubtypeSpellOnlyManaTotal(Set.of(CardSubtype.LESSON, CardSubtype.SHRINE))).isZero();
    }

    @Test
    void restrictedManaCannotCastNonLessonOrShrineSpell() {
        addReadyHideout();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "WHITE");
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setHand(player1, List.of(new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId())
                .getSubtypeSpellOnlyManaTotal(Set.of(CardSubtype.LESSON, CardSubtype.SHRINE))).isEqualTo(1);
    }

    private Permanent addReadyHideout() {
        Permanent hideout = harness.addToBattlefieldAndReturn(player1, new WhiteLotusHideout());
        hideout.setSummoningSick(false);
        return hideout;
    }
}
