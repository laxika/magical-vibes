package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhyrexianArena;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MajorTeroh.class, BlackKnight.class, EliteVanguard.class, GrizzlyBears.class, PhyrexianArena.class})
class MajorTerohTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing Major Teroh exiles all black creatures")
    void exilesAllBlackCreatures() {
        Permanent majorTeroh = addCreatureReady(player1, new MajorTeroh());
        Permanent ownBlackCreature = addCreatureReady(player1, new BlackKnight());
        Permanent opposingBlackCreature = addCreatureReady(player2, new BlackKnight());
        Permanent opposingGreenCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent opposingWhiteCreature = addCreatureReady(player2, new EliteVanguard());
        Permanent blackEnchantment = harness.addToBattlefieldAndReturn(player2, new PhyrexianArena());

        addAbilityMana();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(majorTeroh, ownBlackCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opposingBlackCreature);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(opposingGreenCreature, opposingWhiteCreature, blackEnchantment);
    }

    @Test
    @DisplayName("Sacrifice is paid before Major Teroh's ability resolves")
    void sacrificeIsPaidOnActivation() {
        Permanent majorTeroh = addCreatureReady(player1, new MajorTeroh());
        addAbilityMana();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(majorTeroh);
        harness.assertInGraveyard(player1, "Major Teroh");
        assertThat(gd.stack).hasSize(1);
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 2);
    }
}
