package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TombTyrant.class, WalkingCorpse.class, GrizzlyBears.class})
class TombTyrantTest extends BaseCardTest {

    @Test
    @DisplayName("Other Zombies you control get +1/+1")
    void buffsOtherZombiesYouControl() {
        addCreatureReady(player1, new TombTyrant());
        addCreatureReady(player1, new WalkingCorpse());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new WalkingCorpse());

        Permanent tyrant = findPermanent(player1, "Tomb Tyrant");
        Permanent zombie = findPermanent(player1, "Walking Corpse");
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        Permanent opponentZombie = findPermanent(player2, "Walking Corpse");

        assertThat(gqs.getEffectivePower(gd, tyrant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, tyrant)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, zombie)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, zombie)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentZombie)).isEqualTo(2);
    }

    @Test
    @DisplayName("The reanimation ability requires three Zombie creature cards in the graveyard")
    void requiresThreeZombieCreatureCards() {
        addCreatureReady(player1, new TombTyrant());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new WalkingCorpse(), new WalkingCorpse(), new GrizzlyBears()));
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Zombie creature cards in your graveyard");
    }

    @Test
    @DisplayName("The reanimation ability is restricted to your turn")
    void onlyActivatesDuringYourTurn() {
        addCreatureReady(player1, new TombTyrant());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new WalkingCorpse(), new WalkingCorpse(), new WalkingCorpse()));
        addManaForAbility();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your turn");
    }

    @Test
    @DisplayName("Sacrificing a creature returns one random Zombie creature to the battlefield")
    void sacrificesCreatureAndReturnsRandomZombie() {
        addCreatureReady(player1, new TombTyrant());
        Permanent fodder = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new WalkingCorpse(), new WalkingCorpse(), new WalkingCorpse()));
        addManaForAbility();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(fodder.getId()));
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Tomb Tyrant").isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(
                permanent -> permanent.getCard().getName().equals("Walking Corpse")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).filteredOn(
                card -> card.getName().equals("Walking Corpse")).hasSize(2);
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
