package com.github.laxika.magicalvibes.cards.p;
import com.github.laxika.magicalvibes.model.CounterType;

import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhenaxGodOfDeception.class, WalkingCorpse.class})
class PhenaxGodOfDeceptionTest extends BaseCardTest {

    @Test
    @DisplayName("Phenax is not a creature below seven combined blue and black devotion")
    void isNotCreatureBelowDevotionThreshold() {
        Permanent phenax = addPhenax();
        addBlackDevotion(4);

        assertThat(gqs.isCreature(gd, phenax)).isFalse();
        assertThat(gqs.isEnchantment(gd, phenax)).isTrue();
    }

    @Test
    @DisplayName("Phenax becomes a creature at seven combined blue and black devotion")
    void becomesCreatureAtDevotionThreshold() {
        Permanent phenax = addPhenax();
        addBlackDevotion(5);

        assertThat(gqs.isCreature(gd, phenax)).isTrue();
    }

    @Test
    @DisplayName("A creature you control can tap to mill cards equal to its toughness")
    void creatureMillsEqualToItsToughness() {
        Permanent phenax = addPhenax();
        addBlackDevotion(5);
        phenax.setSummoningSick(false);
        harness.setLibrary(player2, IntStream.range(0, 10)
                .mapToObj(i -> new WalkingCorpse()).toList());
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 7);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(7);
        assertThat(phenax.isTapped()).isTrue();
    }

    @Test
    void grantsMillToOtherCreaturesWhilePhenaxIsNotACreature() {
        addPhenax();
        Permanent corpse = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        corpse.setSummoningSick(false);
        int before = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(before - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(corpse.isTapped()).isTrue();
    }

    @Test
    void grantedAbilityCanTargetItsController() {
        addPhenax();
        Permanent corpse = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        corpse.setSummoningSick(false);
        int before = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(before - 2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void usesToughnessAtResolutionRatherThanActivation() {
        addPhenax();
        Permanent corpse = harness.addToBattlefieldAndReturn(player1, new WalkingCorpse());
        corpse.setSummoningSick(false);
        int before = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 1, null, player2.getId());
        corpse.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(before - 5);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
    }

    @Test
    void millsZeroWhenPhenaxStopsBeingACreatureBeforeResolution() {
        Permanent phenax = addPhenax();
        addBlackDevotion(5);
        phenax.setSummoningSick(false);
        int before = gd.playerDecks.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        Permanent devotionSource = gd.playerBattlefields.get(player1.getId()).get(1);
        harness.getPermanentRemovalService().sacrificePermanentToGraveyard(gd, devotionSource);
        assertThat(gqs.isCreature(gd, phenax)).isFalse();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(before);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private Permanent addPhenax() {
        return harness.addToBattlefieldAndReturn(player1, new PhenaxGodOfDeception());
    }

    private void addBlackDevotion(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new WalkingCorpse());
        }
    }

}
