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

@CardUsed({DragoonsLance.class, GrizzlyBears.class})
class DragoonsLanceTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Dragoon's Lance creates and equips a Hero token")
    void enteringCreatesAndEquipsHero() {
        harness.setHand(player1, List.of(new DragoonsLance()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent lance = findPermanent(player1, "Dragoon's Lance");
        Permanent hero = findPermanent(player1, "Hero");

        assertThat(lance.getAttachedTo()).isEqualTo(hero.getId());
        assertThat(hero.getCard().getPower()).isEqualTo(1);
        assertThat(hero.getCard().getToughness()).isEqualTo(1);
        assertThat(hero.getCard().getSubtypes()).contains(CardSubtype.HERO);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero)).contains(CardSubtype.KNIGHT);
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, hero, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying applies only during the equipped creature controller's turn")
    void flyingAppliesOnlyDuringControllerTurn() {
        Permanent lance = addLanceReady(player1);
        Permanent hero = addCreatureReady(player1, new GrizzlyBears());
        lance.setAttachedTo(hero.getId());

        harness.forceActivePlayer(player2);
        assertThat(gqs.hasKeyword(gd, hero, Keyword.FLYING)).isFalse();
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(3);

        harness.forceActivePlayer(player1);
        assertThat(gqs.hasKeyword(gd, hero, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Equip moves Dragoon's Lance and its abilities to another creature")
    void equipMovesLance() {
        Permanent lance = addLanceReady(player1);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        lance.setAttachedTo(first.getId());

        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, null, second.getId());
        harness.passBothPriorities();

        assertThat(lance.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.effectiveCreatureSubtypes(gd, first)).doesNotContain(CardSubtype.KNIGHT);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.effectiveCreatureSubtypes(gd, second)).contains(CardSubtype.KNIGHT);
        assertThat(gqs.hasKeyword(gd, second, Keyword.FLYING)).isTrue();
    }

    private Permanent addLanceReady(Player player) {
        Permanent permanent = new Permanent(new DragoonsLance());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addCreatureReady(Player player, GrizzlyBears bears) {
        Permanent permanent = new Permanent(bears);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
