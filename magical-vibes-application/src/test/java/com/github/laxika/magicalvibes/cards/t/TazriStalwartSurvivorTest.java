package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DrudgeSkeletons;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.l.LoxodonStalwart;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.z.ZuranSpellcaster;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TazriStalwartSurvivor.class, DrudgeSkeletons.class, GrizzlyBears.class,
        LlanowarElves.class, LoxodonStalwart.class,
        ZuranSpellcaster.class, Mountain.class})
class TazriStalwartSurvivorTest extends BaseCardTest {

    @Test
    void grantsManaAbilityOnlyToCreaturesWithAnotherActivatedAbility() {
        addCreatureReady(player1, new TazriStalwartSurvivor());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent loxodon = addCreatureReady(player1, new LoxodonStalwart());
        int toughnessBefore = gqs.getEffectiveToughness(gd, loxodon);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another activated ability");

        harness.activateAbility(player1, 0, 1, null, null);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.getCreatureAbilityOnlyMana(ManaColor.WHITE)).isEqualTo(1);

        harness.activateAbility(player1, 2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, loxodon)).isEqualTo(toughnessBefore + 1);
    }

    @Test
    void millsAndReturnsOnlyCreaturesWithNonManaActivatedAbilities() {
        addCreatureReady(player1, new TazriStalwartSurvivor());
        DrudgeSkeletons regenerate = new DrudgeSkeletons();
        LlanowarElves manaCreature = new LlanowarElves();
        GrizzlyBears vanilla = new GrizzlyBears();
        ZuranSpellcaster damageCreature = new ZuranSpellcaster();
        Mountain land = new Mountain();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(regenerate, manaCreature, vanilla, damageCreature, land));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(regenerate, damageCreature);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(manaCreature, vanilla, land);
    }
}
