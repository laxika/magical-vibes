package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BoneSaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.ViridianLongbow;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PatriotYoungAvenger.class, GrizzlyBears.class, BoneSaw.class, ViridianLongbow.class})
class PatriotYoungAvengerTest extends BaseCardTest {

    @Test
    void prowessBoostsPatriotWhenCastingNoncreatureSpell() {
        Permanent patriot = addPatriotReady(player1);
        harness.setHand(player1, List.of(new BoneSaw()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, patriot)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, patriot)).isEqualTo(3);
    }

    @Test
    void creatureSpellDoesNotTriggerProwess() {
        Permanent patriot = addPatriotReady(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gqs.getEffectivePower(gd, patriot)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, patriot)).isEqualTo(2);
    }

    @Test
    void equipmentBoostsOtherCreaturesButNotPatriot() {
        Permanent patriot = addPatriotReady(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent equipment = new Permanent(new ViridianLongbow());
        equipment.setAttachedTo(patriot.getId());
        gd.playerBattlefields.get(player1.getId()).add(equipment);

        assertThat(gqs.getEffectivePower(gd, patriot)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, patriot)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);

        equipment.setAttachedTo(null);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    private Permanent addPatriotReady(Player player) {
        Permanent permanent = new Permanent(new PatriotYoungAvenger());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
