package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagitekArmor.class, GrizzlyBears.class})
class MagitekArmorTest extends BaseCardTest {

    @Test
    void enteringCreatesColorlessHeroToken() {
        harness.setHand(player1, List.of(new MagitekArmor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> heroes = findPermanents(player1, "Hero").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(heroes).hasSize(1);
        Permanent hero = heroes.getFirst();
        assertThat(hero.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(hero.getCard().getColor()).isNull();
        assertThat(hero.getCard().getSubtypes()).containsExactly(CardSubtype.HERO);
        assertThat(hero.getCard().getPower()).isEqualTo(1);
        assertThat(hero.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    void crewOneAnimatesArmorAndTapsTheCrew() {
        Permanent armor = addArmorReady(player1);
        Permanent crew = addCreatureReady(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, armor)).isTrue();
        assertThat(gqs.getEffectivePower(gd, armor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, armor)).isEqualTo(4);
        assertThat(crew.isTapped()).isTrue();
    }

    private Permanent addArmorReady(Player player) {
        Permanent permanent = new Permanent(new MagitekArmor());
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
