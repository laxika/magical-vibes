package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SakuraTribeElder;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IkraShidiqiTheUsurper.class, GrizzlyBears.class, SakuraTribeElder.class})
class IkraShidiqiTheUsurperTest extends BaseCardTest {

    @Test
    @DisplayName("You gain life equal to the toughness of each creature that deals combat damage")
    void gainsLifeForEachDamagingCreature() {
        Permanent ikra = addCreatureReady(player1, new IkraShidiqiTheUsurper());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        ikra.setAttacking(true);
        bears.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(29);
    }

    @Test
    @DisplayName("Ikra does not trigger for an opponent's creature")
    void ignoresOpponentsCreature() {
        addCreatureReady(player1, new IkraShidiqiTheUsurper());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        bears.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Life gain uses the damaging creature's toughness at resolution")
    void usesToughnessAtResolution() {
        addCreatureReady(player1, new IkraShidiqiTheUsurper());
        Permanent elder = addCreatureReady(player1, new SakuraTribeElder());
        elder.setAttacking(true);

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        elder.setToughnessModifier(3);
        resolveAllTriggers();

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Life gain uses last-known toughness after the damaging creature is sacrificed")
    void usesLastKnownToughnessAfterSacrifice() {
        addCreatureReady(player1, new IkraShidiqiTheUsurper());
        Permanent elder = addCreatureReady(player1, new SakuraTribeElder());
        elder.setToughnessModifier(3);
        elder.setAttacking(true);
        harness.setLibrary(player1, List.of());

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Sakura-Tribe Elder");
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("A pending life gain trigger survives Ikra leaving the battlefield")
    void triggerSurvivesIkraLeavingBattlefield() {
        Permanent ikra = addCreatureReady(player1, new IkraShidiqiTheUsurper());
        Permanent elder = addCreatureReady(player1, new SakuraTribeElder());
        elder.setAttacking(true);

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, ikra));
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Ikra Shidiqi, the Usurper");
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Prevented combat damage does not trigger life gain")
    void preventedDamageDoesNotTrigger() {
        Permanent ikra = addCreatureReady(player1, new IkraShidiqiTheUsurper());
        ikra.setAttacking(true);
        gd.preventAllCombatDamageToPlayers = true;

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An attacking creature with zero power does not trigger life gain")
    void zeroPowerDoesNotTrigger() {
        addCreatureReady(player1, new IkraShidiqiTheUsurper());
        Permanent elder = addCreatureReady(player1, new SakuraTribeElder());
        elder.setPowerModifier(-1);
        elder.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
