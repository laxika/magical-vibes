package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SagesNouliths.class, GrizzlyBears.class})
class SagesNoulithsTest extends BaseCardTest {

    @Test
    void enteringCreatesAndEquipsHero() {
        harness.setHand(player1, List.of(new SagesNouliths()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent nouliths = findPermanent(player1, "Sage's Nouliths");
        Permanent hero = findPermanent(player1, "Hero");

        assertThat(nouliths.getAttachedTo()).isEqualTo(hero.getId());
        assertThat(gqs.getEffectivePower(gd, hero)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hero)).isEqualTo(1);
        assertThat(gqs.effectiveCreatureSubtypes(gd, hero))
                .contains(CardSubtype.HERO, CardSubtype.CLERIC);
    }

    @Test
    void equippedCreatureCanUntapAnAttackingCreature() {
        Permanent nouliths = addNoulithsReady(player1);
        Permanent creature = addCreatureReady(player1);
        nouliths.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(1));
        assertThat(creature.isTapped()).isTrue();

        harness.handlePermanentChosen(player1, creature.getId());
        resolveAllTriggers();

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void attackTriggerCannotTargetNonAttackingCreature() {
        Permanent nouliths = addNoulithsReady(player1);
        Permanent creature = addCreatureReady(player1);
        Permanent bystander = addCreatureReady(player1);
        nouliths.setAttachedTo(creature.getId());

        declareAttackers(player1, List.of(1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bystander.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addNoulithsReady(Player player) {
        Permanent permanent = new Permanent(new SagesNouliths());
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
