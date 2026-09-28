package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DancersChakrams.class, GrizzlyBears.class})
class DancersChakramsTest extends BaseCardTest {

    @Test
    @DisplayName("Job select creates and equips a Hero Performer")
    void jobSelectCreatesAndEquipsHeroPerformer() {
        harness.setHand(player1, List.of(new DancersChakrams()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent chakrams = findPermanent(player1, "Dancer's Chakrams");
        Permanent hero = findPermanent(player1, "Hero");

        assertThat(chakrams.getAttachedTo()).isEqualTo(hero.getId());
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, hero, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero))
                .contains(CardSubtype.HERO, CardSubtype.PERFORMER);
    }

    @Test
    @DisplayName("Equip moves Dancer's Chakrams and its grants to another creature")
    void equipMovesChakrams() {
        Permanent chakrams = addChakramsReady(player1);
        Permanent first = addCreatureReady(player1);
        Permanent second = addCreatureReady(player1);
        chakrams.setAttachedTo(first.getId());

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(chakrams.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.effectiveCreatureSubtypes(gd, first)).doesNotContain(CardSubtype.PERFORMER);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, second, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.effectiveCreatureSubtypes(gd, second)).contains(CardSubtype.PERFORMER);
    }

    @Test
    @DisplayName("Other commanders you control get +2/+2 and lifelink")
    void boostsOtherCommandersYouControl() {
        Permanent chakrams = addChakramsReady(player1);
        Permanent commander = addCreatureReady(player1);
        Permanent nonCommander = addCreatureReady(player1);
        Permanent opponentCommander = addCreatureReady(player2);
        gd.makeCommander(player1.getId(), commander.getCard());
        gd.makeCommander(player2.getId(), opponentCommander.getCard());

        assertThat(gqs.getEffectivePower(gd, chakrams)).isEqualTo(0);
        assertThat(gqs.getEffectivePower(gd, commander)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, commander)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, commander, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.getEffectivePower(gd, nonCommander)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, nonCommander, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.getEffectivePower(gd, opponentCommander)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentCommander, Keyword.LIFELINK)).isFalse();
    }

    private Permanent addChakramsReady(Player player) {
        Permanent permanent = new Permanent(new DancersChakrams());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addCreatureReady(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
