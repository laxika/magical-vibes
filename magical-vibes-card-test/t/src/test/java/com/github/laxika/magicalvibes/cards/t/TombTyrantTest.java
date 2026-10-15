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

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Tomb Tyrant").isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Walking Corpse")).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).filteredOn(
                card -> card.getName().equals("Walking Corpse")).hasSize(2);
    }

    @Test
    @DisplayName("A Zombie sacrificed for the cost cannot supply the third graveyard card")
    void requiresThreeZombiesBeforePayingSacrificeCost() {
        addCreatureReady(player1, new TombTyrant());
        harness.setGraveyard(player1, List.of(new TombTyrant(), new TombTyrant()));
        addManaForAbility();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Zombie creature cards in your graveyard");
        harness.assertOnBattlefield(player1, "Tomb Tyrant");
    }

    @Test
    @DisplayName("Tomb Tyrant can sacrifice itself and return itself after the other Zombies leave")
    void canSacrificeAndReturnItself() {
        TombTyrant card = new TombTyrant();
        Permanent tyrant = addCreatureReady(player1, card);
        harness.setGraveyard(player1, List.of(new TombTyrant(), new TombTyrant(), new TombTyrant()));
        addManaForAbility();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, tyrant.getId());
        harness.assertNotOnBattlefield(player1, "Tomb Tyrant");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);

        harness.setGraveyard(player1, List.of(card));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Tomb Tyrant");
        assertThat(returned.getCard().getId()).isEqualTo(card.getId());
        assertThat(returned.getId()).isNotEqualTo(tyrant.getId());
        assertThat(returned.isTapped()).isFalse();
        assertThat(returned.isSummoningSick()).isTrue();
        harness.assertNotInGraveyard(player1, "Tomb Tyrant");
    }

    @Test
    @DisplayName("The ability resolves harmlessly if no Zombies remain in your graveyard")
    void doesNothingWhenGraveyardIsEmptyAtResolution() {
        Permanent tyrant = addCreatureReady(player1, new TombTyrant());
        harness.setGraveyard(player1, List.of(new TombTyrant(), new TombTyrant(), new TombTyrant()));
        harness.setGraveyard(player2, List.of(new TombTyrant()));
        addManaForAbility();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, tyrant.getId());
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Tomb Tyrant");
        harness.assertNotOnBattlefield(player2, "Tomb Tyrant");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability may be activated during your end step")
    void canActivateDuringYourEndStep() {
        Permanent tyrant = addCreatureReady(player1, new TombTyrant());
        harness.setGraveyard(player1, List.of(new TombTyrant(), new TombTyrant(), new TombTyrant()));
        addManaForAbility();
        harness.forceStep(TurnStep.END_STEP);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, tyrant.getId());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Tomb Tyrant")).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Multiple Tomb Tyrants boost each other and stop boosting when sacrificed")
    void anthemStacksAndEndsWhenSourceLeaves() {
        Permanent first = addCreatureReady(player1, new TombTyrant());
        Permanent second = addCreatureReady(player1, new TombTyrant());
        harness.setGraveyard(player1, List.of(new TombTyrant(), new TombTyrant(), new TombTyrant()));
        addManaForAbility();

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handlePermanentChosen(player1, first.getId());

        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(3);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Tomb Tyrant")).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(4);
    }

    private void addManaForAbility() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
