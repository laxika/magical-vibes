package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BorosRecruit;
import com.github.laxika.magicalvibes.cards.c.Char;
import com.github.laxika.magicalvibes.cards.t.TatteredDrake;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DimirDoppelganger.class, TatteredDrake.class, BorosRecruit.class, Char.class})
class DimirDoppelgangerTest extends BaseCardTest {

    @Test
    void exilesCreatureFromAnyGraveyardAndBecomesCopyWithAbility() {
        Permanent doppelganger = addReadyDoppelganger();
        TatteredDrake target = new TatteredDrake();
        harness.setGraveyard(player2, List.of(target));
        addActivationMana();

        harness.activateAbility(player1, battlefieldIndex(doppelganger), 0,
                null, target.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.activateAbility(player1, battlefieldIndex(doppelganger), 0, null, null);
        harness.passBothPriorities();

        assertThat(doppelganger.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    void retainedAbilityCanBeActivatedAgainAfterCopying() {
        Permanent doppelganger = addReadyDoppelganger();
        BorosRecruit firstTarget = new BorosRecruit();
        BorosRecruit secondTarget = new BorosRecruit();
        harness.setGraveyard(player2, List.of(firstTarget));
        addActivationMana();

        harness.activateAbility(player1, battlefieldIndex(doppelganger), 0,
                null, firstTarget.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.setGraveyard(player1, List.of(secondTarget));
        addActivationMana();
        harness.activateAbility(player1, battlefieldIndex(doppelganger), 0,
                null, secondTarget.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(secondTarget);
        assertThat(doppelganger.getCard().getActivatedAbilities()).hasSize(1);
    }

    @Test
    void rejectsNoncreatureGraveyardTarget() {
        Permanent doppelganger = addReadyDoppelganger();
        Char target = new Char();
        harness.setGraveyard(player2, List.of(target));
        addActivationMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(doppelganger), 0,
                null, target.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void doesNothingIfTargetLeavesGraveyardBeforeResolution() {
        Permanent doppelganger = addReadyDoppelganger();
        BorosRecruit target = new BorosRecruit();
        harness.setGraveyard(player2, List.of(target));
        addActivationMana();

        harness.activateAbility(player1, battlefieldIndex(doppelganger), 0,
                null, target.getId(), Zone.GRAVEYARD);
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(doppelganger.getCard().getActivatedAbilities()).hasSize(1);
    }

    @Test
    void exilesTargetEvenIfSourceLeavesBeforeResolution() {
        Permanent doppelganger = addReadyDoppelganger();
        BorosRecruit target = new BorosRecruit();
        harness.setGraveyard(player2, List.of(target));
        addActivationMana();

        harness.activateAbility(player1, battlefieldIndex(doppelganger), 0,
                null, target.getId(), Zone.GRAVEYARD);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(harness.getGameData(), doppelganger));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(target);
    }

    private Permanent addReadyDoppelganger() {
        return addCreatureReady(player1, new DimirDoppelganger());
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
