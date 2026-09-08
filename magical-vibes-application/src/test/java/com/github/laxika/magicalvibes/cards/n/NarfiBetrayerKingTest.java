package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DraugrNecromancer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcehideTroll;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NarfiBetrayerKingTest extends BaseCardTest {

    @Test
    @DisplayName("Other snow and Zombie creatures you control get +1/+1, only once if both")
    void boostsOtherSnowAndZombieCreaturesOnlyOnce() {
        Permanent narfi = addCreatureReady(player1, new NarfiBetrayerKing());
        Permanent zombie = addCreatureReady(player1, new WalkingCorpse());
        Permanent snowCreature = addCreatureReady(player1, new IcehideTroll());
        Permanent snowZombie = addCreatureReady(player1, new DraugrNecromancer());
        Permanent nonmatching = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentZombie = addCreatureReady(player2, new WalkingCorpse());

        assertThat(gqs.getEffectivePower(gd, narfi)).isEqualTo(narfi.getCard().getPower());
        assertThat(gqs.getEffectiveToughness(gd, narfi)).isEqualTo(narfi.getCard().getToughness());
        assertBoosted(zombie);
        assertBoosted(snowCreature);
        assertBoosted(snowZombie);
        assertUnchanged(nonmatching);
        assertUnchanged(opponentZombie);
    }

    @Test
    @DisplayName("Three snow mana returns Narfi from the graveyard tapped")
    void returnsFromGraveyardTapped() {
        prepareGraveyardAbility(new NarfiBetrayerKing());
        addSnowMana(player1, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent narfi = findPermanent(player1, "Narfi, Betrayer King");
        assertThat(narfi.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Narfi, Betrayer King");
    }

    @Test
    @DisplayName("Narfi's graveyard ability requires snow mana")
    void requiresSnowMana() {
        prepareGraveyardAbility(new NarfiBetrayerKing());
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    private void prepareGraveyardAbility(Card card) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setGraveyard(player1, List.of(card));
    }

    private void addSnowMana(Player player, int amount) {
        ManaPool pool = gd.playerManaPools.get(player.getId());
        pool.add(ManaColor.BLUE, amount);
        pool.addSnowMana(ManaColor.BLUE, amount);
    }

    private void assertBoosted(Permanent permanent) {
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(permanent.getCard().getPower() + 1);
        assertThat(gqs.getEffectiveToughness(gd, permanent))
                .isEqualTo(permanent.getCard().getToughness() + 1);
    }

    private void assertUnchanged(Permanent permanent) {
        assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(permanent.getCard().getPower());
        assertThat(gqs.getEffectiveToughness(gd, permanent))
                .isEqualTo(permanent.getCard().getToughness());
    }
}
