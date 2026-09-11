package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BoneSaw;
import com.github.laxika.magicalvibes.cards.d.DromokaWarrior;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KorBlademaster.class, DromokaWarrior.class, GrizzlyBears.class, BoneSaw.class})
class KorBlademasterTest extends BaseCardTest {

    @Test
    void equippedWarriorsYouControlHaveDoubleStrike() {
        harness.addToBattlefield(player1, new KorBlademaster());
        Permanent warrior = addCreatureReady(player1, new DromokaWarrior());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        attachEquipment(player1, warrior);
        attachEquipment(player1, bear);

        assertThat(gqs.hasKeyword(gd, warrior, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bear, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void onlyEquippedWarriorsYouControlHaveDoubleStrike() {
        harness.addToBattlefield(player1, new KorBlademaster());
        Permanent ownWarrior = addCreatureReady(player1, new DromokaWarrior());
        Permanent ownUnequippedWarrior = addCreatureReady(player1, new DromokaWarrior());
        Permanent opposingWarrior = addCreatureReady(player2, new DromokaWarrior());
        attachEquipment(player1, ownWarrior);
        attachEquipment(player2, opposingWarrior);

        assertThat(gqs.hasKeyword(gd, ownWarrior, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownUnequippedWarrior, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingWarrior, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private void attachEquipment(Player player, Permanent creature) {
        Permanent equipment = harness.addToBattlefieldAndReturn(player, new BoneSaw());
        equipment.setAttachedTo(creature.getId());
    }
}
