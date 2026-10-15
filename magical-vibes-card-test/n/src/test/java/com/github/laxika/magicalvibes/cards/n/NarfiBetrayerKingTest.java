package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DraugrNecromancer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcehideTroll;
import com.github.laxika.magicalvibes.cards.p.PoisonTheCup;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NarfiBetrayerKing.class, WalkingCorpse.class, IcehideTroll.class,
        DraugrNecromancer.class, GrizzlyBears.class, PoisonTheCup.class})
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

    @Test
    @DisplayName("Only the activated copy returns, even with another Narfi in the graveyard")
    void returnsOnlyTheActivatedCopy() {
        NarfiBetrayerKing source = new NarfiBetrayerKing();
        NarfiBetrayerKing other = new NarfiBetrayerKing();
        prepareGraveyardAbility(source);
        harness.setGraveyard(player1, List.of(source, other));
        addSnowMana(player1, 3);

        harness.activateGraveyardAbility(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(source, other);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Narfi, Betrayer King").getCard()).isSameAs(source);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(other);
    }

    @Test
    @DisplayName("Two snow mana and ordinary mana cannot pay for Narfi's ability")
    void requiresAllThreeManaToBeSnow() {
        prepareGraveyardAbility(new NarfiBetrayerKing());
        addSnowMana(player1, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
        harness.assertInGraveyard(player1, "Narfi, Betrayer King");
        harness.assertNotOnBattlefield(player1, "Narfi, Betrayer King");
    }

    @Test
    @DisplayName("Snow mana of different colors pays for Narfi during the opponent's upkeep")
    void returnsAtInstantSpeedUsingMixedSnowMana() {
        prepareGraveyardAbility(new NarfiBetrayerKing());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.addSnowMana(ManaColor.WHITE, 1);
        pool.addSnowMana(ManaColor.RED, 1);
        pool.addSnowMana(ManaColor.COLORLESS, 1);

        harness.activateGraveyardAbility(player1, 0);
        assertThat(pool.getTotal()).isZero();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Narfi, Betrayer King").isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Narfi, Betrayer King");
    }

    @Test
    @DisplayName("Narfi's boost ends when it leaves the battlefield")
    void boostEndsWhenNarfiDies() {
        Permanent narfi = addCreatureReady(player1, new NarfiBetrayerKing());
        Permanent troll = addCreatureReady(player1, new IcehideTroll());
        assertBoosted(troll);
        harness.setHand(player2, List.of(new PoisonTheCup()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player2, 0, narfi.getId());

        harness.assertInGraveyard(player1, "Narfi, Betrayer King");
        assertUnchanged(troll);
    }

    @Test
    @DisplayName("An older activation cannot return Narfi after it returns and dies again")
    void olderActivationDoesNotReturnNewGraveyardObject() {
        prepareGraveyardAbility(new NarfiBetrayerKing());
        addSnowMana(player1, 6);

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Narfi, Betrayer King");
        assertThat(gd.stack).hasSize(1);
        harness.setHand(player2, List.of(new PoisonTheCup()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, returned.getId());

        harness.assertInGraveyard(player1, "Narfi, Betrayer King");
        harness.assertNotOnBattlefield(player1, "Narfi, Betrayer King");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Narfi, Betrayer King");
        harness.assertNotOnBattlefield(player1, "Narfi, Betrayer King");
    }

    private void prepareGraveyardAbility(Card card) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setGraveyard(player1, List.of(card));
    }

    private void addSnowMana(Player player, int amount) {
        ManaPool pool = gd.playerManaPools.get(player.getId());
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
