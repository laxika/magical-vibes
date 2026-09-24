package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LongLostLances.class, GrizzlyBears.class, LeoninScimitar.class})
class LongLostLancesTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsPlusTwoPower() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent lances = addLancesReady(player1);
        lances.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    void equippedCreaturesYouControlGainFirstStrikeAndVigilanceDuringYourTurn() {
        Permanent lances = addLancesReady(player1);
        Permanent equippedWithLances = addCreatureReady(player1, new GrizzlyBears());
        Permanent equippedWithAnotherEquipment = addCreatureReady(player1, new GrizzlyBears());
        Permanent unequipped = addCreatureReady(player1, new GrizzlyBears());
        Permanent scimitar = addScimitarReady(player1);
        Permanent opponentEquipped = addCreatureReady(player2, new GrizzlyBears());
        Permanent opponentScimitar = addScimitarReady(player2);

        lances.setAttachedTo(equippedWithLances.getId());
        scimitar.setAttachedTo(equippedWithAnotherEquipment.getId());
        opponentScimitar.setAttachedTo(opponentEquipped.getId());
        harness.forceActivePlayer(player1);

        assertHasFirstStrikeAndVigilance(equippedWithLances);
        assertHasFirstStrikeAndVigilance(equippedWithAnotherEquipment);
        assertThat(gqs.hasKeyword(gd, unequipped, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, unequipped, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentEquipped, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentEquipped, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void conditionalKeywordsAreAbsentDuringOpponentsTurn() {
        Permanent lances = addLancesReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        lances.setAttachedTo(creature.getId());

        harness.forceActivePlayer(player2);

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
    }

    @Test
    void equipAbilityAttachesLancesToCreatureYouControl() {
        Permanent lances = addLancesReady(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(lances.getAttachedTo()).isEqualTo(creature.getId());
    }

    private void assertHasFirstStrikeAndVigilance(Permanent creature) {
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, creature, Keyword.VIGILANCE)).isTrue();
    }

    private Permanent addLancesReady(Player player) {
        Permanent permanent = new Permanent(new LongLostLances());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addScimitarReady(Player player) {
        Permanent permanent = new Permanent(new LeoninScimitar());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
