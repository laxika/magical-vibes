package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.e.ExpeditionChampion;
import com.github.laxika.magicalvibes.cards.m.MesaLynx;
import com.github.laxika.magicalvibes.cards.u.UtilityKnife;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KorBlademaster.class, ExpeditionChampion.class, MesaLynx.class, UtilityKnife.class})
class KorBlademasterTest extends BaseCardTest {

    @Test
    void equippedWarriorsYouControlHaveDoubleStrike() {
        harness.addToBattlefield(player1, new KorBlademaster());
        Permanent warrior = addCreatureReady(player1, new ExpeditionChampion());
        Permanent nonWarrior = addCreatureReady(player1, new MesaLynx());
        attachEquipment(player1, warrior);
        attachEquipment(player1, nonWarrior);

        assertThat(gqs.hasKeyword(gd, warrior, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonWarrior, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void onlyEquippedWarriorsYouControlHaveDoubleStrike() {
        harness.addToBattlefield(player1, new KorBlademaster());
        Permanent ownWarrior = addCreatureReady(player1, new ExpeditionChampion());
        Permanent ownUnequippedWarrior = addCreatureReady(player1, new ExpeditionChampion());
        Permanent opposingWarrior = addCreatureReady(player2, new ExpeditionChampion());
        attachEquipment(player1, ownWarrior);
        attachEquipment(player2, opposingWarrior);

        assertThat(gqs.hasKeyword(gd, ownWarrior, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, ownUnequippedWarrior, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingWarrior, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void unequippedBlademasterDealsDamageInBothCombatDamageSteps() {
        addCreatureReady(player1, new KorBlademaster());

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    void equipmentControlledByOpponentStillEnablesDoubleStrike() {
        harness.addToBattlefield(player1, new KorBlademaster());
        Permanent warrior = addCreatureReady(player1, new ExpeditionChampion());
        attachEquipment(player2, warrior);

        assertThat(gqs.hasKeyword(gd, warrior, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void doubleStrikeMovesWithEquipmentAndEndsWhenItDetaches() {
        harness.addToBattlefield(player1, new KorBlademaster());
        Permanent first = addCreatureReady(player1, new ExpeditionChampion());
        Permanent second = addCreatureReady(player1, new ExpeditionChampion());
        Permanent equipment = attachEquipment(player1, first);

        assertThat(gqs.hasKeyword(gd, first, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.DOUBLE_STRIKE)).isFalse();

        equipment.setAttachedTo(second.getId());

        assertThat(gqs.hasKeyword(gd, first, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.DOUBLE_STRIKE)).isTrue();

        equipment.setAttachedTo(null);

        assertThat(gqs.hasKeyword(gd, second, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void grantedDoubleStrikeEndsWhenBlademasterLeaves() {
        Permanent blademaster = harness.addToBattlefieldAndReturn(player1, new KorBlademaster());
        Permanent warrior = addCreatureReady(player1, new ExpeditionChampion());
        attachEquipment(player1, warrior);
        assertThat(gqs.hasKeyword(gd, warrior, Keyword.DOUBLE_STRIKE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(blademaster);

        assertThat(gqs.hasKeyword(gd, warrior, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    private Permanent attachEquipment(Player player, Permanent creature) {
        Permanent equipment = harness.addToBattlefieldAndReturn(player, new UtilityKnife());
        equipment.setAttachedTo(creature.getId());
        return equipment;
    }
}
